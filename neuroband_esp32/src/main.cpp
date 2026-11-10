/**
 * NeuroBand AI - ESP32 Wearable Sensor Bridge (Ultra Precision Edition)
 * Features:
 * - Direct Register-level MPU-6050 Driver (Works with 100% of all chips & clones on 0x68/0x69)
 * - Real-Time Optical PPG Beat-by-Beat Peak Detection (MAX30102)
 * - GSR on GPIO 34, converted to uS from a resistor calibration table (see GSR_CAL_MV)
 * - Real-time On-Screen Medical Remedy & Threshold Warning System
 * - Port 5000 REST API with full CORS support for Android App
 *
 * Not measured (no sensor): blood pressure (always 0), body temperature without an
 * MLX90614 (0 = no reading). Not a medical device.
 */

#include <WiFi.h>
#include <WebServer.h>
#include <Wire.h>
#include "MAX30105.h"
#include "heartRate.h"
#include "spo2_algorithm.h"
#include "gsr_cal.h"

// ============================================================
// 1. WI-FI CONFIGURATION
// ============================================================
const char* WIFI_SSID     = "Goodboys";
const char* WIFI_PASSWORD = "123321123";

WebServer server(5000);

// ============================================================
// 2. PIN DEFINITIONS & INSTANCES
// ============================================================
#define GSR_PIN     34  // Analog ADC1 input for physical GSR sensor
#define I2C_SDA_PIN 21  // Shared I2C Data line
#define I2C_SCL_PIN 22  // Shared I2C Clock line
#define BATT_PIN    35  // Battery + through 100k/100k divider (reads ~0 / noise if not wired)

// --- GSR CALIBRATION (do once) ---
// Put each resistor across the two finger electrodes, read "GSR mV" on the web page
// or serial log, and write that number below in the same order. Until you do,
// stressGsr is reported as 0 (uncalibrated).
//                         2M    1M    470k   220k   100k
const float GSR_CAL_US[] = {0.5,  1.0,  2.13,  4.55,  10.0};
const float GSR_CAL_MV[] = {0,    0,    0,     0,     0};   // <-- your measured mV
const int   GSR_CAL_N    = 5;
const bool  GSR_CALIBRATED = GSR_CAL_MV[0] != GSR_CAL_MV[GSR_CAL_N - 1];

// SpO2 calibration knob: cheap MAX30102 boards are not factory calibrated.
// Compare against a clip-on pulse oximeter and set the difference here.
const int SPO2_OFFSET = 0;

MAX30105 particleSensor;

uint8_t mpuAddr = 0x68;
bool hasMpu = false;
bool hasMax = false;
bool hasMlx = false; // Dedicated non-contact IR temperature sensor (MLX90614 at 0x5A)

// Real-time Telemetry State
int heartRate = 0;
int spO2 = 0;
bool fingerDetected = false;
// /api/simulate holds its values this long, else the sensor loop overwrites them within 40 ms
const unsigned long SIM_HOLD_MS = 15000;
bool simActive = false;
unsigned long simStart = 0;
float bodyTemp = 0.0;  // 0 = no body temperature sensor (MLX90614) reading
float mpuTemp = 31.5;
float rawGsrUs = 0.0;
float gsrMv = 0.0;
float batteryV = 0.0;
int steps = 0;
float accelNet = 1.0;
float gyroNet = 0.0;
float ax = 0.0, ay = 0.0, az = 1.0;
float gx = 0.0, gy = 0.0, gz = 0.0;
long rawRed = 0;
long rawIr = 0;

// Pulse Beat Detector State (timed by sample count, not millis, so bursts read from the FIFO stay accurate)
#define MAX_ADDR      0x57
#define PPG_SAMPLE_MS 10   // setup(): 400 Hz / sampleAverage 4 = 100 samples/s
uint32_t ppgSampleCount = 0;
uint32_t lastBeatSample = 0;
int beatBuffer[4] = {0, 0, 0, 0};
int beatIdx = 0;
int validBeatsCount = 0;

// SpO2 window: spo2_algorithm expects 100 samples at 25 Hz (4 s). We average 4 raw samples into 1.
uint32_t spo2IrBuf[100], spo2RedBuf[100];
int spo2BufLen = 0;
uint32_t decimIr = 0, decimRed = 0;
int decimCount = 0;

// Pedometer Stride State Machine
enum PedometerState { STEP_ARMED, STEP_PEAK_FOUND };
PedometerState pedState = STEP_ARMED;
unsigned long lastStepMs = 0;
unsigned long peakDetectedMs = 0;

unsigned long lastMpuCheck = 0;
unsigned long lastMaxCheck = 0;
unsigned long lastMlxCheck = 0;
unsigned long lastConsolePrint = 0;
unsigned long lastTempRead = 0;
unsigned long lastFifoDrain = 0;

