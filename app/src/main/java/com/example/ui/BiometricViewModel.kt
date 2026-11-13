package com.example.ui

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.MainActivity
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.reflect.KMutableProperty0

data class ThresholdAlert(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val parameterName: String,
    val currentValue: String,
    val threshold: String,
    val isIncreasing: Boolean, // true if value went ABOVE max, false if BELOW min
    val severity: String, // "warning", "critical"
    val remedy: String,
    val timestamp: Long = System.currentTimeMillis()
)

class BiometricViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val CHANNEL_ID = "neuroband_vital_alerts_channel"
        const val NOTIFICATION_ID = 1001
    }

    private val db = AppDatabase.getDatabase(application)
    private val repository = BiometricRepository(db.biometricDao())
    private val sensorApiClient = SensorApiClient()

    // Light / Dark Theme Mode (defaults to clean light theme as requested)
    var isDarkTheme by mutableStateOf(false)

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
    }

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

    // Tactile Vibration & Alert Settings
    var vibrationGuidanceEnabled by mutableStateOf(true)
    var ttsVoiceEnabled by mutableStateOf(true)
    var notificationFeedsEnabled by mutableStateOf(true)
    var soundEffectsEnabled by mutableStateOf(true)

    // Temperature Monitoring Mode: "BODY" (Calibrated Human Temp) vs "MPU_RAW" (Raw Silicon Junction)
    var tempMonitoringMode by mutableStateOf("BODY")
    var maxMpuTempThreshold by mutableStateOf(45.0) // °C for raw MPU6050 silicon die

    // Configurable Safety Threshold Limits
    var maxTempThreshold by mutableStateOf(37.5) // °C
    var minTempThreshold by mutableStateOf(35.5) // °C
    var maxHeartRateThreshold by mutableStateOf(100) // BPM
    var minHeartRateThreshold by mutableStateOf(55) // BPM
    var minSpo2Threshold by mutableStateOf(94) // %
    var maxGsrThreshold by mutableStateOf(3.8) // µS
    var maxGyroThreshold by mutableStateOf(45.0) // °/s

    // Real-time Active Warning Alert with Remedy
    var activeThresholdAlert by mutableStateOf<ThresholdAlert?>(null)
    private var lastVibrationTime: Long = 0L
    private var alarmRingtone: Ringtone? = null
    private var lastSpokenAlertTitle: String? = null
    // Tracks parameter names of dismissed alerts to show warnings ONLY ONCE per breach event
    private val dismissedAlerts = mutableSetOf<String>()

    // Text-To-Speech (TTS) Engine
    private var tts: TextToSpeech? = null
    var isTtsReady by mutableStateOf(false)
    var isTtsSpeaking by mutableStateOf(false)

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

    // Settings saved across app restarts. Add a field here to persist it.
    private val prefs = application.getSharedPreferences("neuroband_settings", Context.MODE_PRIVATE)
    private val persistedSettings: List<Pair<String, KMutableProperty0<out Any>>> = listOf(
        "isDarkTheme" to ::isDarkTheme,
        "raspberryPiHost" to ::raspberryPiHost,
        "vibrationGuidanceEnabled" to ::vibrationGuidanceEnabled,
        "ttsVoiceEnabled" to ::ttsVoiceEnabled,
        "notificationFeedsEnabled" to ::notificationFeedsEnabled,
        "soundEffectsEnabled" to ::soundEffectsEnabled,
        "tempMonitoringMode" to ::tempMonitoringMode,
        "maxMpuTempThreshold" to ::maxMpuTempThreshold,
        "maxTempThreshold" to ::maxTempThreshold,
        "minTempThreshold" to ::minTempThreshold,
        "maxHeartRateThreshold" to ::maxHeartRateThreshold,
        "minHeartRateThreshold" to ::minHeartRateThreshold,
        "minSpo2Threshold" to ::minSpo2Threshold,
        "maxGsrThreshold" to ::maxGsrThreshold,
        "maxGyroThreshold" to ::maxGyroThreshold
    )

    @Suppress("UNCHECKED_CAST")
    private fun loadSettings() {
        persistedSettings.forEach { (key, ref) ->
            if (!prefs.contains(key)) return@forEach
            val p = ref as KMutableProperty0<Any>
            try {
                when (p.get()) {
                    is Boolean -> p.set(prefs.getBoolean(key, false))
                    is Int -> p.set(prefs.getInt(key, 0))
                    is Double -> prefs.getString(key, null)?.toDoubleOrNull()?.let { p.set(it) }
                    is String -> prefs.getString(key, null)?.let { p.set(it) }
                }
            } catch (_: ClassCastException) {
                // Stored with an older type; keep the default.
            }
        }
    }

    private fun saveSettingsOnChange() {
        viewModelScope.launch {
            snapshotFlow { persistedSettings.map { it.second.get() } }
                .drop(1) // the just-loaded values
                .collect { values ->
                    val edit = prefs.edit()
                    persistedSettings.zip(values).forEach { (entry, v) ->
                        when (v) {
                            is Boolean -> edit.putBoolean(entry.first, v)
                            is Int -> edit.putInt(entry.first, v)
                            else -> edit.putString(entry.first, v.toString())
                        }
                    }
                    edit.apply()
                }
        }
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
        loadSettings() // before polling starts, so the saved Raspberry Pi host is used
        saveSettingsOnChange()
        viewModelScope.launch { chatMessages.addAll(repository.chatHistory()) }
        initTextToSpeech()
        createNotificationChannel()
        // Start real-time hardware telemetry polling loop
        startHardwarePolling()
    }

    override fun onCleared() {
        super.onCleared()
        stopAlarm()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
    }

    private fun initTextToSpeech() {
        tts = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true
                    tts?.setSpeechRate(0.95f)
                    tts?.setPitch(1.0f)
                }
            }
        }
    }

    fun speakRemedy(text: String) {
        if (!ttsVoiceEnabled) return
        try {
            if (isTtsReady && tts != null) {
                isTtsSpeaking = true
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "neuroband_tts_${System.currentTimeMillis()}")
            }
        } catch (_: Exception) {}
    }

    fun stopTTS() {
        try {
            tts?.stop()
            isTtsSpeaking = false
        } catch (_: Exception) {}
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NeuroBand Vital & Threshold Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts and medical remedies when biometric thresholds are exceeded."
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
            }
            val manager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun postSystemNotification(alert: ThresholdAlert) {
        if (!notificationFeedsEnabled) return
        try {
            val context = getApplication<Application>()
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("🚨 " + alert.title)
                .setContentText("${alert.currentValue} • Tap to see what to do")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .setBigContentTitle("🚨 " + alert.title)
                        .bigText(
                            "Reading: ${alert.currentValue}\n${alert.threshold}\n\n💡 WHAT TO DO NOW:\n" +
                                remedySteps(alert.remedy).mapIndexed { i, s -> "${i + 1}. $s" }.joinToString("\n")
                        )
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify(NOTIFICATION_ID, builder.build())
        } catch (_: Exception) {}
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

                            // Evaluate vital & motion thresholds for real-time vibration + warnings + remedies
                            evaluateThresholds(payload)

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

    /**
     * Checks sensor values against safe threshold bands.
     * If a threshold is violated, triggers device vibration, displays warning on screen,
     * speaks remedy via TTS, and dispatches a system notification.
     */
    fun evaluateThresholds(payload: SensorPayload) {
        val fingerPresent = payload.fingerDetected || payload.heartRate > 0

        // Auto-reset dismissed state once vitals recover back to safe normal bounds
        if (tempMonitoringMode == "BODY" && payload.temperature in minTempThreshold..maxTempThreshold) {
            dismissedAlerts.remove("Body Temperature")
        }
        if (tempMonitoringMode == "MPU_RAW" && payload.rawMpuTemp <= maxMpuTempThreshold) {
            dismissedAlerts.remove("MPU6050 Silicon Die")
        }
        if (payload.heartRate in (minHeartRateThreshold + 1)..(maxHeartRateThreshold - 1)) {
            dismissedAlerts.remove("Heart Rate (BPM)")
        }
        if (payload.spO2 > minSpo2Threshold) {
            dismissedAlerts.remove("SpO2 Blood Oxygen")
        }
        if (payload.gyro <= maxGyroThreshold) {
            dismissedAlerts.remove("Tremor / Kinematics")
        }
        if (payload.stressGsr <= maxGsrThreshold) {
            dismissedAlerts.remove("Autonomic Stress GSR")
        }

        val newAlert: ThresholdAlert? = when {
            // MPU6050 Raw Hardware Temperature Mode
            tempMonitoringMode == "MPU_RAW" && payload.rawMpuTemp > maxMpuTempThreshold -> {
                val isCritical = payload.rawMpuTemp >= (maxMpuTempThreshold + 5.0)
                ThresholdAlert(
                    title = "High MPU6050 Heat (${payload.rawMpuTemp} °C)",
                    parameterName = "MPU6050 Silicon Die",
                    currentValue = "${payload.rawMpuTemp} °C",
                    threshold = "Max Limit: $maxMpuTempThreshold °C",
                    isIncreasing = true,
                    severity = if (isCritical) "critical" else "warning",
                    remedy = "Hardware silicon temperature exceeded safe limit. Ensure the Raspberry Pi and MPU-6050 have adequate airflow, disconnect from direct heat/sunlight, and verify 3.3V power rails."
                )
            }
            // Calibrated Body Temperature High (exceeds maxTempThreshold)
            tempMonitoringMode == "BODY" && payload.temperature > maxTempThreshold -> {
                val isCritical = payload.temperature >= 38.5
                ThresholdAlert(
                    title = "High Temperature (${payload.temperature} °C)",
                    parameterName = "Body Temperature",
                    currentValue = "${payload.temperature} °C",
                    threshold = "Max Limit: $maxTempThreshold °C",
                    isIncreasing = true,
                    severity = if (isCritical) "critical" else "warning",
                    remedy = "Drink cool water immediately, move to a well-ventilated or air-conditioned room, loosen tight garments, and apply a cool damp cloth to your forehead, neck, and wrists."
                )
            }
            // Calibrated Body Temperature Low (below minTempThreshold)
            // temperature 0 = no body temp sensor on the band, not hypothermia
            tempMonitoringMode == "BODY" && payload.temperature > 0 && payload.temperature < minTempThreshold -> {
                ThresholdAlert(
                    title = "Low Temperature (${payload.temperature} °C)",
                    parameterName = "Body Temperature",
                    currentValue = "${payload.temperature} °C",
                    threshold = "Min Limit: $minTempThreshold °C",
                    isIncreasing = false,
                    severity = "warning",
                    remedy = "Put on warm clothing or a thermal wrap, move away from cold air drafts, drink a warm beverage, and gently massage your hands and arms."
                )
            }
            // Heart Rate High (Tachycardia / exceeds maxHeartRateThreshold)
            fingerPresent && payload.heartRate > maxHeartRateThreshold -> {
                val isCritical = payload.heartRate >= (maxHeartRateThreshold + 25)
                ThresholdAlert(
                    title = "High Heart Rate (${payload.heartRate} BPM)",
                    parameterName = "Heart Rate (BPM)",
                    currentValue = "${payload.heartRate} BPM",
                    threshold = "Max Limit: $maxHeartRateThreshold BPM",
                    isIncreasing = true,
                    severity = if (isCritical) "critical" else "warning",
                    remedy = "Sit down comfortably, practice slow 4-7-8 breathing (inhale 4s, hold 7s, exhale 8s), drink a glass of cool water, and avoid caffeine."
                )
            }
            // Heart Rate Low (Bradycardia / below minHeartRateThreshold)
            fingerPresent && payload.heartRate in 1..minHeartRateThreshold -> {
                ThresholdAlert(
                    title = "Low Heart Rate (${payload.heartRate} BPM)",
                    parameterName = "Heart Rate (BPM)",
                    currentValue = "${payload.heartRate} BPM",
                    threshold = "Min Limit: $minHeartRateThreshold BPM",
                    isIncreasing = false,
                    severity = "warning",
                    remedy = "Sit upright to prevent dizziness, flex your calf muscles and open/close your fists to stimulate blood return, and sip warm water."
                )
            }
            // Blood Oxygen SpO2 Low (Hypoxia / below minSpo2Threshold)
            fingerPresent && payload.spO2 in 1..minSpo2Threshold -> {
                val isCritical = payload.spO2 < (minSpo2Threshold - 5)
                ThresholdAlert(
                    title = "Low Oxygen (${payload.spO2}% SpO2)",
                    parameterName = "SpO2 Blood Oxygen",
                    currentValue = "${payload.spO2}%",
                    threshold = "Min Limit: $minSpo2Threshold%",
                    isIncreasing = false,
                    severity = if (isCritical) "critical" else "warning",
                    remedy = "Sit upright with shoulders relaxed, open a window for fresh airflow, practice pursed-lip exhalations, and unbutton tight collars."
                )
            }
            // Motion Tremor / Gyroscope Spikes (exceeds maxGyroThreshold)
            payload.gyro > maxGyroThreshold -> {
                val isCritical = payload.gyro > (maxGyroThreshold + 30.0)
                ThresholdAlert(
                    title = "Tremor & Motion Spike (${payload.gyro} °/s)",
                    parameterName = "Tremor / Kinematics",
                    currentValue = "${payload.gyro} °/s",
                    threshold = "Max Limit: $maxGyroThreshold °/s",
                    isIncreasing = true,
                    severity = if (isCritical) "critical" else "warning",
                    remedy = "Rest your forearm on a stable flat table, release shoulder tension, perform gentle finger stretches, and verify your medication schedule."
                )
            }
            // Electrodermal Activity (GSR) / Acute Stress Spike (exceeds maxGsrThreshold)
            payload.stressGsr > maxGsrThreshold -> {
                ThresholdAlert(
                    title = "High Stress GSR Surge (${payload.stressGsr} µS)",
                    parameterName = "Autonomic Stress GSR",
                    currentValue = "${payload.stressGsr} µS",
                    threshold = "Max Limit: $maxGsrThreshold µS",
                    isIncreasing = true,
                    severity = "warning",
                    remedy = "Take a 3-minute mindful pause, close your eyes, unclench your jaw, take 5 deep diaphragmatic breaths, and take a screen break."
                )
            }
            else -> null
        }

        if (newAlert != null) {
            // Warn only once: skip re-populating if the user already dismissed this breach
            if (!dismissedAlerts.contains(newAlert.parameterName)) {
                val isDifferentAlert = activeThresholdAlert?.title != newAlert.title
                activeThresholdAlert = newAlert
                triggerVibration(newAlert.severity)
                startAlarm()
                if (isDifferentAlert || lastSpokenAlertTitle != newAlert.title) {
                    lastSpokenAlertTitle = newAlert.title
                    postSystemNotification(newAlert)
                    speakRemedy("Warning! ${newAlert.title}. Exceeds threshold limit. Actionable remedy: ${newAlert.remedy}")
                }
            }
        } else {
            // When all readings return to safe normal bounds
            if (activeThresholdAlert != null) {
                activeThresholdAlert = null
            }
            stopAlarm()
        }
    }

    fun updateMaxTemp(newLimit: Double) {
        maxTempThreshold = Math.round(newLimit * 10.0) / 10.0
        recheckCurrentVitals()
    }

    fun updateMinTemp(newLimit: Double) {
        minTempThreshold = Math.round(newLimit * 10.0) / 10.0
        recheckCurrentVitals()
    }

    fun updateMaxMpuTemp(newLimit: Double) {
        maxMpuTempThreshold = Math.round(newLimit * 10.0) / 10.0
        recheckCurrentVitals()
    }

    fun setTempMode(mode: String) {
        tempMonitoringMode = mode
        dismissThresholdAlert()
        recheckCurrentVitals()
    }

    fun updateMaxHeartRate(newLimit: Int) {
        maxHeartRateThreshold = newLimit
        recheckCurrentVitals()
    }

    fun updateMinHeartRate(newLimit: Int) {
        minHeartRateThreshold = newLimit
        recheckCurrentVitals()
    }

    fun updateMinSpo2(newLimit: Int) {
        minSpo2Threshold = newLimit
        recheckCurrentVitals()
    }

    fun updateMaxGsr(newLimit: Double) {
        maxGsrThreshold = Math.round(newLimit * 10.0) / 10.0
        recheckCurrentVitals()
    }

    fun updateMaxGyro(newLimit: Double) {
        maxGyroThreshold = Math.round(newLimit * 10.0) / 10.0
        recheckCurrentVitals()
    }

    fun resetThresholdsToDefault() {
        dismissedAlerts.clear()
        tempMonitoringMode = "BODY"
        maxTempThreshold = 37.5
        minTempThreshold = 35.5
        maxMpuTempThreshold = 45.0
        maxHeartRateThreshold = 100
        minHeartRateThreshold = 55
        minSpo2Threshold = 94
        maxGsrThreshold = 3.8
        maxGyroThreshold = 45.0
        recheckCurrentVitals()
    }

    fun recheckCurrentVitals() {
        val payload = liveSensorPayload ?: SensorPayload(
            heartRate = latestLog.value?.heartRate ?: 72,
            temperature = latestLog.value?.temperature ?: 36.6,
            stressGsr = latestLog.value?.stressGsr ?: 2.1,
            fingerDetected = true
        )
        evaluateThresholds(payload)
    }

    /**
     * Vibrates the device using the Android Vibrator service.
     * Incorporates debouncing to prevent continuous rattling.
     */
    fun triggerVibration(severity: String) {
        if (!vibrationGuidanceEnabled) return
        val now = System.currentTimeMillis()
        if (now - lastVibrationTime < 8000) return // 8 second cooldown between buzzes
        lastVibrationTime = now

        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getApplication<Application>().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = if (severity == "critical") {
                        longArrayOf(0, 350, 150, 350, 150, 350)
                    } else {
                        longArrayOf(0, 200, 100, 200)
                    }
                    val amplitudes = if (severity == "critical") {
                        intArrayOf(0, 255, 0, 255, 0, 255)
                    } else {
                        intArrayOf(0, 180, 0, 180)
                    }
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    val pattern = if (severity == "critical") {
                        longArrayOf(0, 350, 150, 350, 150, 350)
                    } else {
                        longArrayOf(0, 200, 100, 200)
                    }
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (_: Exception) {
            // Handled gracefully on emulators or devices without haptics
        }
    }

    fun dismissThresholdAlert() {
        activeThresholdAlert?.let {
            dismissedAlerts.add(it.parameterName)
        }
        activeThresholdAlert = null
        stopTTS()
        stopAlarm()
    }

    /** Rings the phone's alarm sound (alarm stream) until dismissed or vitals recover. Idempotent per poll. */
    private fun startAlarm() {
        if (!soundEffectsEnabled || alarmRingtone?.isPlaying == true) return
        try {
            val ctx = getApplication<Application>()
            val uri = RingtoneManager.getActualDefaultRingtoneUri(ctx, RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            alarmRingtone = RingtoneManager.getRingtone(ctx, uri)?.apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                // API < 28 has no looping; the 1.5 s poll restarts it when it finishes
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) isLooping = true
                play()
            }
        } catch (_: Exception) {}
    }

    private fun stopAlarm() {
        try { alarmRingtone?.stop() } catch (_: Exception) {}
        alarmRingtone = null
    }

    /**
     * Test trigger to demonstrate vibration, warning banner, TTS audio remedy, and push notification in action.
     */
    fun triggerTestAlert(parameter: String = "temperature") {
        val alert = when (parameter) {
            "temperature" -> ThresholdAlert(
                title = "High Temperature Alert (38.6 °C)",
                parameterName = "Body Temperature",
                currentValue = "38.6 °C",
                threshold = "Max Limit: $maxTempThreshold °C",
                isIncreasing = true,
                severity = "critical",
                remedy = "Drink cool water immediately, rest in a cool air-conditioned room, loosen tight clothing, and apply a cool damp cloth to your forehead and neck."
            )
            "mpuTemp" -> ThresholdAlert(
                title = "High MPU6050 Heat (48.5 °C)",
                parameterName = "MPU6050 Silicon Die",
                currentValue = "48.5 °C",
                threshold = "Max Limit: $maxMpuTempThreshold °C",
                isIncreasing = true,
                severity = "critical",
                remedy = "Hardware silicon temperature exceeded safe limit. Ensure the Raspberry Pi and MPU-6050 have adequate airflow, disconnect from direct heat/sunlight, and verify 3.3V power rails."
            )
            "heartRate" -> ThresholdAlert(
                title = "Tachycardia Alert (118 BPM)",
                parameterName = "Heart Rate (BPM)",
                currentValue = "118 BPM",
                threshold = "Max Limit: $maxHeartRateThreshold BPM",
                isIncreasing = true,
                severity = "warning",
                remedy = "Sit down comfortably, practice slow 4-7-8 breathing (inhale 4s, hold 7s, exhale 8s), sip cool water, and avoid physical strain."
            )
            "spO2" -> ThresholdAlert(
                title = "Low Blood Oxygen (89% SpO2)",
                parameterName = "SpO2 Blood Oxygen",
                currentValue = "89%",
                threshold = "Min Limit: $minSpo2Threshold%",
                isIncreasing = false,
                severity = "critical",
                remedy = "Sit upright with shoulders relaxed, open windows for fresh air, practice pursed-lip breathing, and avoid lying flat."
            )
            "stress" -> ThresholdAlert(
                title = "High Autonomic Stress (4.5 µS)",
                parameterName = "Autonomic Stress GSR",
                currentValue = "4.5 µS",
                threshold = "Max Limit: $maxGsrThreshold µS",
                isIncreasing = true,
                severity = "warning",
                remedy = "Take a 3-minute mindful pause, close your eyes, unclench your jaw, perform 5 deep diaphragmatic breaths, and take a screen break."
            )
            else -> ThresholdAlert(
                title = "Tremor Kinematics Spike (62 °/s)",
                parameterName = "Tremor / Kinematics",
                currentValue = "62 °/s",
                threshold = "Max Limit: $maxGyroThreshold °/s",
                isIncreasing = true,
                severity = "warning",
                remedy = "Rest your forearm on a stable flat table, release shoulder tension, perform gentle finger stretches, and verify your medication schedule."
            )
        }

        activeThresholdAlert = alert
        lastVibrationTime = 0L
        triggerVibration(alert.severity)
        startAlarm()
        postSystemNotification(alert)
        speakRemedy("Warning! ${alert.title}. Exceeds threshold limit. Actionable remedy: ${alert.remedy}")
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

    /** Live heart rate from the band, or null when it's offline / no finger on the sensor. */
    fun currentHeartRate(): Int? {
        val p = liveSensorPayload ?: return null
        if (sensorConnectionStatus != "ONLINE" || !(p.fingerDetected || p.heartRate > 0)) return null
        return p.heartRate.takeIf { it > 0 }
    }

    /** Short tap on the phone's vibrator to pace each breath phase. */
    fun breathCue() {
        if (!vibrationGuidanceEnabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getApplication<Application>().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.vibrate(VibrationEffect.createOneShot(70, 140))
        } catch (_: Exception) {}
    }

    /** hrDrop = start HR - end HR (positive = calmer), null when no reading was available. */
    fun completeMindfulnessSession(title: String, durationMin: Int, hrDrop: Int?) {
        viewModelScope.launch {
            val session = MindfulnessSession(
                title = title,
                durationMin = durationMin,
                difficulty = if (durationMin <= 5) "Beginner" else "Intermediate",
                heartRateReduction = hrDrop ?: 0,
                focusIncreasePercent = 0, // not measured
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

    /** True when a real Gemini key was built in (.env); otherwise the coach answers offline. */
    val isAiConfigured: Boolean =
        BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    private val chatHttp = OkHttpClient.Builder().readTimeout(40, TimeUnit.SECONDS).build()

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage("user", userText.trim())
        chatMessages.add(userMsg)

        viewModelScope.launch {
            repository.insertChat(userMsg)
            isThinkingByCoach = true
            val live = liveSensorPayload.takeIf { sensorConnectionStatus == "ONLINE" }
            val log = repository.latestLog.first()
            val vitals = CoachVitals(
                live = live != null,
                heartRate = (live?.heartRate ?: log?.heartRate)?.takeIf { it > 0 },
                spo2 = live?.spO2?.takeIf { it > 0 && (live.fingerDetected || live.heartRate > 0) },
                temperature = live?.temperature ?: log?.temperature,
                gsr = live?.stressGsr ?: log?.stressGsr,
                steps = live?.steps ?: log?.steps ?: 0
            )
            // Gemini needs the conversation to start with a user turn, so drop the leading greeting.
            val history = chatMessages.takeLast(12).dropWhile { it.sender != "user" }

            val hasKey = isAiConfigured
            val aiReply = if (hasKey) askGemini(BuildConfig.GEMINI_API_KEY, history, vitals) else null
            val reply = aiReply ?: (offlineCoachReply(userText, vitals) + if (hasKey)
                "\n\n(Couldn't reach the AI server — check internet. This is offline guidance.)"
            else
                "\n\n(Offline mode — add GEMINI_API_KEY in .env and rebuild for full AI answers.)")

            val coachMsg = ChatMessage("coach", reply)
            chatMessages.add(coachMsg)
            repository.insertChat(coachMsg)
            isThinkingByCoach = false
        }
    }

    private suspend fun askGemini(key: String, history: List<ChatMessage>, vitals: CoachVitals): String? =
        withContext(Dispatchers.IO) {
            try {
                val contents = JSONArray()
                history.forEach { m ->
                    contents.put(
                        JSONObject()
                            .put("role", if (m.sender == "user") "user" else "model")
                            .put("parts", JSONArray().put(JSONObject().put("text", m.text)))
                    )
                }
                val system = COACH_SYSTEM_PROMPT + "\n\nCurrent NeuroBand readings: " + vitals.describe()
                val body = JSONObject()
                    .put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
                    .put("contents", contents)
                    .put("generationConfig", JSONObject().put("temperature", 0.7))
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent")
                    .header("x-goog-api-key", key)
                    .post(body.toString().toRequestBody("application/json".toMediaType()))
                    .build()
                chatHttp.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext null
                    val parts = JSONObject(resp.body?.string().orEmpty())
                        .getJSONArray("candidates").getJSONObject(0)
                        .getJSONObject("content").getJSONArray("parts")
                    (0 until parts.length())
                        .joinToString("") { parts.getJSONObject(it).optString("text") }
                        .replace("**", "")
                        .trim()
                        .ifEmpty { null }
                }
            } catch (_: Exception) {
                null
            }
        }
}

private const val GEMINI_MODEL = "gemini-flash-latest"

private const val COACH_SYSTEM_PROMPT = """You are NeuroBand AI Coach, the assistant inside a stress & wellness wearable app. The band has a MAX30102 (heart rate, SpO2), MPU6050 (motion, steps, temperature) and a GSR skin-conductance sensor. The app has Home, Flow (analytics), Scan (TinyML stress scan, Yoga poses, Diet plans) and Insight (settings & alert thresholds) tabs.
Answer the user's actual question directly and helpfully - any topic: stress, anxiety, sleep, breathing, yoga, diet, hydration, study/exam focus, fitness, general health, or how the app works. When their readings are relevant, use them and explain what they mean in plain words.
Style: warm, practical, concise - 2 to 6 short sentences, or a few lines starting with "• ". Plain text only, no markdown, no headings.
You are not a doctor. For chest pain, fainting, severe breathlessness, SpO2 below 90%, or any emergency, tell them to get urgent medical help immediately."""

data class CoachVitals(
    val live: Boolean,
    val heartRate: Int?,
    val spo2: Int?,
    val temperature: Double?,
    val gsr: Double?,
    val steps: Int
) {
    fun describe(): String = buildString {
        append(if (live) "live from the band. " else "band not connected; last logged values. ")
        append("Heart rate ${heartRate?.let { "$it BPM" } ?: "unknown"}, ")
        append("SpO2 ${spo2?.let { "$it%" } ?: "unknown"}, ")
        append("temperature ${temperature?.let { "%.1f °C".format(it) } ?: "unknown"}, ")
        append("skin conductance ${gsr?.let { "%.1f µS".format(it) } ?: "unknown"}, ")
        append("steps today $steps.")
    }
}

/** Rule-based answers used when Gemini is not configured or unreachable. */
fun offlineCoachReply(question: String, v: CoachVitals): String {
    val q = question.lowercase()
    // Word-prefix match: "stress" hits "stressed", but "eat" doesn't hit "great".
    fun has(vararg words: String) = words.any { Regex("\\b" + Regex.escape(it)).containsMatchIn(q) }
    val hr = v.heartRate
    val hrNote = when {
        hr == null -> "I can't see a heart-rate reading right now — place your finger on the MAX30102 sensor."
        hr > 100 -> "Your heart rate is $hr BPM, which is above the normal resting range (60–100)."
        hr < 55 -> "Your heart rate is $hr BPM, which is on the low side for resting."
        else -> "Your heart rate is $hr BPM — within the healthy resting range (60–100)."
    }
    val stressHigh = (v.gsr ?: 0.0) > 3.8 || (hr ?: 0) > 100

    return when {
        has("chest pain", "faint", "can't breathe", "cant breathe", "emergency", "heart attack") ->
            "This could be serious. Please stop what you're doing, sit down, and contact emergency services or a doctor right away. Don't rely on the app for emergencies."
        has("hi", "hello", "hey") && q.length < 15 ->
            "Hi! I'm your NeuroBand coach. Ask me about your heart rate, stress, sleep, breathing, yoga, diet or study focus. $hrNote"
        has("thank") -> "You're welcome! Keep taking small breaks and stay hydrated."
        has("heart", "pulse", "bpm") ->
            "$hrNote\n• Resting HR usually sits between 60 and 100 BPM.\n• Caffeine, stress, dehydration and recent activity raise it.\n• Slow breathing (inhale 4s, exhale 6s) for 2 minutes usually brings it down."
        has("oxygen", "spo2", "o2") -> {
            val s = v.spo2
            when {
                s == null -> "No SpO2 reading yet — rest your fingertip gently on the sensor and stay still for 10 seconds."
                s < 90 -> "Your SpO2 is $s%, which is low. Sit upright, breathe slowly and deeply, and seek medical help if it stays below 90% or you feel breathless."
                s < 95 -> "Your SpO2 is $s%, slightly below ideal (95–100%). Sit upright, open a window and take slow, deep breaths."
                else -> "Your SpO2 is $s% — healthy (95–100% is normal)."
            }
        }
        has("temp", "fever", "hot", "cold", "heat") -> {
            val t = v.temperature
            when {
                t == null -> "I don't have a temperature reading right now."
                t > 37.5 -> "Your temperature is %.1f °C, a little high. Drink cool water, rest in a ventilated room and use a cool damp cloth. See a doctor if it goes above 38.5 °C or lasts more than a day.".format(t)
                t < 35.5 -> "Your temperature is %.1f °C, on the low side. Wear warm layers and have a warm drink.".format(t)
                else -> "Your temperature is %.1f °C — normal range.".format(t)
            }
        }
        has("stress", "anxi", "panic", "tense", "nervous", "overwhelm", "angry") ->
            (if (stressHigh) "Your readings show elevated stress right now. " else "Your readings look fairly calm, but let's settle your mind. ") +
                "Try this:\n• 4-7-8 breathing: inhale 4s, hold 7s, exhale 8s — repeat 4 times.\n• Unclench your jaw and drop your shoulders.\n• Step away from the screen for 3 minutes.\nThe Scan → Yoga tab has a guided Child's Pose that helps too."
        has("sleep", "insomnia", "tired", "fatigue", "rest") ->
            "For better sleep:\n• Keep a fixed bed and wake time, even on weekends.\n• No screens or caffeine 1 hour before bed.\n• Try 5 minutes of Savasana or slow breathing in bed.\n• Keep the room cool and dark.\nIf you feel tired during the day, a 20-minute nap before 3 PM helps without hurting night sleep."
        has("breath", "calm", "relax") ->
            "Box breathing works fast:\n• Inhale 4s\n• Hold 4s\n• Exhale 4s\n• Hold 4s\nRepeat for 2–3 minutes. $hrNote"
        has("yoga", "stretch", "pose", "exercise", "workout") ->
            "Good stress-relief poses (see Scan → Yoga for steps):\n• Child's Pose — 5 min, releases back tension.\n• Cat-Cow — 5 min, loosens the spine.\n• Tree Pose — 8 min, improves focus.\n• Savasana — 10 min, deep relaxation.\nYou've walked ${v.steps} steps today."
        has("food", "diet", "eat", "meal", "nutrition") ->
            "Stress-friendly eating:\n• Morning: oats with nuts + green tea (magnesium, L-theanine).\n• Lunch: fish or dal, leafy greens, avocado (omega-3, potassium).\n• Night: sweet potato, curd or turkey (tryptophan for sleep).\nLimit sugar and caffeine after 4 PM."
        has("water", "hydrat", "drink") ->
            "Aim for about 2–3 litres of water a day, more if it's hot or you exercise. Dehydration raises heart rate and makes stress feel worse — keep a bottle on your desk."
        has("study", "focus", "exam", "concentrat", "burnout", "work") ->
            "To stay focused without burning out:\n• Work 25 min, break 5 min (Pomodoro).\n• Every 2 hours take a 15-min walk.\n• Sleep 7–8 hours — memory consolidates during sleep.\n• Use 2 minutes of slow breathing before starting." +
                if (stressHigh) "\nYour readings show elevated stress — take a break now." else ""
        has("headache", "migraine") ->
            "For a tension headache: drink a glass of water, rest your eyes away from screens, massage your temples and neck, and breathe slowly. See a doctor if it's sudden and severe or comes with vision changes."
        has("step", "walk", "activity", "motion") ->
            "You've taken ${v.steps} steps today. A good target is 7,000–10,000. Short 5-minute walks every hour help lower stress hormones."
        else ->
            "Here's your current status: " + v.describe() + "\n$hrNote\nYou can ask me about stress, sleep, breathing, yoga, diet, hydration, or study focus."
    }
}

/** Splits a one-sentence remedy ("Do A, do B (x, y), and do C.") into separate steps. */
fun remedySteps(remedy: String): List<String> =
    remedy.split(Regex("""(?:,\s*(?:and\s+)?|\.\s+)(?![^(]*\))"""))
        .map { it.trim().trimEnd('.').replaceFirstChar { c -> c.uppercase() } }
        .filter { it.isNotEmpty() }
