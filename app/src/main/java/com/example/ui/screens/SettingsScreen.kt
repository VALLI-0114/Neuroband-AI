package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BiometricViewModel
import com.example.ui.components.GlassmorphicCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BiometricViewModel,
    innerPadding: PaddingValues
) {
    val isDark = viewModel.isDarkTheme
    val primaryTextColor = if (isDark) Color.White else Color(0xFF0F172A)
    val secondaryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var activeActionTitle by remember { mutableStateOf<String?>(null) }
    var activeActionDesc by remember { mutableStateOf<String?>(null) }

    var ipInputValue by remember(viewModel.raspberryPiHost) { mutableStateOf(viewModel.raspberryPiHost) }

    val livePayload = viewModel.liveSensorPayload
    val isOnline = viewModel.sensorConnectionStatus == "ONLINE"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + 16.dp,
            bottom = innerPadding.calculateBottomPadding() + 92.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Daily Insight Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF06B6D4).copy(alpha = 0.15f), shape = RoundedCornerShape(99.dp))
                        .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.3f), RoundedCornerShape(99.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        "HARDWARE CONTROL CENTER",
                        color = if (isDark) Color(0xFF06B6D4) else Color(0xFF0284C7),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Thresholds & Setup",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = primaryTextColor,
                    lineHeight = 38.sp,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Adjust safety thresholds, vibration alerts, TTS voice remedies, and configure your Raspberry Pi sensor bridge.",
                    fontSize = 13.sp,
                    color = secondaryTextColor
                )
            }
        }

        // THEME & DISPLAY SELECTION CARD (Light vs Dark Mode)
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "APPEARANCE & THEME",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isDark) Color(0xFF8B5CF6).copy(alpha = 0.2f) else Color(0xFF0284C7).copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                    contentDescription = "Theme Icon",
                                    tint = if (isDark) Color(0xFFA78BFA) else Color(0xFF0284C7),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (isDark) "Dark Cyber Theme" else "Clean Light Theme",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor
                                )
                                Text(
                                    text = if (isDark) "Deep Cyber Navy UI active" else "Crisp Light UI active",
                                    fontSize = 11.sp,
                                    color = secondaryTextColor
                                )
                            }
                        }

                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.toggleTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF8B5CF6),
                                uncheckedThumbColor = Color(0xFF0284C7),
                                uncheckedTrackColor = Color(0xFFE0F2FE)
                            )
                        )
                    }
                }
            }
        }

        // SAFETY THRESHOLD LIMITS & WARNING ALARMS CARD
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = Color(0x33F59E0B)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "SAFETY THRESHOLDS & ALARM LIMITS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFF59E0B),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    "Adjust Trigger Limits",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor
                                )
                            }
                        }

                        TextButton(
                            onClick = { viewModel.resetThresholdsToDefault() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "RESET",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF06B6D4) else Color(0xFF0284C7)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "When sensor readings exceed these limits, the app automatically vibrates, posts a system notification, and speaks actionable remedies via TTS.",
                        fontSize = 11.sp,
                        color = secondaryTextColor,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Temperature Sensor Mode Selector
                    Text(
                        "TEMPERATURE SOURCE & THRESHOLD MODE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = primaryTextColor,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isBody = viewModel.tempMonitoringMode == "BODY"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isBody) (if (isDark) Color(0xFF06B6D4).copy(alpha = 0.25f) else Color(0xFF0284C7).copy(alpha = 0.15f)) else (if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)))
                                .border(1.5.dp, if (isBody) (if (isDark) Color(0xFF06B6D4) else Color(0xFF0284C7)) else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { viewModel.setTempMode("BODY") }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "🌡️ Body Temp Mode",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBody) (if (isDark) Color(0xFF06B6D4) else Color(0xFF0284C7)) else secondaryTextColor
                                )
                                Text(
                                    "Calibrated (~36.6°C • Limit: ${viewModel.maxTempThreshold}°C)",
                                    fontSize = 9.sp,
                                    color = secondaryTextColor
                                )
                            }
                        }

                        val isMpu = viewModel.tempMonitoringMode == "MPU_RAW"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isMpu) (if (isDark) Color(0xFFF59E0B).copy(alpha = 0.25f) else Color(0xFFD97706).copy(alpha = 0.15f)) else (if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)))
                                .border(1.5.dp, if (isMpu) (if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)) else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { viewModel.setTempMode("MPU_RAW") }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "⚙️ MPU Hardware Mode",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMpu) (if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)) else secondaryTextColor
                                )
                                Text(
                                    "Raw Silicon (~39°C • Limit: ${viewModel.maxMpuTempThreshold}°C)",
                                    fontSize = 9.sp,
                                    color = secondaryTextColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (viewModel.tempMonitoringMode == "BODY") {
                        // 1. Max Body Temperature
                        ThresholdStepperRow(
                            title = "Max Body Temperature",
                            valueStr = "${viewModel.maxTempThreshold} °C",
                            note = "Normal: 36.1 - 37.2°C • Fever triggers if breached",
                            onDecrement = { viewModel.updateMaxTemp(viewModel.maxTempThreshold - 0.1) },
                            onIncrement = { viewModel.updateMaxTemp(viewModel.maxTempThreshold + 0.1) },
                            isDark = isDark
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Min Body Temperature
                        ThresholdStepperRow(
                            title = "Min Body Temperature",
                            valueStr = "${viewModel.minTempThreshold} °C",
                            note = "Hypothermia warning if below limit",
                            onDecrement = { viewModel.updateMinTemp(viewModel.minTempThreshold - 0.1) },
                            onIncrement = { viewModel.updateMinTemp(viewModel.minTempThreshold + 0.1) },
                            isDark = isDark
                        )
                    } else {
                        // Max MPU6050 Silicon Die Temperature
                        ThresholdStepperRow(
                            title = "Max MPU6050 Silicon Temp",
                            valueStr = "${viewModel.maxMpuTempThreshold} °C",
                            note = "Raw silicon runs ~38°C–42°C normal heat. Overheat alarm triggers above this limit.",
                            onDecrement = { viewModel.updateMaxMpuTemp(viewModel.maxMpuTempThreshold - 0.5) },
                            onIncrement = { viewModel.updateMaxMpuTemp(viewModel.maxMpuTempThreshold + 0.5) },
                            isDark = isDark
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Max Heart Rate
                    ThresholdStepperRow(
                        title = "Max Heart Rate (Tachycardia)",
                        valueStr = "${viewModel.maxHeartRateThreshold} BPM",
                        note = "Resting limit: 60 - 100 BPM",
                        onDecrement = { viewModel.updateMaxHeartRate(viewModel.maxHeartRateThreshold - 5) },
                        onIncrement = { viewModel.updateMaxHeartRate(viewModel.maxHeartRateThreshold + 5) },
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Min Heart Rate
                    ThresholdStepperRow(
                        title = "Min Heart Rate (Bradycardia)",
                        valueStr = "${viewModel.minHeartRateThreshold} BPM",
                        note = "Low pulse warning if below limit",
                        onDecrement = { viewModel.updateMinHeartRate(viewModel.minHeartRateThreshold - 5) },
                        onIncrement = { viewModel.updateMinHeartRate(viewModel.minHeartRateThreshold + 5) },
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. Min SpO2 Oxygen
                    ThresholdStepperRow(
                        title = "Min Blood Oxygen (SpO2)",
                        valueStr = "${viewModel.minSpo2Threshold}%",
                        note = "Hypoxia alert if saturation drops below limit",
                        onDecrement = { viewModel.updateMinSpo2(viewModel.minSpo2Threshold - 1) },
                        onIncrement = { viewModel.updateMinSpo2(viewModel.minSpo2Threshold + 1) },
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6. Max Stress GSR
                    ThresholdStepperRow(
                        title = "Max Stress GSR Conductance",
                        valueStr = "${viewModel.maxGsrThreshold} µS",
                        note = "Sympathetic nervous spike threshold",
                        onDecrement = { viewModel.updateMaxGsr(viewModel.maxGsrThreshold - 0.1) },
                        onIncrement = { viewModel.updateMaxGsr(viewModel.maxGsrThreshold + 0.1) },
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 7. Max Tremor / Gyro
                    ThresholdStepperRow(
                        title = "Max Tremor Kinematics (MPU6050)",
                        valueStr = "${viewModel.maxGyroThreshold} °/s",
                        note = "Involuntary shake / kinetic threshold",
                        onDecrement = { viewModel.updateMaxGyro(viewModel.maxGyroThreshold - 5.0) },
                        onIncrement = { viewModel.updateMaxGyro(viewModel.maxGyroThreshold + 5.0) },
                        isDark = isDark
                    )
                }
            }
        }

        // CONSOLE & ALERTS PREFERENCES (Vibration, TTS, Notifications)
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "ALARM & REMEDY DELIVERY PREFERENCES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // TTS Voice Guidance Toggle
                    ToggleControlRow(
                        label = "Text-to-Speech (TTS) Voice Remedies",
                        desc = "Speaks the warning and exact actionable medical remedy aloud hands-free.",
                        checked = viewModel.ttsVoiceEnabled,
                        onCheckedChange = { viewModel.ttsVoiceEnabled = it },
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tactile Vibration Toggle
                    ToggleControlRow(
                        label = "Tactile Vibration Feedback",
                        desc = "Vibrates the phone when vitals or motion breach configured limits.",
                        checked = viewModel.vibrationGuidanceEnabled,
                        onCheckedChange = { viewModel.vibrationGuidanceEnabled = it },
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Push Notification Toggle
                    ToggleControlRow(
                        label = "System Push Notifications",
                        desc = "Dispatches high-priority heads-up notification with remedy text.",
                        checked = viewModel.notificationFeedsEnabled,
                        onCheckedChange = { viewModel.notificationFeedsEnabled = it },
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        "TEST ALARM, TTS & NOTIFICATIONS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color(0xFF06B6D4) else Color(0xFF0284C7),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Tap any test below to instantly trigger vibration, voice remedy audio (TTS), push notification, and screen warning banner:",
                        fontSize = 11.sp,
                        color = secondaryTextColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Test Buttons Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.triggerTestAlert("temperature")
                                viewModel.selectTab(0)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("🌡 High Temp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                viewModel.triggerTestAlert("heartRate")
                                viewModel.selectTab(0)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("💓 High BPM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.triggerTestAlert("spO2")
                                viewModel.selectTab(0)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("🫁 Low Oxygen", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                viewModel.triggerTestAlert("stress")
                                viewModel.selectTab(0)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("⚡ High Stress", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.triggerTestAlert("mpuTemp")
                                viewModel.selectTab(0)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("⚙ MPU Heat (48°C)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                viewModel.triggerTestAlert("gyro")
                                viewModel.selectTab(0)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("🌀 Tremor Spike", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // RASPBERRY PI NETWORK CONFIGURATION CARD
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = if (isOnline) Color(0x3306B6D4) else Color(0x22EF4444)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF06B6D4).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Wifi,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF06B6D4) else Color(0xFF0284C7),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("HARDWARE IP ADDRESS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), letterSpacing = 1.sp)
                                Text("Raspberry Pi Sensor Bridge", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
                            }
                        }

                        // Connection Status Pill
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isOnline) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                                    RoundedCornerShape(99.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isOnline) Color(0xFF22C55E).copy(alpha = 0.35f) else Color(0xFFEF4444).copy(alpha = 0.35f),
                                    RoundedCornerShape(99.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = viewModel.sensorConnectionStatus,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isOnline) Color(0xFF22C55E) else Color(0xFFEF4444)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // IP Input Field
                    OutlinedTextField(
                        value = ipInputValue,
                        onValueChange = { ipInputValue = it },
                        label = { Text("Raspberry Pi IP or Hostname", fontSize = 12.sp, color = secondaryTextColor) },
                        placeholder = { Text("e.g. 192.168.1.50 or 10.0.0.5", fontSize = 12.sp, color = secondaryTextColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = primaryTextColor,
                            unfocusedTextColor = primaryTextColor,
                            focusedBorderColor = if (isDark) Color(0xFF06B6D4) else Color(0xFF0284C7),
                            unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                            focusedContainerColor = if (isDark) Color(0xFF090D16) else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDark) Color(0xFF090D16) else Color(0xFFF8FAFC)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.updateRaspberryPiHost(ipInputValue) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF06B6D4) else Color(0xFF0284C7)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SAVE HOST", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { viewModel.testSensorConnection() },
                            shape = RoundedCornerShape(14.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp,
                                brush = Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6)))
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isDark) Color(0xFF06B6D4) else Color(0xFF0284C7))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TEST LINK", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryTextColor)
                        }
                    }

                    if (viewModel.connectionErrorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "⚠ ${viewModel.connectionErrorMessage}",
                            fontSize = 11.sp,
                            color = Color(0xFFEF4444),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // HARDWARE DIAGNOSTICS & TELEMETRY PROBES CARD
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("TELEMETRY STATUS & LIVE PROBES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    SensorStatusRow(label = "MAX30102 PPG Optical Bio-Sensor", active = isOnline, isDark = isDark)
                    SensorStatusRow(label = "MPU6050 6-DOF IMU Kinematics", active = isOnline, isDark = isDark)
                    SensorStatusRow(label = "Finger-Touch Optical Contact", active = isOnline && (livePayload?.fingerDetected ?: false), isDark = isDark)

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DevicePropertyColumn(label = "SAMPLE RATE", value = "100 Hz", isDark = isDark)
                        DevicePropertyColumn(label = "FIFO BUFFER", value = "4-Depth Avg", isDark = isDark)
                        DevicePropertyColumn(label = "I2C BUS", value = "Bus 1 (400kHz)", isDark = isDark)
                    }
                }
            }
        }

        // RECOMMENDATIONS LIBRARY
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "TARGETED RECOMMENDATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp
                )

                RecommendationUnit(
                    category = "Sleep",
                    text = "Shutdown all OLED/blue screens after 10 PM. Introduce Pre-Sleep Calm Breathing series.",
                    icon = Icons.Rounded.Bedtime,
                    color = Color(0xFF06B6D4),
                    isDark = isDark
                )

                RecommendationUnit(
                    category = "Stress Relief",
                    text = "Initiate a 5-minute Coherent Breathwork cycle to normalize erratic sympathetic signals.",
                    icon = Icons.Rounded.SelfImprovement,
                    color = Color(0xFF8B5CF6),
                    isDark = isDark
                )

                RecommendationUnit(
                    category = "Recovery",
                    text = "Schedule a daily 1-hour academic disconnected interval to allow cardiac recuperation.",
                    icon = Icons.Rounded.OfflineBolt,
                    color = Color(0xFF10B981),
                    isDark = isDark
                )
            }
        }
    }

    // Interactive Action info dialog
    if (activeActionTitle != null && activeActionDesc != null) {
        AlertDialog(
            onDismissRequest = {
                activeActionTitle = null
                activeActionDesc = null
            },
            title = {
                Text(text = activeActionTitle ?: "", fontWeight = FontWeight.Black, color = primaryTextColor)
            },
            text = {
                Text(text = activeActionDesc ?: "", fontSize = 14.sp, color = secondaryTextColor)
            },
            confirmButton = {
                Button(
                    onClick = {
                        activeActionTitle = null
                        activeActionDesc = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF06B6D4) else Color(0xFF0284C7))
                ) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
        )
    }
}