// ============================================================
// 3. DIRECT MPU-6050 HARDWARE DRIVER (Direct Wire Registers)
// ============================================================
bool initMpuDirect() {
  Wire.beginTransmission(0x68);
  if (Wire.endTransmission() == 0) {
    mpuAddr = 0x68;
  } else {
    Wire.beginTransmission(0x69);
    if (Wire.endTransmission() == 0) {
      mpuAddr = 0x69;
    } else {
      return false; // Neither 0x68 nor 0x69 responded
    }
  }

  // Wake up MPU-6050 from sleep mode (Register 0x6B = 0)
  Wire.beginTransmission(mpuAddr);
  Wire.write(0x6B);
  Wire.write(0x00);
  if (Wire.endTransmission() != 0) return false;
  delay(10);

  // Set Accelerometer config (+/- 4g)
  Wire.beginTransmission(mpuAddr);
  Wire.write(0x1C);
  Wire.write(0x08);
  Wire.endTransmission();

  // Set Gyroscope config (+/- 500 deg/s)
  Wire.beginTransmission(mpuAddr);
  Wire.write(0x1B);
  Wire.write(0x08);
  Wire.endTransmission();

  Serial.printf("[MPU6050] Connected directly at address 0x%02X!\n", mpuAddr);
  return true;
}

bool readMpuDirect(float &outAx, float &outAy, float &outAz, float &outGx, float &outGy, float &outGz, float &outTemp) {
  Wire.beginTransmission(mpuAddr);
  Wire.write(0x3B);
  if (Wire.endTransmission(false) != 0) return false;

  if (Wire.requestFrom(mpuAddr, (uint8_t)14) < 14) return false;

  int16_t rAx = (Wire.read() << 8) | Wire.read();
  int16_t rAy = (Wire.read() << 8) | Wire.read();
  int16_t rAz = (Wire.read() << 8) | Wire.read();
  int16_t rT  = (Wire.read() << 8) | Wire.read();
  int16_t rGx = (Wire.read() << 8) | Wire.read();
  int16_t rGy = (Wire.read() << 8) | Wire.read();
  int16_t rGz = (Wire.read() << 8) | Wire.read();

  outAx = rAx / 8192.0; // +/-4g
  outAy = rAy / 8192.0;
  outAz = rAz / 8192.0;

  outGx = rGx / 65.5;  // +/-500 deg/s
  outGy = rGy / 65.5;
  outGz = rGz / 65.5;

  // Calibrated MPU die temp (subtracting ~15.5C internal self-heating offset)
  outTemp = (rT / 340.0) + 21.0;
  return true;
}

// ============================================================
// 3B. OPTIONAL MLX90614 NON-CONTACT IR THERMAL SENSOR (0x5A)
// ============================================================
bool initMlxDirect() {
  Wire.beginTransmission(0x5A);
  if (Wire.endTransmission() == 0) {
    Serial.println("[MLX90614] Medical Non-Contact IR Thermometer connected at 0x5A!");
    return true;
  }
  return false;
}

float readMlxObjectTemp() {
  Wire.beginTransmission(0x5A);
  Wire.write(0x07); // RAM Access register 0x07 = Object 1 Temperature
  if (Wire.endTransmission(false) != 0) return -999.0;

  if (Wire.requestFrom((uint8_t)0x5A, (uint8_t)3) == 3) {
    uint8_t lsb = Wire.read();
    uint8_t msb = Wire.read();
    uint8_t pec = Wire.read();
    uint16_t raw = (msb << 8) | lsb;
    if (raw == 0xFFFF) return -999.0;
    float tempC = (raw * 0.02) - 273.15;
    return tempC;
  }
  return -999.0;
}

// ============================================================
// 3C. MAX30102 PULSE + SpO2 (reads the chip FIFO directly)
// The SparkFun library stores only 4 samples between check() calls, so any loop
// stall over 40 ms (e.g. serving the web page) silently dropped pulse samples.
// The chip's own FIFO holds 32 samples = 320 ms of headroom.
// ============================================================
void resetPpg() {
  fingerDetected = false;
  heartRate = 0;
  spO2 = 0;
  validBeatsCount = 0;
  lastBeatSample = 0;
  spo2BufLen = 0;
  decimIr = decimRed = 0;
  decimCount = 0;
}

