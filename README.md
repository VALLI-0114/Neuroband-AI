# 🧠 NeuroBand AI™ - Smart Wearable & IoT Stress Intelligence System

An AI-Powered Wearable companion and Mobile Application that detects stress, explains autonomic triggers, predicts cognitive burnout, and guides users toward better mental wellbeing using live hardware sensors (**MAX30102 PPG** and **MPU6050 6-Axis IMU**) connected to a **Raspberry Pi**.

---

## 📁 Repository Structure

```
Neuroband-AI/
├── app/                      # Android Jetpack Compose Application Source
│   └── src/main/java/com/example/
│       ├── data/             # Room DB, DAOs & SensorApiService (REST Client)
│       └── ui/
│           ├── screens/      # HomeScreen, AnalyticsScreen, BreathingScreen, SettingsScreen
│           └── BiometricViewModel.kt
├── neuroband_server/         # Raspberry Pi IoT Sensor Bridge Server
│   ├── sensor_server.py      # Python Flask I2C Acquisition Server (Port 5000)
│   └── README.md
├── SENSOR_SETUP_GUIDE.md     # Complete Hardware & Command Guide
└── README.md
```

---

## ⚡ Quick Start: Running the Entire System

### 1. Raspberry Pi Sensor Server
1. SSH into your Raspberry Pi:
   ```bash
   ssh jayasuma@192.168.148.52
   ```
2. Navigate to your project folder on the Pi and start the sensor server:
   ```bash
   cd ~/neuroband
   python sensor_server.py
   ```
   *The server will start listening on `http://192.168.148.52:5000`.*

---

### 2. Android App
1. Open this repository in **Android Studio**.
2. Select your device (Emulator or Connected Android Phone) at the top.
3. Click the green **▶ Run ('app')** button.
4. In the app, switch to the **Settings / Control Center** tab (4th icon).
5. Ensure the IP is set to `192.168.148.52` and tap **"Connect"**.
6. The status will display **ONLINE** 🟢 and stream live biometric telemetry!

---

📖 *For pinout wiring and troubleshooting, see the [SENSOR_SETUP_GUIDE.md](file:///c:/Users/Likhith%20Kumar/Downloads/Neuroband-AI/SENSOR_SETUP_GUIDE.md).*
