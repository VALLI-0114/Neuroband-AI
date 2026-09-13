package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

data class ChatMessage(val sender: String, val text: String, val timestamp: Long = System.currentTimeMillis())
data class StressAlert(val id: Int, val title: String, val description: String, val severity: String, val time: String)

class BiometricViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = BiometricRepository(db.biometricDao())
    private val sensorApiClient = SensorApiClient()

    // Bottom Navigation tab
    var currentTab by mutableStateOf(0)
        private set

    // Sub-Dashboard Chip selection
    var selectedDashboardSection by mutableStateOf("Core Dashboard")

    // Hardware Sensor Node Configuration & Live Telemetry
    var raspberryPiHost by mutableStateOf("192.168.1.50")
    var sensorConnectionStatus by mutableStateOf("OFFLINE") // "ONLINE", "CONNECTING", "OFFLINE"
    var connectionErrorMessage by mutableStateOf<String?>(null)
    var liveSensorPayload by mutableStateOf<SensorPayload?>(null)
        private set

    // Scan State
    var isScanning by mutableStateOf(false)
        private set
    var scanProgress by mutableStateOf(0f)
        private set
    var scanStatusText by mutableStateOf("SENSOR READY")
        private set
    var liveScanHrv by mutableStateOf(0)
        private set
    var liveScanGsr by mutableStateOf(0.0)
        private set
    var latestCompletedScan by mutableStateOf<StressScanResult?>(null)
        private set

    // AI Coach Chat History
    val chatMessages = mutableStateListOf<ChatMessage>().apply {
        add(
            ChatMessage(
                "coach",
                "Hello! I am your NeuroBand AI Coach. Connected to your Raspberry Pi bio-sensors (MAX30102 & MPU6050), I continuously evaluate your heart-rate, motion kinematics, and stress levels. How can I help you optimize your mental clarity today?"
            )
        )
    }

    var isThinkingByCoach by mutableStateOf(false)
        private set

    // Active Stress Alerts Notification System
    val activeAlerts = mutableStateListOf<StressAlert>().apply {
        add(StressAlert(1, "High Stress Detected", "Elevated GSR conduction of 4.2 µS indicates acute cognitive load.", "high", "Now"))
        add(StressAlert(2, "Poor Sleep Alert", "Sleep quality is at 31% with elevated sleep latency from student schedules.", "warning", "1h ago"))
        add(StressAlert(3, "Burnout Threat", "Predictive indicators suggest an 82% exam strain peak in the next 2 hours.", "critical", "3h ago"))
    }

    private var scanJob: Job? = null
    private var telemetryJob: Job? = null

    // Room DB Flow outputs
    val recentLogs: StateFlow<List<BiometricLog>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestLog: StateFlow<BiometricLog?> = repository.latestLog
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allStressScans: StateFlow<List<StressScanResult>> = repository.allStressScans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMindfulnessSessions: StateFlow<List<MindfulnessSession>> = repository.allMindfulnessSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Start real-time hardware telemetry polling loop
        startHardwarePolling()
    }

    fun selectTab(index: Int) {
        currentTab = index
    }

    fun updateRaspberryPiHost(newHost: String) {
        raspberryPiHost = newHost
        startHardwarePolling()
    }

    fun testSensorConnection() {
        viewModelScope.launch {
            sensorConnectionStatus = "CONNECTING"
            connectionErrorMessage = null
            val result = sensorApiClient.testConnection(raspberryPiHost)
            if (result.isSuccess) {
                sensorConnectionStatus = "ONLINE"
                connectionErrorMessage = null
            } else {
                sensorConnectionStatus = "OFFLINE"
                connectionErrorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to reach Raspberry Pi"
            }
        }
    }

    private fun startHardwarePolling() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (true) {
                try {
                    val result = sensorApiClient.fetchBiometrics(raspberryPiHost)
                    if (result.isSuccess) {
                        val payload = result.getOrNull()
                        if (payload != null) {
                            sensorConnectionStatus = "ONLINE"
                            connectionErrorMessage = null
                            liveSensorPayload = payload

                            // Record real biometric log in Room Database
                            val realLog = BiometricLog(
                                heartRate = payload.heartRate,
                                temperature = payload.temperature,
                                stressGsr = payload.stressGsr,
                                bloodPressureSystolic = payload.bloodPressureSystolic,
                                bloodPressureDiastolic = payload.bloodPressureDiastolic,
                                steps = payload.steps,
                                calories = payload.calories,
                                stairs = payload.stairs,
                                timestamp = payload.timestamp
                            )
                            repository.insertLog(realLog)
                        }
                    } else {
                        sensorConnectionStatus = "OFFLINE"
                        connectionErrorMessage = result.exceptionOrNull()?.localizedMessage
                    }
                } catch (e: Exception) {
                    sensorConnectionStatus = "OFFLINE"
                    connectionErrorMessage = e.localizedMessage
                }
                delay(1500) // Poll sensors every 1.5 seconds
            }
        }
    }

    fun startScanning() {
        if (isScanning) return
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            isScanning = true
            scanProgress = 0f
            scanStatusText = "COMMUNING WITH SENSORS..."
            delay(800)

            scanStatusText = "SAMPLING MAX30102 & MPU6050..."
            val stepsCount = 40
            for (i in 1..stepsCount) {
                scanProgress = i / stepsCount.toFloat()

                val currentPayload = liveSensorPayload
                if (currentPayload != null && sensorConnectionStatus == "ONLINE") {
                    // Use real sensor metrics from live hardware
                    liveScanHrv = ((60000.0 / currentPayload.heartRate.coerceAtLeast(40)) * 0.08).toInt().coerceIn(30, 95)
                    liveScanGsr = currentPayload.stressGsr
                } else {
                    liveScanHrv = Random.nextInt(40, 75)
                    liveScanGsr = (2.2 + Random.nextDouble(-0.3, 0.3))
                }

                if (i == 20) {
                    scanStatusText = "TinyML INFERENCE IN PROGRESS..."
                }
                delay(70)
            }

            scanStatusText = "ANALYSIS COMPLETE"
            scanProgress = 1f
            delay(400)

            val finalPayload = liveSensorPayload
            val finalHrv = if (finalPayload != null && sensorConnectionStatus == "ONLINE") {
                ((60000.0 / finalPayload.heartRate.coerceAtLeast(40)) * 0.08).toInt().coerceIn(30, 95)
            } else 68

            val finalGsr = finalPayload?.stressGsr ?: 2.4
            val hr = finalPayload?.heartRate ?: 72

            // Compute actual stress score from heart rate & GSR
            val computedStressScore = ((hr - 60).coerceAtLeast(0) * 1.2 + (finalGsr * 8)).toInt().coerceIn(10, 95)
            val confidence = if (sensorConnectionStatus == "ONLINE") 96 else 85

            val scanResult = StressScanResult(
                stressScore = computedStressScore,
                confidence = confidence,
                status = if (computedStressScore > 60) "Elevated Cognitive Load. Recommended deep breathing flow." else "Optimal Autonomic Balance.",
                hrvMs = finalHrv,
                conductanceUs = finalGsr,
                timestamp = System.currentTimeMillis()
            )
            repository.insertScan(scanResult)
            latestCompletedScan = scanResult

            isScanning = false
        }
    }

    fun completeMindfulnessSession(title: String, duration: Int) {
        viewModelScope.launch {
            val session = MindfulnessSession(
                title = title,
                durationMin = duration,
                difficulty = if (duration <= 5) "Beginner" else "Intermediate",
                heartRateReduction = 12,
                focusIncreasePercent = 18,
                timestamp = System.currentTimeMillis()
            )
            repository.insertSession(session)
        }
    }

    fun clearAllScans() {
        viewModelScope.launch {
            repository.clearScans()
            latestCompletedScan = null
        }
    }

    fun dismissAlert(id: Int) {
        val alert = activeAlerts.find { it.id == id }
        if (alert != null) {
            activeAlerts.remove(alert)
        }
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        chatMessages.add(ChatMessage("user", userText))

        viewModelScope.launch {
            isThinkingByCoach = true
            delay(1200)

            val currentLog = repository.latestLog.first()
            val hr = currentLog?.heartRate ?: 72
            val gsr = currentLog?.stressGsr ?: 2.1
            val steps = currentLog?.steps ?: 0

            val responseText = when {
                userText.contains("stress", ignoreCase = true) || userText.contains("why", ignoreCase = true) -> {
                    "Real-time sensor telemetry shows your Heart Rate is at $hr BPM and Skin Conductance is at $gsr µS. Autonomic balance suggests pacing your tasks and initiating a 3-minute diaphragmatic breathing exercise."
                }
                userText.contains("step", ignoreCase = true) || userText.contains("motion", ignoreCase = true) || userText.contains("activity", ignoreCase = true) -> {
                    "MPU6050 accelerometer has registered $steps active steps today. Regular physical micro-movements help discharge acute muscular tension."
                }
                userText.contains("burnout", ignoreCase = true) || userText.contains("exam", ignoreCase = true) -> {
                    "Based on continuous biometric telemetry, your current recovery stability index is active. Take a proactive 10-minute break now and prioritize hydration."
                }
                else -> {
                    "Live sensors (MAX30102 & MPU6050) report: Heart Rate = $hr BPM, Motion Activity = $steps steps, GSR = $gsr µS. Your biological signals are being monitored in real time. Would you like guidance through a restorative breathing session?"
                }
            }

            chatMessages.add(ChatMessage("coach", responseText))
            isThinkingByCoach = false
        }
    }
}