void processPpgSample(uint32_t red, uint32_t ir) {
  ppgSampleCount++;
  rawRed = red;
  rawIr = ir;

  // Contact threshold (typical finger presence gives IR > 30000)
  if (ir < 30000) {
    if (fingerDetected) resetPpg();
    return;
  }
  fingerDetected = true;

  // SparkFun checkForBeat squeezes samples into uint16 (max 65535) and only accepts pulses
  // 20-1000 units tall; raw 18-bit IR is ~200k with a finger, so it wrapped and never beat.
  // >>3 keeps any 18-bit value in range. Calibration knob: use >>2 if weak pulses are missed.
  if (checkForBeat(ir >> 3)) {
    uint32_t delta = (ppgSampleCount - lastBeatSample) * PPG_SAMPLE_MS;
    bool firstBeat = lastBeatSample == 0;
    lastBeatSample = ppgSampleCount;

    // Valid physiological pulse interval (380ms to 1500ms => 40 to 157 BPM)
    if (!firstBeat && delta > 380 && delta < 1500) {
      int instantBpm = (int)(60000.0 / delta);
      if (instantBpm >= 45 && instantBpm <= 165) {
        beatBuffer[beatIdx++] = instantBpm;
        beatIdx %= 4;
        validBeatsCount++;
        int count = min(validBeatsCount, 4);
        int sum = 0;
        for (int i = 0; i < count; i++) sum += beatBuffer[i];
        heartRate = sum / count;
      }
    }
  }

  // SpO2: average 4 samples (100 Hz -> 25 Hz), run Maxim's ratio-of-ratios every 1 s on a 4 s window
  decimIr += ir;
  decimRed += red;
  if (++decimCount < 4) return;
  spo2IrBuf[spo2BufLen] = decimIr / 4;
  spo2RedBuf[spo2BufLen] = decimRed / 4;
  spo2BufLen++;
  decimIr = decimRed = 0;
  decimCount = 0;
  if (spo2BufLen < 100) return;

  int32_t spo2Val, hrVal;
  int8_t spo2Ok, hrOk;
  maxim_heart_rate_and_oxygen_saturation(spo2IrBuf, 100, spo2RedBuf, &spo2Val, &spo2Ok, &hrVal, &hrOk);
  if (spo2Ok && spo2Val >= 70 && spo2Val <= 100) {
    spO2 = constrain(spo2Val + SPO2_OFFSET, 0, 100);
  }
  memmove(spo2IrBuf, spo2IrBuf + 25, 75 * sizeof(uint32_t));
  memmove(spo2RedBuf, spo2RedBuf + 25, 75 * sizeof(uint32_t));
  spo2BufLen = 75;
}

uint32_t readFifo18() {
  uint32_t v = (uint32_t)Wire.read() << 16;
  v |= (uint32_t)Wire.read() << 8;
  v |= Wire.read();
  return v & 0x3FFFF;
}

void configMax() {
  particleSensor.setup(0x1F, 4, 2, 400, 411, 4096);
  particleSensor.setPulseAmplitudeRed(0x32); // Increased optical LED power for instant peak acquisition
  particleSensor.setPulseAmplitudeIR(0x32);
  particleSensor.enableDIETEMPRDY();
}

uint8_t mpuReg(uint8_t reg) {
  Wire.beginTransmission(mpuAddr);
  Wire.write(reg);
  if (Wire.endTransmission(false) != 0) return 0xFF;
  if (Wire.requestFrom(mpuAddr, (uint8_t)1) != 1) return 0xFF;
  return Wire.read();
}

// A loose breadboard wire can power-cycle a sensor: it still ACKs on I2C but has lost its
// config (MAX30102 LED off -> 0 readings; MPU back to +/-2g -> accel reads 2x). Restore it.
unsigned long sensorReinits = 0;
void checkSensorConfig() {
  if (hasMax && particleSensor.readRegister8(MAX_ADDR, 0x0C) != 0x32) { configMax(); sensorReinits++; }
  if (hasMpu && (mpuReg(0x1C) != 0x08 || (mpuReg(0x6B) & 0x40))) { hasMpu = initMpuDirect(); sensorReinits++; }
}

void drainMaxFifo() {
  uint8_t wr = particleSensor.getWritePointer();
  uint8_t rd = particleSensor.getReadPointer();
  int n = (wr - rd) & 0x1F;
  // Pointers equal + overflow counter set = FIFO completely full (we fell behind > 320 ms)
  if (n == 0 && particleSensor.readRegister8(MAX_ADDR, 0x05) > 0) n = 32;

  while (n > 0) {
    int batch = min(n, 5);  // 5 samples x 6 bytes = 30 bytes, fits any Wire buffer
    Wire.beginTransmission(MAX_ADDR);
    Wire.write(0x07);       // FIFO_DATA register
    if (Wire.endTransmission(false) != 0) return;
    if (Wire.requestFrom((uint8_t)MAX_ADDR, (uint8_t)(batch * 6)) != batch * 6) return;
    for (int i = 0; i < batch; i++) {
      uint32_t red = readFifo18();  // ledMode 2: RED then IR
      uint32_t ir = readFifo18();
      processPpgSample(red, ir);
    }
    n -= batch;
  }
}

// ============================================================
// 4. ACTIVE THRESHOLD & ACTIONABLE REMEDY ENGINE
// ============================================================
struct ThresholdNotice {
  bool active;
  String title;
  String remedy;
  String color;
};

