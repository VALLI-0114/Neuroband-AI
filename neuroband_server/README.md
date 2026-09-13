# NeuroBand AI - Raspberry Pi Sensor Server

This folder contains the Python server for streaming real-time hardware telemetry from **MAX30102** and **MPU6050** to the NeuroBand AI Android App over your local network.

---

## 🚀 Quick Setup on Raspberry Pi

### 1. Transfer or create folder on Raspberry Pi
In your SSH terminal on the Raspberry Pi:
```bash
mkdir -p ~/neuroband
cd ~/neuroband
```

### 2. Install Required Python Libraries
```bash
pip3 install flask flask-cors smbus2
```

### 3. Copy `sensor_server.py`
You can either upload this file or create it:
```bash
nano sensor_server.py
```
*(Paste the contents and save with `Ctrl+O` -> `Enter`, exit with `Ctrl+X`)*

### 4. Enable I2C Interface (If not already enabled)
```bash
sudo raspi-config
# Select Interface Options -> I2C -> Enable -> Finish
```

Test that sensors are detected:
```bash
i2cdetect -y 1
```
*(You should see `0x57` for MAX30102 and `0x68` for MPU6050)*

### 5. Run the Server
```bash
python3 sensor_server.py
```

### 6. Find Your Raspberry Pi's IP
```bash
hostname -I
```
*(e.g. `192.168.1.50`)*

Enter `192.168.1.50` (or `http://10.0.2.2:5000` if using the Android Studio emulator on the same PC) into the Android App's **Settings -> Raspberry Pi Sensor Node** configuration.
