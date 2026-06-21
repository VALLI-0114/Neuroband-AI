package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
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

    // Bottom Navigation tab
    var currentTab by mutableStateOf(0)
        private set

    // Sub-Dashboard Chip selection (for the top sub-navigation menu requested by the user)
    var selectedDashboardSection by mutableStateOf("Core Dashboard")

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
        add(ChatMessage("coach", "Hello! I am your NeuroBand AI Coach. Connected to your ESP32 bio-sensors, I have analyzed your heart-rate, GSR, skin temp, and motion activity. How can I help you optimize your recovery today?"))
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
        // Pre-populate database with baseline data if empty so the interface is richly populated
        viewModelScope.launch {
            repository.latestLog.first()?.let {
                // Already has data, perfect
            } ?: run {
                generateBaselineData()
            }
        }

        // Ticker to periodically update current bio logs to make dashboard interactive
        viewModelScope.launch {
            while (true) {
                delay(4000)
                if (!isScanning) {
                    updateCurrentRealTimeMetrics()
                }
            }
        }
    }

    fun selectTab(index: Int) {
        currentTab = index
    }

    private suspend fun generateBaselineData() {
        // Insert historical biometric logs
        val baseTime = System.currentTimeMillis()
        for (i in 5 downTo 1) {
            val log = BiometricLog(
                heartRate = Random.nextInt(68, 76),
                temperature = 36.5 + Random.nextDouble(-0.3, 0.3),
                stressGsr = 2.0 + Random.nextDouble(-0.5, 0.5),
                bloodPressureSystolic = Random.nextInt(115, 122),
                bloodPressureDiastolic = Random.nextInt(74, 79),
                steps = 8000 - i * 1200,
                calories = 380 - i * 50,
                stairs = 10 - i,
                timestamp = baseTime - i * 3600 * 1000
            )
            repository.insertLog(log)
        }

        // Add some default baseline mindfulness sessions
        repository.insertSession(
            MindfulnessSession(
                title = "Deep Sleep Recovery Flow",
                durationMin = 15,
                difficulty = "Beginner",
                heartRateReduction = 14,
                focusIncreasePercent = 18,
                timestamp = baseTime - 12 * 3600 * 1000
            )
        )
        repository.insertSession(
            MindfulnessSession(
                title = "Pre-Exam Focus Breathing",
                durationMin = 8,
                difficulty = "Intermediate",
                heartRateReduction = 10,
                focusIncreasePercent = 22,
                timestamp = baseTime - 4 * 3600 * 1000
            )
        )
    }

    private suspend fun updateCurrentRealTimeMetrics() {
        val current = repository.recentLogs.first().firstOrNull()
        val nextLog = BiometricLog(
            heartRate = if (Random.nextBoolean()) Random.nextInt(70, 75) else current?.heartRate ?: 72,
            temperature = 36.4 + Random.nextDouble(0.1, 0.5),
            stressGsr = (2.2 + Random.nextDouble(-0.4, 0.4)).coerceAtLeast(0.5),
            bloodPressureSystolic = Random.nextInt(117, 120),
            bloodPressureDiastolic = Random.nextInt(75, 78),
            steps = (current?.steps ?: 8432) + Random.nextInt(5, 25),
            calories = (current?.calories ?: 420) + Random.nextInt(1, 3),
            stairs = current?.stairs ?: 12,
            timestamp = System.currentTimeMillis()
        )
        repository.insertLog(nextLog)
    }

    fun startScanning() {
        if (isScanning) return
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            isScanning = true
            scanProgress = 0f
            scanStatusText = "INITIATING SENSORS..."
            delay(1000)
            
            scanStatusText = "COLLECTING BIOMETRICS..."
            val stepsCount = 50
            for (i in 1..stepsCount) {
                scanProgress = i / stepsCount.toFloat()
                
                // Pulsing biometric noise
                liveScanHrv = Random.nextInt(35, 85)
                liveScanGsr = (2.5 + Random.nextDouble(0.0, 4.0))
                
                if (i == 25) {
                    scanStatusText = "TinyML INFERENCE IN PROGRESS..."
                }
                delay(60)
            }

            scanStatusText = "ANALYSIS COMPLETE"
            scanProgress = 1f
            delay(500)

            val finalHrv = Random.nextInt(65, 75)
            val finalGsr = 3.2
            val stressScore = Random.nextInt(10, 20) // optimal low stress range
            val confidence = Random.nextInt(88, 96)

            val scanResult = StressScanResult(
                stressScore = stressScore,
                confidence = confidence,
                status = "Optimal. Continue with your deep breathing exercises.",
                hrvMs = finalHrv,
                conductanceUs = finalGsr,
                timestamp = System.currentTimeMillis()
            )
            repository.insertScan(scanResult)
            latestCompletedScan = scanResult

            // Also append a biometric log entry corresponding to the scan setup
            repository.insertLog(
                BiometricLog(
                    heartRate = 72,
                    temperature = 36.6,
                    stressGsr = finalGsr,
                    bloodPressureSystolic = 118,
                    bloodPressureDiastolic = 75,
                    steps = 8432,
                    calories = 422,
                    stairs = 12,
                    timestamp = System.currentTimeMillis()
                )
            )

            isScanning = false
        }
    }

    fun completeMindfulnessSession(title: String, duration: Int) {
        viewModelScope.launch {
            val reductionLst = listOf(8, 10, 12, 14, 15)
            val focusIncreaseLst = listOf(14, 16, 18, 20, 22)
            
            val session = MindfulnessSession(
                title = title,
                durationMin = duration,
                difficulty = if (duration <= 5) "Beginner" else "Intermediate",
                heartRateReduction = reductionLst.random(),
                focusIncreasePercent = focusIncreaseLst.random(),
                timestamp = System.currentTimeMillis()
            )
            repository.insertSession(session)

            // Trigger log entry showing reduced heart rate and optimal GSR
            val current = repository.recentLogs.first().firstOrNull()
            repository.insertLog(
                BiometricLog(
                    heartRate = Random.nextInt(60, 66), // noticeably lower
                    temperature = 36.5,
                    stressGsr = 1.6, // low Gsr indicates calm
                    bloodPressureSystolic = 112, // reduced blood pressure
                    bloodPressureDiastolic = 70,
                    steps = current?.steps ?: 8432,
                    calories = (current?.calories ?: 420) + duration,
                    stairs = current?.stairs ?: 12,
                    timestamp = System.currentTimeMillis()
                )
            )
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
            delay(1500) // Simulated cognitive thinking state
            
            val responseText = when {
                userText.contains("stress", ignoreCase = true) || userText.contains("why", ignoreCase = true) -> {
                    "Your smart band telemetry shows your electrodermal activity (GSR) spiked to 4.2 µS, and your heart rate variability (HRV) has dropped to 38ms. This indicates acute cognitive fatigue, likely due to a 70% Sleep Deficit combined with academic pressure. Try to perform our 4-7-8 Breathing flow to soothe your nervous system."
                }
                userText.contains("burnout", ignoreCase = true) || userText.contains("exam", ignoreCase = true) -> {
                    "Your current Burnout Risk Score is 43% (Moderate), but predicted academic workload indicates a potential spike to 82% during exam slots in 2 hours. Take a proactive 15-minute break now and prioritize hydration."
                }
                userText.contains("sleep", ignoreCase = true) -> {
                    "Last night's sleep quality was recorded at 31% with several micro-arousals. To recover, we suggest avoiding blue screens after 10 PM and practicing the Pre-Sleep Wind Down breathing series."
                }
                else -> {
                    "Based on real-time ESP32 MAX30102 PPG and GSR sensor feeds, your autonomic nervous system is on elevated alert. I recommend deep inhalation cycles followed by relaxing slow exhales. Would you like me to guide you through a session?"
                }
            }
            
            chatMessages.add(ChatMessage("coach", responseText))
            isThinkingByCoach = false
        }
    }
}