ThresholdNotice evaluateThresholds() {
  ThresholdNotice n;
  n.active = false;

  if (fingerDetected && heartRate > 100) {
    n.active = true;
    n.title = "🚨 HIGH HEART RATE (" + String(heartRate) + " BPM)";
    n.remedy = "Sit down comfortably, practice slow 4-7-8 breathing (inhale 4s, hold 7s, exhale 8s), sip cool water, and avoid physical strain.";
    n.color = "#ef4444";
  } else if (fingerDetected && heartRate >= 1 && heartRate <= 55) {
    n.active = true;
    n.title = "⚠ LOW HEART RATE (" + String(heartRate) + " BPM)";
    n.remedy = "Sit upright to prevent dizziness, flex your calf muscles, open and close your fists to stimulate blood flow, and drink warm water.";
    n.color = "#f59e0b";
  } else if (fingerDetected && spO2 >= 1 && spO2 <= 94) {
    n.active = true;
    n.title = "🚨 LOW BLOOD OXYGEN (" + String(spO2) + "% SpO2)";
    n.remedy = "Sit upright with shoulders relaxed, open windows for fresh airflow, practice pursed-lip exhalations, and unbutton tight collars.";
    n.color = "#ef4444";
  } else if (rawGsrUs > 3.8) {
    n.active = true;
    n.title = "⚡ HIGH STRESS GSR SURGE (" + String(rawGsrUs, 2) + " µS)";
    n.remedy = "Acute sympathetic stress detected. Take a 3-minute mindful pause, close your eyes, unclench your jaw, and perform 5 diaphragmatic breaths.";
    n.color = "#8b5cf6";
  } else if (gyroNet > 45.0) {
    n.active = true;
    n.title = "⚠ TREMOR & KINEMATIC SPIKE (" + String(gyroNet, 1) + " °/s)";
    n.remedy = "Rest your forearm on a stable flat table, release shoulder tension, perform gentle wrist and finger stretches, and check your posture.";
    n.color = "#f59e0b";
  } else if (bodyTemp > 37.5) {
    n.active = true;
    n.title = "🚨 HIGH TEMPERATURE ALERT (" + String(bodyTemp, 1) + " °C)";
    n.remedy = "Drink cool water immediately, rest in an air-conditioned room, loosen tight garments, and apply a cool damp cloth to your forehead.";
    n.color = "#ef4444";
  }

  return n;
}

// ============================================================
// 5. REST API ENDPOINTS
// ============================================================
void handleBiometrics() {
  String tempSourceStr = hasMlx ? "MLX90614 IR Non-Contact (0x5A)" : "No body temp sensor (add MLX90614)";

  String json = "{";
  json += "\"heartRate\":" + String(fingerDetected ? heartRate : 0) + ",";
  // SpO2 only once a pulse is locked: unsettled readings (e.g. 89%) would fire false low-oxygen alarms
  json += "\"spO2\":" + String(fingerDetected && heartRate > 0 ? spO2 : 0) + ",";
  json += "\"fingerDetected\":" + String(fingerDetected ? "true" : "false") + ",";
  json += "\"temperature\":" + String(bodyTemp, 1) + ",";
  json += "\"rawMpuTemp\":" + String(mpuTemp, 1) + ",";
  json += "\"tempSource\":\"" + tempSourceStr + "\",";
  json += "\"stressGsr\":" + String(rawGsrUs, 2) + ",";
  json += "\"gsrMv\":" + String(gsrMv, 0) + ",";
  json += "\"gsrCalibrated\":" + String(GSR_CALIBRATED ? "true" : "false") + ",";
  json += "\"batteryV\":" + String(batteryV, 2) + ",";
  // No BP sensor. Must send 0 explicitly: the app defaults a missing field to a fake 118/76.
  json += "\"bloodPressureSystolic\":0,";
  json += "\"bloodPressureDiastolic\":0,";
  json += "\"steps\":" + String(steps) + ",";
  json += "\"calories\":" + String((int)(steps * 0.04)) + ",";
  json += "\"stairs\":0,";
  json += "\"acceleration\":" + String(accelNet, 2) + ",";
  json += "\"gyro\":" + String(gyroNet, 1) + ",";
  json += "\"ax\":" + String(ax, 2) + ",";
  json += "\"ay\":" + String(ay, 2) + ",";
  json += "\"az\":" + String(az, 2) + ",";
  json += "\"gx\":" + String(gx, 1) + ",";
  json += "\"gy\":" + String(gy, 1) + ",";
  json += "\"gz\":" + String(gz, 1) + ",";
  json += "\"rawRed\":" + String(rawRed) + ",";
  json += "\"rawIr\":" + String(rawIr) + ",";
  json += "\"connected\":true,";
  json += "\"sensorStatus\":\"" + String(fingerDetected ? "ACTIVE (FINGER DETECTED)" : "ACTIVE (IDLE)") + "\",";
  json += "\"timestamp\":" + String(millis());
  json += "}";

  server.sendHeader("Access-Control-Allow-Origin", "*");
  server.send(200, "application/json", json);
}

