# 🧠 NeuroBand AI™ - Smart Wearable & IoT Stress Intelligence System

An AI-Powered Wearable companion and Mobile Application that detects stress, explains autonomic triggers, predicts cognitive burnout, and guides users toward better mental wellbeing using live hardware sensors (**MAX30102 PPG** and **MPU6050 6-Axis IMU**) connected to a **Raspberry Pi** (or local simulation server).

---

## ✨ Key Features Added
* ☀️ **Clean Light & Dark Theme Toggle:** Option in Settings to switch between a bright, clean light theme and deep cyber navy dark theme.
* 📳 **Threshold Vibration Guidance & Actionable Remedies:** When pulse, SpO2, tremors, temperature, or stress GSR cross safe thresholds (e.g. pulse > 100 BPM or < 55 BPM), the band/phone vibrates with haptic patterns, shows a prominent warning banner with an increasing/decreasing indicator, and displays an actionable clinical remedy (e.g. *"Drink a glass of cool water, sit down comfortably, and take slow 4-7-8 deep breaths"*).
* 🧪 **One-Tap Vibration & Remedy Test:** A test button in Settings allows instant verification of haptic buzzing and remedy display.
* 📊 **Clinical 4-Level Sensor Matrix:** Complete threshold levels (Critical Low, Normal/Safe, Warning, Critical High) documented in [SYSTEM_EXECUTION_AND_THRESHOLD_MATRIX.md](file:///c:/Users/Likhith%20Kumar/Downloads/Neuroband-AI/SYSTEM_EXECUTION_AND_THRESHOLD_MATRIX.md).

---

## 📁 Repository Structure

```
Neuroband-AI/
├── app/                                       # Android Jetpack Compose Application Source
│   └── src/main/java/com/example/
│       ├── data/                              # Room DB, DAOs & SensorApiService (REST Client)
│       └── ui/
│           ├── screens/                       # HomeScreen, AnalyticsScreen, BreathingScreen, SettingsScreen
│           ├── components/                    # GlassmorphicCard, VaporwaveBackground
│           ├── theme/                         # Theme.kt (Light & Dark Schemes), Color.kt
│           └── BiometricViewModel.kt          # State, Threshold Monitoring, & Vibration Logic
├── neuroband_server/                          # Raspberry Pi IoT Sensor Bridge Server
│   ├── sensor_server.py                       # Python Flask I2C Acquisition Server (Port 5000)
│   └── README.md
├── SYSTEM_EXECUTION_AND_THRESHOLD_MATRIX.md   # 4-Level Thresholds, Outputs, Warnings & Remedies
├── SENSOR_SETUP_GUIDE.md                      # Hardware Wiring & Pinout Guide
└── README.md
```

---

## ⚡ Quick Start: Running the System

### 1. Start the Sensor Server (`sensor_server.py`)
* **On Raspberry Pi (Hardware Mode):**
  ```bash
  cd ~/Neuroband-AI/neuroband_server
  python sensor_server.py
  ```
* **On PC / Laptop (Testing / Simulation Mode):**
  ```powershell
  cd neuroband_server
  python sensor_server.py
  ```
* Open your browser at `http://localhost:5000` to view the live dashboard and test simulation buttons (`/api/simulate?hr=118`).

### 2. Run the Android Mobile App
1. Open the project in **Android Studio**.
2. Connect your Android phone or launch an emulator.
3. Click the green **▶ Run ('app')** button.
4. Navigate to the **Settings** tab (4th icon):
   * Set the IP (e.g. `10.0.2.2` for emulator on same PC, or `192.168.x.x` for phone over Wi-Fi).
   * Tap **SAVE HOST** and **TEST LINK**.
   * Toggle **Clean Light Theme** / **Dark Cyber Theme**.
   * Tap **TEST VIBRATION & REMEDY WARNING** to test haptic feedback!

---

📖 *For the full 4-level clinical threshold reference matrix, outputs, vibration patterns, and remedies, see [SYSTEM_EXECUTION_AND_THRESHOLD_MATRIX.md](file:///c:/Users/Likhith%20Kumar/Downloads/Neuroband-AI/SYSTEM_EXECUTION_AND_THRESHOLD_MATRIX.md).*