@Composable
fun ThresholdStepperRow(
    title: String,
    valueStr: String,
    note: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    isDark: Boolean
) {
    val cardBg = if (isDark) Color(0xFF0F172A).copy(alpha = 0.8f) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val noteColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(2.dp))
                Text(note, fontSize = 10.sp, color = noteColor)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Decrement Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                        .clickable { onDecrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "Decrease",
                        tint = if (isDark) Color(0xFF06B6D4) else Color(0xFF0284C7),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .widthIn(min = 68.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = valueStr,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color(0xFF06B6D4) else Color(0xFF0284C7)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Increment Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                        .clickable { onIncrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Increase",
                        tint = if (isDark) Color(0xFF06B6D4) else Color(0xFF0284C7),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DevicePropertyColumn(label: String, value: String, isDark: Boolean) {
    Column {
        Text(label, fontSize = 10.sp, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF0F172A))
    }
}

@Composable
fun SensorStatusRow(label: String, active: Boolean, isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (active) Color(0xFF22C55E) else Color(0xFFEF4444), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (active) "ONLINE" else "OFFLINE",
                fontSize = 10.sp,
                color = if (active) Color(0xFF22C55E) else Color(0xFFEF4444),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RecommendationUnit(
    category: String,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isDark) Color(0xFF0F172A).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.9f),
                RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = color)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = text,
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ToggleControlRow(
    label: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.1f)) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF0F172A))
            Text(desc, fontSize = 10.sp, color = Color(0xFF64748B), lineHeight = 14.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = if (isDark) Color(0xFF06B6D4) else Color(0xFF0284C7),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
            )
        )
    }
}