void handleStatus() {
  String json = "{";
  json += "\"status\":\"ONLINE\",";
  json += "\"device\":\"ESP32 NeuroBand Wearable Hub\",";
  json += "\"hasMpu\":" + String(hasMpu ? "true" : "false") + ",";
  json += "\"hasMax\":" + String(hasMax ? "true" : "false") + ",";
  json += "\"hasMlx\":" + String(hasMlx ? "true" : "false") + ",";
  json += "\"fingerDetected\":" + String(fingerDetected ? "true" : "false") + ",";
  json += "\"heartRate\":" + String(heartRate) + ",";
  json += "\"spO2\":" + String(spO2) + ",";
  json += "\"gsr\":" + String(rawGsrUs, 2) + ",";
  json += "\"batteryV\":" + String(batteryV, 2) + ",";
  json += "\"sensorReinits\":" + String(sensorReinits) + ",";
  json += "\"freeHeap\":" + String(ESP.getFreeHeap()) + ",";      // battery soak test: should stay flat
  json += "\"minFreeHeap\":" + String(ESP.getMinFreeHeap()) + ",";
  json += "\"serverTime\":" + String(millis());
  json += "}";

  server.sendHeader("Access-Control-Allow-Origin", "*");
  server.send(200, "application/json", json);
}

void handleSimulate() {
  if (server.hasArg("resetSensors")) {  // power-glitch test: soft-reset sensors, checkSensorConfig() must restore them
    if (hasMpu) { Wire.beginTransmission(mpuAddr); Wire.write(0x6B); Wire.write(0x80); Wire.endTransmission(); }
    if (hasMax) particleSensor.softReset();
    server.send(200, "application/json", "{\"reset\":true}");
    return;
  }
  simActive = true;
  simStart = millis();
  if (server.hasArg("hr")) { heartRate = server.arg("hr").toInt(); fingerDetected = true; }
  if (server.hasArg("spo2")) spO2 = server.arg("spo2").toInt();
  if (server.hasArg("temp")) bodyTemp = server.arg("temp").toFloat();
  if (server.hasArg("rawMpuTemp")) mpuTemp = server.arg("rawMpuTemp").toFloat();
  if (server.hasArg("gyro")) gyroNet = server.arg("gyro").toFloat();
  if (server.hasArg("stress")) rawGsrUs = server.arg("stress").toFloat();

  server.sendHeader("Access-Control-Allow-Origin", "*");
  server.send(200, "application/json", "{\"success\":true}");
}

// Electrical check of one I2C line with Wire released. Sensor modules carry ~4.7k pull-ups,
// which beat the ESP32's ~45k internal pull-down.
String i2cLineState(int pin) {
  pinMode(pin, INPUT_PULLUP);   delay(5); bool upHigh = digitalRead(pin);
  pinMode(pin, INPUT_PULLDOWN); delay(5); bool downHigh = digitalRead(pin);
  if (!upHigh)  return "HELD LOW: shorted to GND, or a sensor on this wire has no power";
  if (downHigh) return "OK: powered sensor pull-up present";
  return "NOTHING CONNECTED: wire not reaching a powered sensor";
}

void handleI2cScan() {
  // ACK probe first (0 = found, 2 = no answer, 5 = bus timeout), then the line test
  Wire.beginTransmission(MAX_ADDR);
  int maxErr = Wire.endTransmission();
  Wire.end();
  String sda = i2cLineState(I2C_SDA_PIN);
  String scl = i2cLineState(I2C_SCL_PIN);

  // Short test: drive SCL low, see if SDA follows
  pinMode(I2C_SDA_PIN, INPUT_PULLUP);
  pinMode(I2C_SCL_PIN, OUTPUT); digitalWrite(I2C_SCL_PIN, LOW); delay(5);
  bool shorted = !digitalRead(I2C_SDA_PIN);
  pinMode(I2C_SCL_PIN, INPUT_PULLUP);

  // Swap test: does any sensor answer with SDA/SCL pins reversed?
  String swapped = "";
  Wire.begin(I2C_SCL_PIN, I2C_SDA_PIN);
  for (uint8_t a : {(uint8_t)0x57, (uint8_t)0x68, (uint8_t)0x69, (uint8_t)0x5A}) {
    Wire.beginTransmission(a);
    if (Wire.endTransmission() == 0) swapped += "0x" + String(a, HEX) + " ";
  }
  Wire.end();

  Wire.begin(I2C_SDA_PIN, I2C_SCL_PIN);
  Wire.setClock(100000);

  String json = "{\"sda_gpio21\":\"" + sda + "\",\"scl_gpio22\":\"" + scl + "\",\"max30102_code\":" + String(maxErr) +
                ",\"sda_scl_shorted\":" + String(shorted ? "true" : "false") +
                ",\"found_if_swapped\":\"" + swapped + "\",\"devices\":[";
  bool first = true;
  for (uint8_t addr = 1; addr < 127; addr++) {
    Wire.beginTransmission(addr);
    if (Wire.endTransmission() == 0) {
      if (!first) json += ",";
      String hexStr = String(addr, HEX);
      if (hexStr.length() == 1) hexStr = "0" + hexStr;
      hexStr.toUpperCase();
      json += "{\"address\":\"0x" + hexStr + "\",\"dec\":" + String(addr);
      if (addr == 0x57) json += ",\"name\":\"MAX30102 Optical Sensor\"";
      else if (addr == 0x68 || addr == 0x69) json += ",\"name\":\"MPU-6050 IMU\"";
      else if (addr == 0x5A) json += ",\"name\":\"MLX90614 IR Thermometer\"";
      else json += ",\"name\":\"Other I2C Device\"";
      json += "}";
      first = false;
    }
  }
  json += "]}";
  server.sendHeader("Access-Control-Allow-Origin", "*");
  server.send(200, "application/json", json);
}

