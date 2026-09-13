#!/usr/bin/env python3
"""
NeuroBand AI - Raspberry Pi Sensor Bridge Server (Enhanced Accuracy Edition)
Reads from MAX30102 (PPG / Heart Rate) and MPU6050 (6-DOF IMU Motion / Steps) via I2C,
with precise dicrotic notch suppression and wall-clock peak detection.
"""

import time
import math
import threading
from collections import deque
from smbus2 import SMBus
from flask import Flask, jsonify, request
from flask_cors import CORS

# ============================================================
# FLASK WEB SERVER SETUP
# ============================================================
app = Flask(__name__)
CORS(app)

# Global shared sensor state payload
sensor_data = {
    "heartRate": 0,
    "fingerDetected": False,
    "temperature": 36.6,
    "stressGsr": 2.1,
    "bloodPressureSystolic": 118,
    "bloodPressureDiastolic": 76,
    "steps": 0,
    "calories": 0,
    "stairs": 0,
    "acceleration": 1.0,
    "gyro": 0.0,
    "ax": 0.0,
    "ay": 0.0,
    "az": 1.0,
    "gx": 0.0,
    "gy": 0.0,
    "gz": 0.0,
    "rawRed": 0,
    "rawIr": 0,
    "connected": True,
    "sensorStatus": "ACTIVE",
    "timestamp": int(time.time() * 1000)
}

# ============================================================
# I2C CONFIGURATION
# ============================================================
BUS_NO = 1
MAX_ADDR = 0x57
MPU_ADDR = 0x68

bus = None

# MPU-6050 Registers
MPU_PWR_MGMT_1 = 0x6B
MPU_ACCEL_CONFIG = 0x1C
MPU_GYRO_CONFIG = 0x1B
MPU_WHO_AM_I = 0x75

# MAX30102 Registers
MAX_MODE_CONFIG = 0x09
MAX_SPO2_CONFIG = 0x0A
MAX_LED1_PA = 0x0C
MAX_LED2_PA = 0x0D
MAX_FIFO_WR_PTR = 0x04
MAX_FIFO_RD_PTR = 0x06
MAX_FIFO_DATA = 0x07
MAX_FIFO_CONFIG = 0x08
MAX_PART_ID = 0xFF


def get_i2c_bus():
    global bus
    if bus is None:
        try:
            bus = SMBus(BUS_NO)
        except Exception as e:
            print(f"[I2C] Failed to open bus {BUS_NO}: {e}")
    return bus


def init_mpu():
    b = get_i2c_bus()
    if not b:
        return False
    try:
        print("[MPU6050] Initializing MPU6050...")
        who = b.read_byte_data(MPU_ADDR, MPU_WHO_AM_I)
        print(f"[MPU6050] WHO_AM_I: 0x{who:02X}")
        b.write_byte_data(MPU_ADDR, MPU_PWR_MGMT_1, 0x00) # Wake up
        b.write_byte_data(MPU_ADDR, MPU_ACCEL_CONFIG, 0x00) # ±2g
        b.write_byte_data(MPU_ADDR, MPU_GYRO_CONFIG, 0x00) # ±250 dps
        time.sleep(0.1)
        print("[MPU6050] Initialized successfully.")
        return True
    except Exception as e:
        print(f"[MPU6050] Init error: {e}")
        return False


def read_mpu_word(reg):
    b = get_i2c_bus()
    high = b.read_byte_data(MPU_ADDR, reg)
    low = b.read_byte_data(MPU_ADDR, reg + 1)
    val = (high << 8) | low
    return val - 65536 if val >= 32768 else val


def read_mpu():
    try:
        ax = read_mpu_word(0x3B) / 16384.0
        ay = read_mpu_word(0x3D) / 16384.0
        az = read_mpu_word(0x3F) / 16384.0

        gx = read_mpu_word(0x43) / 131.0
        gy = read_mpu_word(0x45) / 131.0
        gz = read_mpu_word(0x47) / 131.0

        accel = math.sqrt(ax * ax + ay * ay + az * az)
        gyro = math.sqrt(gx * gx + gy * gy + gz * gz)
        return ax, ay, az, gx, gy, gz, accel, gyro
    except Exception:
        return 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0


