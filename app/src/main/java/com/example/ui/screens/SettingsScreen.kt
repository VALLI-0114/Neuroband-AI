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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    var activeActionTitle by remember { mutableStateOf<String?>(null) }
    var activeActionDesc by remember { mutableStateOf<String?>(null) }

    var ipInputValue by remember(viewModel.raspberryPiHost) { mutableStateOf(viewModel.raspberryPiHost) }

    var soundEffectsEnabled by remember { mutableStateOf(true) }
    var vibrationGuidanceEnabled by remember { mutableStateOf(true) }
    var notificationFeedsEnabled by remember { mutableStateOf(true) }

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
        verticalArrangement = Arrangement.spacedBy(24.dp)
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
                        color = Color(0xFF06B6D4),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Sensor Bridge",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    lineHeight = 40.sp,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Configure your Raspberry Pi sensor bridge and stream real-time MAX30102 & MPU6050 biometrics.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
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
                            Icon(
                                imageVector = if (isOnline) Icons.Rounded.Wifi else Icons.Rounded.WifiOff,
                                contentDescription = null,
                                tint = if (isOnline) Color(0xFF06B6D4) else Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Raspberry Pi Sensor Node",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "LAN REST API • Port 5000",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        // Connection Status Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    when (viewModel.sensorConnectionStatus) {
                                        "ONLINE" -> Color(0xFF22C55E).copy(alpha = 0.18f)
                                        "CONNECTING" -> Color(0xFFEAB308).copy(alpha = 0.18f)
                                        else -> Color(0xFFEF4444).copy(alpha = 0.18f)
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    when (viewModel.sensorConnectionStatus) {
                                        "ONLINE" -> Color(0xFF22C55E).copy(alpha = 0.4f)
                                        "CONNECTING" -> Color(0xFFEAB308).copy(alpha = 0.4f)
                                        else -> Color(0xFFEF4444).copy(alpha = 0.4f)
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = viewModel.sensorConnectionStatus,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = when (viewModel.sensorConnectionStatus) {
                                    "ONLINE" -> Color(0xFF22C55E)
                                    "CONNECTING" -> Color(0xFFEAB308)
                                    else -> Color(0xFFEF4444)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // IP Address Input Field
                    OutlinedTextField(
                        value = ipInputValue,
                        onValueChange = { ipInputValue = it },
                        label = { Text("Raspberry Pi IP Address / Host", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                        placeholder = { Text("192.168.1.50 or 10.0.2.2", color = Color(0xFF475569)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF06B6D4),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.8f),
                            unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                ipInputValue = "10.0.2.2"
                                viewModel.updateRaspberryPiHost("10.0.2.2")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E293B),
                                contentColor = Color(0xFF94A3B8)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Emulator (10.0.2.2)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.updateRaspberryPiHost(ipInputValue)
                                viewModel.testSensorConnection()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF06B6D4),
                                contentColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (viewModel.connectionErrorMessage != null && !isOnline) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Connection Error: ${viewModel.connectionErrorMessage}",
                            fontSize = 11.sp,
                            color = Color(0xFFEF4444)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text("CONNECTED I2C HARDWARE SENSORS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))

                    SensorStatusRow(label = "MAX30102 (PPG / Heart Rate & SpO2)", active = isOnline)
                    SensorStatusRow(label = "MPU6050 (6-Axis IMU Accelerometer + Gyro)", active = isOnline)

                    if (livePayload != null && isOnline) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("LIVE HARDWARE TELEMETRY STREAM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DevicePropertyColumn("Heart Rate", "${livePayload.heartRate} BPM")
                            DevicePropertyColumn("Steps", "${livePayload.steps}")
                            DevicePropertyColumn("Stress GSR", "${livePayload.stressGsr} µS")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DevicePropertyColumn("Accel Net", "${livePayload.acceleration} g")
                            DevicePropertyColumn("Accel XYZ", "(${livePayload.ax}, ${livePayload.ay}, ${livePayload.az})")
                            DevicePropertyColumn("Raw IR", "${livePayload.rawIr}")
                        }
                    }
                }
            }
        }

        // ALERT Center (Active Warning/Reminder System)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "ACTIVE ALERTS SYSTEM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp
                )

                if (viewModel.activeAlerts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓ All stress parameters normal. No active warnings.",
                            color = Color(0xFF22C55E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    viewModel.activeAlerts.forEach { alert ->
                        val alertColor = when (alert.severity) {
                            "critical" -> Color(0xFFEF4444)
                            "high" -> Color(0xFFF97316)
                            else -> Color(0xFF3B82F6)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A).copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                                .border(1.dp, alertColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1.1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(alertColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                alert.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                alert.time,
                                                fontSize = 9.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            alert.description,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = { viewModel.dismissAlert(alert.id) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF06B6D4))
                                ) {
                                    Text("CLEAR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // RECOMMENDATIONS LIBRARY LAYOUT
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
                    color = Color(0xFF06B6D4)
                )

                RecommendationUnit(
                    category = "Stress Relief",
                    text = "Initiate a 5-minute Coherent Breathwork cycle to normalize erratic sympathetic signals.",
                    icon = Icons.Rounded.SelfImprovement,
                    color = Color(0xFF8B5CF6)
                )

                RecommendationUnit(
                    category = "Recovery",
                    text = "Schedule a daily 1-hour academic disconnected interval to allow cardiac recuperation.",
                    icon = Icons.Rounded.OfflineBolt,
                    color = Color(0xFF10B981)
                )

                RecommendationUnit(
                    category = "Focus Tuning",
                    text = "Silence all secondary alerts and trigger focus intervals during hard homework blocks.",
                    icon = Icons.Rounded.AutoAwesome,
                    color = Color(0xFFF59E0B)
                )

                RecommendationUnit(
                    category = "Burnout Avoidance",
                    text = "Plan structured weekend decompression voids to downregulate chronic stress baselines.",
                    icon = Icons.Rounded.WbSunny,
                    color = Color(0xFFEF4444)
                )
            }
        }

        // CONSOLE CONFIGURATION CONTROLS
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("CONSOLE PREFERENCES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(14.dp))

                    ToggleControlRow(
                        label = "Tactile Vibration Guidance",
                        desc = "Vibrates the band to pace breathing intervals.",
                        checked = vibrationGuidanceEnabled,
                        onCheckedChange = { vibrationGuidanceEnabled = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ToggleControlRow(
                        label = "Sound Effects Audio",
                        desc = "Enable calm chime feedbacks inside scans.",
                        checked = soundEffectsEnabled,
                        onCheckedChange = { soundEffectsEnabled = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ToggleControlRow(
                        label = "Adaptive Alert Notifications",
                        desc = "Alerts on abrupt electrodermal (GSR) conduction increases.",
                        checked = notificationFeedsEnabled,
                        onCheckedChange = { notificationFeedsEnabled = it }
                    )
                }
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
                Text(text = activeActionTitle ?: "", fontWeight = FontWeight.Black, color = Color.White)
            },
            text = {
                Text(text = activeActionDesc ?: "", fontSize = 14.sp, color = Color(0xFF94A3B8))
            },
            confirmButton = {
                Button(
                    onClick = {
                        activeActionTitle = null
                        activeActionDesc = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4))
                ) {
                    Text("OK", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color(0xFF0F172A)
        )
    }
}

@Composable
fun DevicePropertyColumn(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun SensorStatusRow(label: String, active: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFFCBD5E1))
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
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A).copy(alpha = 0.8f), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
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
                Text(text, fontSize = 12.sp, color = Color(0xFFE2E8F0), lineHeight = 16.sp)
            }
        }
    }
}

@Composable
fun ToggleControlRow(
    label: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.1f)) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(desc, fontSize = 10.sp, color = Color(0xFF64748B), lineHeight = 14.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF0F172A),
                checkedTrackColor = Color(0xFF06B6D4),
                uncheckedThumbColor = Color(0xFF64748B),
                uncheckedTrackColor = Color(0xFF1E293B)
            )
        )
    }
}