void handleRoot() {
  ThresholdNotice alert = evaluateThresholds();

  String touchStatus = fingerDetected ? "<span style='color:#22c55e;font-weight:bold;'>DETECTED ● (LIVE PULSE)</span>" : "<span style='color:#94a3b8;'>NO FINGER ○ (TOUCH SENSOR TO MEASURE)</span>";
  String mpuStatus = hasMpu ? "<span style='color:#22c55e;font-weight:bold;'>CONNECTED (0x" + String(mpuAddr, HEX) + ")</span>" : "<span style='color:#f59e0b;font-weight:bold;'>WAITING / RECONNECTING (CHECK D21/D22)</span>";
  String mlxStatus = hasMlx ? "<span style='color:#22c55e;font-weight:bold;'>CONNECTED (0x5A)</span>" : "<span style='color:#94a3b8;'>NOT CONNECTED (Optional IR Temp)</span>";

  String alertBannerHtml = "";
  if (alert.active) {
    alertBannerHtml = "<div style='background:" + alert.color + "22;border:2px solid " + alert.color + ";border-radius:12px;padding:14px;margin-bottom:16px;'>";
    alertBannerHtml += "<div style='color:" + alert.color + ";font-weight:bold;font-size:14px;margin-bottom:6px;'>" + alert.title + "</div>";
    alertBannerHtml += "<div style='color:#f8fafc;font-size:12px;line-height:1.5;'><b>💡 ACTIONABLE REMEDY:</b> " + alert.remedy + "</div>";
    alertBannerHtml += "</div>";
  }

  String html = "<!DOCTYPE html><html><head><title>ESP32 NeuroBand Hub</title>";
  html += "<meta http-equiv='refresh' content='1'>";
  html += "<style>body{font-family:-apple-system,sans-serif;background:#0b1120;color:#f8fafc;padding:24px;}";
  html += ".card{background:#0f172a;border:1px solid #1e293b;border-radius:18px;max-width:580px;margin:0 auto;padding:24px;}";
  html += ".grid{display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-top:14px;}";
  html += ".stat{background:#1e293b;padding:12px;border-radius:10px;}";
  html += ".label{font-size:11px;color:#94a3b8;font-weight:bold;text-transform:uppercase;}";
  html += ".val{font-size:22px;color:#38bdf8;font-weight:bold;margin-top:4px;}";
  html += ".btn{display:inline-block;padding:6px 12px;border-radius:6px;background:#0284c7;color:#fff;text-decoration:none;font-size:11px;font-weight:bold;margin-right:6px;margin-top:6px;}</style></head><body>";
  html += "<div class='card'><h2>✦ ESP32 NeuroBand Hardware Hub</h2>";
  html += "<div style='color:#38bdf8;font-weight:bold;font-size:12px;margin-bottom:10px;'>● ONLINE & STREAMING (Port 5000)</div>";
  html += alertBannerHtml;
  html += "<div style='font-size:12px;margin-bottom:4px;'>Finger Contact: " + touchStatus + "</div>";
  html += "<div style='font-size:12px;margin-bottom:4px;'>MPU-6050 IMU: " + mpuStatus + "</div>";
  html += "<div style='font-size:12px;margin-bottom:12px;'>MLX90614 IR Temp: " + mlxStatus + "</div>";
  html += "<div class='grid'>";
  String hrDisplay = (fingerDetected && heartRate > 0) ? String(heartRate) + " BPM" : (fingerDetected ? "MEASURING..." : "--");
  String spo2Display = (fingerDetected && spO2 > 0) ? String(spO2) + "%" : (fingerDetected ? "MEASURING..." : "--");
  html += "<div class='stat'><div class='label'>Real Heart Rate</div><div class='val'>" + hrDisplay + "</div></div>";
  html += "<div class='stat'><div class='label'>Real SpO2</div><div class='val'>" + spo2Display + "</div></div>";
  String gsrDisplay = GSR_CALIBRATED ? String(rawGsrUs, 2) + " &micro;S" : "UNCALIBRATED";
  html += "<div class='stat'><div class='label'>Physical GSR (GPIO 34)</div><div class='val'>" + gsrDisplay + "</div></div>";
  html += "<div class='stat'><div class='label'>GSR mV (for calibration)</div><div class='val'>" + String(gsrMv, 0) + " mV</div></div>";
  String tempDisplay = bodyTemp > 0 ? String(bodyTemp, 1) + " &deg;C" : "-- (no MLX90614)";
  html += "<div class='stat'><div class='label'>Body Temperature</div><div class='val'>" + tempDisplay + "</div></div>";
  html += "<div class='stat'><div class='label'>Battery</div><div class='val'>" + String(batteryV, 2) + " V</div></div>";
  html += "<div class='stat'><div class='label'>MPU Die Temp (Silicon)</div><div class='val'>" + String(mpuTemp, 1) + " &deg;C</div></div>";
  html += "<div class='stat'><div class='label'>Real Steps (MPU)</div><div class='val'>" + String(steps) + "</div></div>";
  html += "<div class='stat'><div class='label'>Live Accel Net</div><div class='val'>" + String(accelNet, 2) + " g</div></div>";
  html += "<div class='stat'><div class='label'>Live Gyro (Tremor)</div><div class='val'>" + String(gyroNet, 1) + " &deg;/s</div></div>";
  html += "</div>";
  html += "<h4 style='color:#94a3b8;font-size:11px;margin-top:16px;margin-bottom:4px;'>TEST THRESHOLD ALERTS & REMEDIES:</h4>";
  html += "<div><a href='/api/simulate?hr=118' class='btn' style='background:#ef4444;'>Test Pulse 118 BPM</a>";
  html += "<a href='/api/simulate?stress=4.6' class='btn' style='background:#8b5cf6;'>Test Stress 4.6 uS</a>";
  html += "<a href='/api/simulate?temp=38.6' class='btn' style='background:#ef4444;'>Test Fever 38.6°C</a>";
  html += "<a href='/api/simulate?gyro=65' class='btn' style='background:#f59e0b;'>Test Tremor 65°/s</a></div>";
  html += "<p style='color:#64748b;font-size:11px;margin-top:18px;'>Raw IR: " + String(rawIr) + " | Raw RED: " + String(rawRed) + "</p></div></body></html>";

  server.send(200, "text/html", html);
}