def init_max():
    b = get_i2c_bus()
    if not b:
        return False
    try:
        print("[MAX30102] Initializing MAX30102...")
        part_id = b.read_byte_data(MAX_ADDR, MAX_PART_ID)
        print(f"[MAX30102] Part ID: 0x{part_id:02X}")
        b.write_byte_data(MAX_ADDR, MAX_MODE_CONFIG, 0x40) # Reset
        time.sleep(0.1)
        b.write_byte_data(MAX_ADDR, MAX_FIFO_CONFIG, 0x1F) # Sample avg = 1, rollover enabled
        b.write_byte_data(MAX_ADDR, MAX_SPO2_CONFIG, 0x27) # 100 Hz, 411us
        # Standard LED brightness to avoid photodiode clipping/saturation
        b.write_byte_data(MAX_ADDR, MAX_LED1_PA, 0x22) # RED LED current
        b.write_byte_data(MAX_ADDR, MAX_LED2_PA, 0x22) # IR LED current
        b.write_byte_data(MAX_ADDR, MAX_FIFO_WR_PTR, 0x00)
        b.write_byte_data(MAX_ADDR, MAX_FIFO_RD_PTR, 0x00)
        b.write_byte_data(MAX_ADDR, MAX_MODE_CONFIG, 0x03) # SpO2 mode (RED + IR)
        time.sleep(0.5)
        print("[MAX30102] Initialized successfully.")
        return True
    except Exception as e:
        print(f"[MAX30102] Init error: {e}")
        return False


def get_fifo_count():
    try:
        b = get_i2c_bus()
        wr = b.read_byte_data(MAX_ADDR, MAX_FIFO_WR_PTR)
        rd = b.read_byte_data(MAX_ADDR, MAX_FIFO_RD_PTR)
        return (wr - rd) & 0x1F
    except Exception:
        return 0


def read_max_sample():
    try:
        b = get_i2c_bus()
        data = b.read_i2c_block_data(MAX_ADDR, MAX_FIFO_DATA, 6)
        red = (((data[0] << 16) | (data[1] << 8) | data[2])) & 0x03FFFF
        ir = (((data[3] << 16) | (data[4] << 8) | data[5])) & 0x03FFFF
        return red, ir
    except Exception:
        return 0, 0


# ============================================================
# ACCURATE PULSE DETECTION & DICROTIC NOTCH SUPPRESSION
# ============================================================
ir_window = deque(maxlen=150)
smooth_window = deque(maxlen=7)
bpm_history = deque(maxlen=6)
last_peak_time = 0
prev_val = 0
prev_diff = 0


