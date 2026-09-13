# 📡 NeuroBand AI - Raspberry Pi & Sensors Setup Guide

This guide gives you the exact step-by-step commands to connect your **MAX30102** and **MPU6050** sensors on your Raspberry Pi and stream live biometrics to the **NeuroBand AI Android App**.

---

## 🧭 System Overview

* **Raspberry Pi IP Address:** `192.168.148.52`
* **Port:** `5000`
* **API Endpoint:** `http://192.168.148.52:5000/api/biometrics`
* **Working Directory on Pi:** `~/neuroband`

```
  +-------------------------------------------------------------------------+
  |                        Raspberry Pi (192.168.148.52)                    |
  |                                                                         |
  |   +-------------+       I2C (SDA/SCL)      +------------------------+   |
  |   |  MAX30102   | -----------------------> |                        |   |
  |   | (Heart/IR)  |                          |  Python Flask Server   |   |
  |   +-------------+                          |  (sensor_server.py)    |   |
  |                                            |                        |   |
  |   +-------------+       I2C (SDA/SCL)      |  Port: 5000            |   |
  |   |   MPU6050   | -----------------------> |                        |   |
  |   | (Motion/IMU)|                          +-----------+------------+   |
  +---+-------------+--------------------------------------|----------------+
                                                           |
                                      Local LAN Network (Wi-Fi / Ethernet)
                                                           |
                                                           v
  +-------------------------------------------------------------------------+
  |                     NeuroBand AI Android Application                    |
  |   (Running on Android Emulator or Physical Android Phone)               |
  |                                                                         |
  |   * Live Heart Rate BPM & Pulse Waveform                                |
  |   * Live MPU6050 Accelerometer (g) & Gyroscope (dps)                    |
  |   * Real-time Step Counter & Cognitive Stress Level Calculation         |
  +-------------------------------------------------------------------------+
```

---

## 🔌 1. Hardware Pin Connections (I2C)

Both the **MAX30102** and **MPU6050** communicate over the Raspberry Pi's **I2C-1** bus (Pins 3 & 5):

| Sensor Pin | Raspberry Pi Header Pin | Function |
| :--- | :--- | :--- |
| **VCC** (Both sensors) | **Pin 1** (3.3V) or **Pin 2** (5V) | Power |
| **GND** (Both sensors) | **Pin 6** or **Pin 9** | Ground |
| **SDA** (Both sensors) | **Pin 3** (GPIO 2 / I2C SDA) | I2C Serial Data |
| **SCL** (Both sensors) | **Pin 5** (GPIO 3 / I2C SCL) | I2C Serial Clock |

> [!TIP]
> Both sensors can share the same SDA and SCL pins because they have different I2C addresses (`0x57` for MAX30102, `0x68` for MPU6050).

---

## ⚡ 2. Raspberry Pi Terminal Commands

In your SSH terminal on the Pi (`(venv) jayasuma@raspberrypi:~/neuroband $`):

### Step 2.1: Install Required Packages
```bash
pip install flask flask-cors smbus2
```

### Step 2.2: Verify Sensor Detection
Make sure both sensors are detected on the I2C bus:
```bash
i2cdetect -y 1
```
*Expected Output:* You should see `57` (MAX30102) and `68` (MPU6050) in the grid.

### Step 2.3: Create / Paste the Server Script
Open `sensor_server.py` in nano:
```bash
nano sensor_server.py
```
*(Copy and paste the code from [neuroband_server/sensor_server.py](file:///c:/Users/Likhith%20Kumar/Downloads/Neuroband-AI/neuroband_server/sensor_server.py), then press `Ctrl+O` -> `Enter` to save, and `Ctrl+X` to exit.)*

### Step 2.4: Start the Sensor Server
```bash
python sensor_server.py
```
*You will see:*
```
=======================================================
   NEUROBAND AI - RASPBERRY PI SENSOR SERVER
   Listening on http://0.0.0.0:5000
   API Endpoint: http://192.168.148.52:5000/api/biometrics
=======================================================
```

---

## 📱 3. How to Run & Connect the Android App

### Step 3.1: Open Android Studio
1. In Android Studio, look at the top toolbar.
2. Select your device from the dropdown:
   * **Medium Phone API 36.0** (or your preferred Emulator)
   * OR your **Physical Android Phone** (via USB).
3. Click the green **▶ Run ('app')** button (or press `Shift + F10`).

---

### Step 3.2: Connect App to Your Raspberry Pi
1. When the NeuroBand AI app opens on the phone/emulator:
2. Tap the **Console / Settings** icon in the bottom navigation bar (the 4th icon on the right).
3. Under **Raspberry Pi Sensor Node**:
   * If running on **Laptop Android Emulator**: Enter `192.168.148.52` (or click `10.0.2.2`).
   * If running on a **Physical Android Phone**: Enter `192.168.148.52`.
4. Tap the **"Connect"** button.
5. The status badge will switch to **ONLINE (Green)**! 🟢

---

## 📊 4. Verifying Real-Time Sensor Data

1. **Settings / Control Center Tab**:
   * View live **Heart Rate BPM**, **Steps**, **Stress GSR**, **Accel Net (g)**, **Accel XYZ**, and **Raw IR Counts**.
2. **Home Tab**:
   * The central neural stress ring will display **LIVE BPM** and indicate **MAX30102 SENSOR**.
3. **Flow / Analytics Tab**:
   * Real-time vectors (Heart Rate, Stress GSR, Skin Temp, Motion Steps) update live as you move the MPU6050 or place your finger on the MAX30102!
4. **Scan / TinyML Tab**:
   * Tap "Start Scan" to run real-time TinyML inference on your active biometrics.