// ============================================================
// 6. SETUP
// ============================================================
void setup() {
  Serial.begin(115200);
  delay(500);

  analogReadResolution(12);
  analogSetAttenuation(ADC_11db); // 0 to ~3.3V

  // Standard 100kHz I2C for maximum reliability on breadboard wires
  Wire.begin(I2C_SDA_PIN, I2C_SCL_PIN);
  Wire.setClock(100000);

  // 1. Initialize MAX30102
  if (particleSensor.begin(Wire, I2C_SPEED_STANDARD)) {
    hasMax = true;
    configMax();
    Serial.println("[MAX30102] Connected and initialized!");
  } else {
    Serial.println("[MAX30102] Not responding at 0x57. Check wiring.");
  }

  // 2. Initialize MPU-6050 directly
  hasMpu = initMpuDirect();

  // 3. Optional MLX90614 Non-Contact IR Thermal Sensor
  hasMlx = initMlxDirect();

  // 4. Connect to Wi-Fi
  WiFi.mode(WIFI_STA);
  WiFi.setSleep(false);  // modem sleep made phone-hotspot replies take 1.3-2.5 s
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("[WiFi] Connecting to: ");
  Serial.println(WIFI_SSID);

  int tries = 0;
  while (WiFi.status() != WL_CONNECTED && tries < 30) {
    delay(300);
    Serial.print(".");
    tries++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n[WiFi] CONNECTED!");
    Serial.print(">>> IP: http://");
    Serial.println(WiFi.localIP());
  }

  server.on("/api/biometrics", HTTP_GET, handleBiometrics);
  server.on("/api/status", HTTP_GET, handleStatus);
  server.on("/api/scan", HTTP_GET, handleI2cScan);
  server.on("/api/simulate", handleSimulate);
  server.on("/", HTTP_GET, handleRoot);

  server.begin();
}

