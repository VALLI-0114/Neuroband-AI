# 🧠 NEUROBAND AI™ - HARDWARE & IOT SYSTEM MANUAL
### Real-Time Wearable Stress Intelligence Platform
*Integrating Raspberry Pi, MAX30102 PPG Pulse Oximeter, MPU6050 6-DOF IMU, and Jetpack Compose Android App*

---

## 📋 Table of Contents
1. [Executive Summary & System Overview](#1-executive-summary--system-overview)
2. [Hardware Components & Pinout Connections](#2-hardware-components--pinout-connections)
3. [Repository File Map & Code Locations](#3-repository-file-map--code-locations)
4. [Raspberry Pi Setup & Execution Guide](#4-raspberry-pi-setup--execution-guide)
5. [Android Application Installation & Setup](#5-android-application-installation--setup)
6. [App Features & Real-Time Telemetry Guide](#6-app-features--real-time-telemetry-guide)
7. [Troubleshooting & Maintenance](#7-troubleshooting--maintenance)

---

## 1. Executive Summary & System Overview

**NeuroBand AI™** is an IoT and AI-driven wearable stress intelligence platform designed to replace simulated/mock data with live physiological and kinematic telemetry.

```
+-------------------------------------------------------------------------------+
|                       RASPBERRY PI HARDWARE NODE (LAN)                        |
|                                                                               |
|   +-------------------+    I2C Bus 1      +-------------------------------+   |
|   |  MAX30102 Sensor  | ----------------> |                               |   |
|   |  (Pulse PPG / IR) |   (Pins 3 & 5)    |     Python Flask Server       |   |
|   +-------------------+                   |    (neuroband_server/         |   |
|                                           |     sensor_server.py)         |   |
|   +-------------------+    I2C Bus 1      |                               |   |
|   |  MPU6050 Sensor   | ----------------> |    Port: 5000                 |   |
|   |  (6-DOF Motion)   |   (Pins 3 & 5)    +---------------+---------------+   |
+---+-------------------+-----------------------------------|-------------------+
                                                            |
                                        Local Network HTTP REST API
                                        (http://192.168.148.52:5000/api/biometrics)
                                                            |
                                                            v
+-------------------------------------------------------------------------------+
|                     NEUROBAND AI ANDROID JETPACK COMPOSE APP                  |
|                                                                               |
|   [SensorApiService] ---> [BiometricViewModel] ---> [Room SQLite Database]    |
|                                                            |                  |
|       +-------------------+-------------------+------------+                  |
|       v                   v                   v            v                  |
|   [Home Screen]   [Analytics Screen]  [Breathing Hub] [Control Center]        |
|   Live Pulse BPM  Real-Time Vectors   TinyML Scan     Hardware IP Link        |
|   Step Counter    Circadian Heatmap   Vagus Flow      Live Telemetry Stream   |
+-------------------------------------------------------------------------------+
```

---

## 2. Hardware Components & Pinout Connections

### Hardware Required:
1. **Raspberry Pi** (3B / 4B / 5 / Zero 2W) with Raspberry Pi OS.
2. **MAX30102 PPG Pulse Oximeter & Heart-Rate Sensor** (I2C address `0x57`).
3. **MPU-6050 6-Axis Gyroscope & Accelerometer Module** (I2C address `0x68`).
4. Jumper Wires & Breadboard.

### Pinout Wiring Diagram:
Both sensors communicate simultaneously over the Raspberry Pi's hardware **I2C-1** interface:

| Sensor Pin | MAX30102 Connection | MPU6050 Connection | Raspberry Pi Physical Header Pin | Function |
| :--- | :--- | :--- | :--- | :--- |
| **VCC** | Connect to 3.3V | Connect to 3.3V / 5V | **Pin 1** (3.3V Power) or **Pin 2** (5V) | Power supply |
| **GND** | Connect to GND | Connect to GND | **Pin 6** or **Pin 9** (Ground) | Ground rail |
| **SDA** | Connect to SDA | Connect to SDA | **Pin 3** (GPIO 2 / I2C SDA) | Serial Data |
| **SCL** | Connect to SCL | Connect to SCL | **Pin 5** (GPIO 3 / I2C SCL) | Serial Clock |

> [!NOTE]
> I2C is a shared bus. Both MAX30102 (`0x57`) and MPU6050 (`0x68`) can be connected directly to the exact same SDA (Pin 3) and SCL (Pin 5) pins without any address conflicts.

---

## 3. Repository File Map & Code Locations

| File / Folder Path | Description |
| :--- | :--- |
| **[`neuroband_server/sensor_server.py`](file:///c:/Users/Likhith%20Kumar/Downloads/Neuroband-AI/neuroband_server/sensor_server.py)** | **Core Raspberry Pi Server Script.** Reads I2C sensors in a background thread and serves JSON REST API endpoints on port `5000`. |
| **[`neuroband_server/README.md`](file:///c:/Users/Likhith%20Kumar/Downloads/Neuroband-AI/neuroband_server/README.md)** | Quick Raspberry Pi command cheatsheet. |
| **[`app/src/main/java/com/example/data/SensorApiService.kt`](file:///c:/Users/Likhith%20Kumar/Downloads/Neuroband-AI/app/src/main/java/com/example/data/SensorApiService.kt)** | **Android REST API Client.** Fetches biometrics and manages timeouts/parsing using OkHttp & Moshi. |
| **[`app/src/main/java/com/example/ui/BiometricViewModel.kt`](file:///c:/Users/Likhith%20Kumar/Downloads/Neuroband-AI/app/src/main/java/com/example/ui/BiometricViewModel.kt)** | **State & Database Bridge.** Continuously polls sensor data, stores real readings in Room DB, and updates app UI. |
| **[`app/src/main/java/com/example/ui/screens/SettingsScreen.kt`](file:///c:/Users/Likhith%20Kumar/Downloads/Neuroband-AI/app/src/main/java/com/example/ui/screens/SettingsScreen.kt)** | **Control Center.** IP address configuration, connection status (`ONLINE`/`OFFLINE`), and live telemetry stream. |
| **[`app/src/main/java/com/example/ui/screens/HomeScreen.kt`](file:///c:/Users/Likhith%20Kumar/Downloads/Neuroband-AI/app/src/main/java/com/example/ui/screens/HomeScreen.kt)** | **Home Dashboard.** Dynamic hero pulse ring and live hardware stats (Steps, Accel, GSR). |
| **[`app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt`](file:///c:/Users/Likhith%20Kumar/Downloads/Neuroband-AI/app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt)** | **Stress Intelligence.** Live KPI vector cards, circadian heatmap, and predictive recovery charts. |
| **[`app/build/outputs/apk/debug/app-debug.apk`](file:///c:/Users/Likhith%20Kumar/Downloads/Neuroband-AI/app/build/outputs/apk/debug/app-debug.apk)** | **Ready-to-Install Android APK** for your mobile phone. |

---

## 4. Raspberry Pi Setup & Execution Guide

### Step 4.1: Connect via SSH
Open Command Prompt (CMD) or Terminal on your PC:
```bash
ssh jayasuma@192.168.148.52
```

### Step 4.2: Enable I2C Interface on the Pi (One-time setup)
```bash
sudo raspi-config
```
1. Navigate to **Interface Options** → **I2C**.
2. Select **Yes** to enable the I2C interface.
3. Select **Finish** and reboot if prompted (`sudo reboot`).

### Step 4.3: Install Python Dependencies
```bash
mkdir -p ~/neuroband
cd ~/neuroband
pip install flask flask-cors smbus2
```

### Step 4.4: Verify I2C Sensor Detection
```bash
i2cdetect -y 1
```
*You should see `57` (MAX30102) and `68` (MPU6050) appear in the table.*

### Step 4.5: Copy or Create `sensor_server.py`
Create the file on your Pi:
```bash
nano sensor_server.py
```
*(Paste the full contents of [`neuroband_server/sensor_server.py`](file:///c:/Users/Likhith%20Kumar/Downloads/Neuroband-AI/neuroband_server/sensor_server.py), then press `Ctrl+O` → `Enter` → `Ctrl+X`)*

### Step 4.6: Start the Server
```bash
python sensor_server.py
```
*The server will start listening on `http://0.0.0.0:5000`:*
```
=======================================================
   NEUROBAND AI - RASPBERRY PI SENSOR SERVER
   Listening on http://0.0.0.0:5000
   API Endpoint: http://192.168.148.52:5000/api/biometrics
=======================================================
```

---

## 5. Android Application Installation & Setup

### Method A: Direct Install via APK on Mobile Phone
1. Transfer **[`app-debug.apk`](file:///c:/Users/Likhith%20Kumar/Downloads/Neuroband-AI/app/build/outputs/apk/debug/app-debug.apk)** to your Android mobile phone (via USB, Google Drive, WhatsApp, or email).
2. Tap the file on your phone and choose **Install**.
3. Ensure your phone is connected to the same Wi-Fi network as your Raspberry Pi.

### Method B: Run from Android Studio
1. Open this project in **Android Studio**.
2. Select your connected phone or emulator from the device dropdown at the top.
3. Click the green **▶ Run ('app')** button (`Shift + F10`).

---

## 6. App Features & Real-Time Telemetry Guide

### 1. Connecting the App to the Pi:
1. In the NeuroBand app, tap the **Settings / Control Center** tab (4th bottom icon).
2. In the **Raspberry Pi IP Address** input, ensure `192.168.148.52` is entered.
3. Tap **"Connect"**. The status badge will change to **ONLINE (Green)** 🟢.

### 2. Live Home Screen:
* **Finger Off Sensor:** Displays `READY` / `PLACE FINGER`.
* **Finger on MAX30102:** Displays your dynamic pulse in real time (e.g. `74 BPM`).
* **Mini Telemetry Cards:** Displays live **Steps (MPU)**, **Motion Accel (g)**, and **Stress GSR (µS)**.

### 3. Analytics & Diagnostics:
* Live KPI cards automatically adjust as heart rate and motion fluctuate.
* **TinyML Scan (Breathing Tab):** Tap "Start Scan" to run real-time stress diagnosis on your active biometric telemetry.

---

## 7. Troubleshooting & Maintenance

| Issue | Cause | Solution |
| :--- | :--- | :--- |
| **BPM showing > 100 or double counting** | Pressing too hard or moving finger | Rest your index finger gently over the MAX30102 sensor glass without pressing hard. Hold still for 3–5 seconds. |
| **App shows OFFLINE / Connection Error** | IP mismatch or Pi server not running | 1. Ensure `python sensor_server.py` is running on the Pi.<br>2. Check `hostname -I` on the Pi.<br>3. Verify phone/emulator and Pi are on the same Wi-Fi. |
| **I2C `i2cdetect` shows empty grid** | Loose jumper wire | Check 3.3V, GND, SDA (Pin 3), and SCL (Pin 5) wiring connections. |
| **Raw IR stays below 10,000** | No finger on sensor | Place finger directly on the sensor LED; Raw IR will jump to `50,000`–`150,000+`. |

---
*NeuroBand AI™ — Intelligent Biometric Engineering System.*