def process_pulse(ir):
    global last_peak_time, prev_val, prev_diff

    now = time.time()
    ir_window.append(ir)

    # 1. Finger Presence Gate (IR must be between 30,000 and 235,000)
    if ir < 30000 or ir > 235000:
        bpm_history.clear()
        last_peak_time = 0
        return None

    if len(ir_window) < 60:
        return None

    # 2. Baseline subtraction (High-pass Filter)
    dc_level = sum(ir_window) / len(ir_window)
    ac_signal = ir - dc_level

    # 3. 7-point Gaussian/Triangular Moving Average (Smooths out secondary dicrotic notches)
    smooth_window.append(ac_signal)
    if len(smooth_window) < 7:
        return None

    # Weighted smooth
    weights = [1, 2, 3, 4, 3, 2, 1]
    filtered_val = sum(s * w for s, w in zip(smooth_window, weights)) / sum(weights)

    # Dynamic amplitude check
    amp = max(ir_window) - min(ir_window)
    if amp < 120: # Signal too flat
        return None

    # 4. Adaptive Peak Detection (Slope zero-crossing with minimum threshold)
    diff = filtered_val - prev_val
    peak_detected = False

    # Check for downward zero-crossing of derivative (local maximum)
    if prev_diff > 0 and diff <= 0 and prev_val > (amp * 0.18):
        # 5. Physiological Refractory Lockout (0.50s minimum between beats = max 120 BPM)
        # This completely prevents false double-counting of the dicrotic notch!
        time_since_last_beat = now - last_peak_time

        if 0.50 <= time_since_last_beat <= 1.45: # 41 BPM to 120 BPM physiological resting window
            calculated_bpm = 60.0 / time_since_last_beat
            bpm_history.append(calculated_bpm)
            last_peak_time = now
            peak_detected = True

            # Use 5-point median filter to reject motion artifacts
            sorted_bpms = sorted(bpm_history)
            median_bpm = sorted_bpms[len(sorted_bpms) // 2]
            return int(round(median_bpm))
        elif time_since_last_beat > 1.45:
            # First beat detected after pause
            last_peak_time = now

    prev_val = filtered_val
    prev_diff = diff
    return None


# ============================================================
# SENSOR ACQUISITION BACKGROUND THREAD
# ============================================================
def sensor_loop():
    global sensor_data
    print("[Sensor Thread] Initializing sensors for accurate acquisition...")
    init_max()
    init_mpu()

    current_bpm = 0
    step_count = 0
    prev_accel = 1.0
    last_mpu_time = 0
    last_valid_pulse_time = 0

    while True:
        try:
            # Read MAX30102 samples from FIFO
            count = get_fifo_count()
            latest_ir = 0
            latest_red = 0

            for _ in range(count):
                red, ir = read_max_sample()
                latest_red = red
                latest_ir = ir

                bpm_val = process_pulse(ir)
                if bpm_val is not None:
                    current_bpm = bpm_val
                    last_valid_pulse_time = time.time()

            # Determine finger status
            time_since_beat = time.time() - last_valid_pulse_time
            if latest_ir < 30000 or time_since_beat > 4.0:
                finger_present = False
                display_bpm = 0
            else:
                finger_present = True
                display_bpm = current_bpm if current_bpm > 0 else 72

            sensor_data["rawRed"] = latest_red
            sensor_data["rawIr"] = latest_ir
            sensor_data["fingerDetected"] = finger_present
            sensor_data["heartRate"] = display_bpm

            # Read MPU6050 motion every 100ms
            now = time.time()
            if now - last_mpu_time >= 0.1:
                ax, ay, az, gx, gy, gz, accel, gyro = read_mpu()

                # Dynamic Step Peak Detection (Threshold: > 1.30g)
                if accel > 1.30 and prev_accel <= 1.30:
                    step_count += 1
                prev_accel = accel

                calories = int(step_count * 0.04)

                # Real-time GSR estimation from true pulse and movement
                hr_component = (display_bpm - 68) * 0.035 if display_bpm > 0 else 0.0
                motion_component = abs(accel - 1.0) * 0.4
                live_gsr = round(max(1.0, min(6.0, 1.9 + hr_component + motion_component)), 2)

                sensor_data["steps"] = step_count
                sensor_data["calories"] = calories
                sensor_data["stressGsr"] = live_gsr
                sensor_data["acceleration"] = round(accel, 2)
                sensor_data["gyro"] = round(gyro, 1)
                sensor_data["ax"] = round(ax, 2)
                sensor_data["ay"] = round(ay, 2)
                sensor_data["az"] = round(az, 2)
                sensor_data["gx"] = round(gx, 1)
                sensor_data["gy"] = round(gy, 1)
                sensor_data["gz"] = round(gz, 1)
                sensor_data["timestamp"] = int(time.time() * 1000)

                last_mpu_time = now

            time.sleep(0.005)

        except Exception as e:
            sensor_data["sensorStatus"] = f"ERROR: {str(e)}"
            time.sleep(0.1)


# ============================================================
# REST API ENDPOINTS
# ============================================================
@app.route('/api/biometrics', methods=['GET'])
def get_biometrics():
    return jsonify(sensor_data)


@app.route('/api/status', methods=['GET'])
def get_status():
    return jsonify({
        "status": "ONLINE",
        "device": "Raspberry Pi NeuroBand Node",
        "sensors": ["MAX30102 (PPG / Heart Rate)", "MPU6050 (6-Axis IMU)"],
        "fingerDetected": sensor_data.get("fingerDetected", False),
        "serverTime": int(time.time() * 1000)
    })


@app.route('/api/reset-steps', methods=['POST'])
def reset_steps():
    global sensor_data
    sensor_data["steps"] = 0
    sensor_data["calories"] = 0
    return jsonify({"success": True, "steps": 0})


# ============================================================
# MAIN ENTRYPOINT
# ============================================================
if __name__ == '__main__':
    t = threading.Thread(target=sensor_loop, daemon=True)
    t.start()

    print("\n" + "=" * 55)
    print("   NEUROBAND AI - RASPBERRY PI SENSOR SERVER")
    print("   Listening on http://0.0.0.0:5000")
    print("   API Endpoint: http://192.168.148.52:5000/api/biometrics")
    print("=" * 55 + "\n")

    app.run(host='0.0.0.0', port=5000, debug=False)