// ============================================================
// 7. MAIN SENSOR ACQUISITION LOOP
// ============================================================
void loop() {
  server.handleClient();

  // ponytail: freezes ALL sensor updates during a test hold; per-field overrides if that ever matters
  if (simActive && millis() - simStart < SIM_HOLD_MS) return;
  if (simActive) {
    // Clear test values; a missing sensor would otherwise leave them stuck forever.
    // Connected sensors refill these on their next read.
    simActive = false;
    heartRate = 0; spO2 = 0; fingerDetected = false;
    gyroNet = 0; mpuTemp = 0;
  }

  unsigned long now = millis();

  // --- A. MAX30102: drain the chip FIFO every 20 ms (~2 samples per read) ---
  if (hasMax && now - lastFifoDrain >= 20) {
    lastFifoDrain = now;
    drainMaxFifo();
  }

  // --- B. PERIODIC SAMPLING (Every 40ms = 25Hz) ---
  static unsigned long lastTick = 0;
  if (now - lastTick >= 40) {
    lastTick = now;

    // --- REAL PHYSICAL GSR (GPIO 34) ---
    // analogReadMilliVolts uses the chip's factory ADC calibration; light smoothing for ADC noise
    gsrMv = 0.8 * gsrMv + 0.2 * analogReadMilliVolts(GSR_PIN);
    rawGsrUs = GSR_CALIBRATED ? gsrMvToUs(gsrMv, GSR_CAL_MV, GSR_CAL_US, GSR_CAL_N) : 0.0;

    // --- TEMPERATURE + BATTERY (Every 1000ms = 1.0 Second) ---
    if (now - lastTempRead >= 1000) {
      lastTempRead = now;
      batteryV = analogReadMilliVolts(BATT_PIN) * 2 / 1000.0;  // x2 for the 100k/100k divider

      // Only the MLX90614 measures body temperature. The MAX30102 "temperature" is its own
      // chip, not your skin, so it is not used. 0 = no reading.
      if (!hasMlx) {
        bodyTemp = 0.0;
      } else {
        float mlxT = readMlxObjectTemp();
        if (mlxT > 20.0 && mlxT < 45.0) bodyTemp = mlxT;
      }
    }

    // --- MPU-6050 KINEMATICS & PEDOMETER ---
    if (!hasMpu && (now - lastMpuCheck > 2500)) {
      lastMpuCheck = now;
      hasMpu = initMpuDirect();
    }

    static unsigned long lastConfigCheck = 0;
    if (now - lastConfigCheck > 2500) {
      lastConfigCheck = now;
      checkSensorConfig();
    }

    // Dynamic periodic check for MAX30102 hot-plugging
    if (!hasMax && (now - lastMaxCheck > 2500)) {
      lastMaxCheck = now;
      if (particleSensor.begin(Wire, I2C_SPEED_STANDARD)) {
        hasMax = true;
        configMax();
      }
    }

    // Dynamic periodic check for MLX90614 hot-plugging
    if (!hasMlx && (now - lastMlxCheck > 2500)) {
      lastMlxCheck = now;
      hasMlx = initMlxDirect();
    }

    if (hasMpu) {
      float tAx, tAy, tAz, tGx, tGy, tGz, tTemp;
      if (readMpuDirect(tAx, tAy, tAz, tGx, tGy, tGz, tTemp)) {
        ax = tAx; ay = tAy; az = tAz;
        gx = tGx; gy = tGy; gz = tGz;

        accelNet = sqrt(ax * ax + ay * ay + az * az);
        gyroNet = sqrt(gx * gx + gy * gy + gz * gz);
        mpuTemp = tTemp;

        // Accurate Stride Pedometer with Peak-Valley Hysteresis and Cadence Lockout
        // Sitting still on desk: accelNet stays ~0.98 - 1.05g -> steps lock at 0
        // Real walking step: impact peak > 1.34g followed by valley < 0.88g within 600ms
        if (pedState == STEP_ARMED) {
          if (accelNet > 1.34 && (now - lastStepMs > 350)) {
            pedState = STEP_PEAK_FOUND;
            peakDetectedMs = now;
          }
        } else if (pedState == STEP_PEAK_FOUND) {
          if (accelNet < 0.88) {
            if ((now - peakDetectedMs >= 60) && (now - peakDetectedMs <= 600)) {
              steps++;
              lastStepMs = now;
            }
            pedState = STEP_ARMED;
          } else if (now - peakDetectedMs > 600) {
            pedState = STEP_ARMED; // Timeout without step completion
          }
        }
      }
    }
  }

  // --- C. CONSOLE TELEMETRY LOG (Every 1.5s) ---
  if (now - lastConsolePrint >= 1500) {
    lastConsolePrint = now;
    String fState = fingerDetected ? "FINGER 🟢" : "NO FINGER ⚪";
    String mState = hasMpu ? "MPU 🟢" : "MPU 🔴";
    Serial.printf("[TELEMETRY] BPM: %3d | SpO2: %2d%% | GSR: %.2fuS (%.0f mV) | Body: %.1fC | MPU: %.1fC | Accel: %.2fg | Batt: %.2fV | Heap: %u | %s | %s\n",
                  heartRate, spO2, rawGsrUs, gsrMv, bodyTemp, mpuTemp, accelNet, batteryV, ESP.getFreeHeap(),
                  fState.c_str(), mState.c_str());
  }
}
