package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BiometricViewModel
import com.example.ui.ThresholdAlert
import com.example.ui.remedySteps
import com.example.ui.components.VaporwaveBackground
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setContent {
            val vm: BiometricViewModel = viewModel()

            MyApplicationTheme(darkTheme = vm.isDarkTheme) {
                VaporwaveBackground {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        bottomBar = {
                            FloatingBottomNavigation(
                                currentTab = vm.currentTab,
                                isDark = vm.isDarkTheme,
                                onTabSelected = { vm.selectTab(it) }
                            )
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            Crossfade(
                                targetState = vm.currentTab,
                                label = "tab_switch"
                            ) { tab ->
                                when (tab) {
                                    0 -> HomeScreen(viewModel = vm, innerPadding = innerPadding)
                                    1 -> AnalyticsScreen(viewModel = vm, innerPadding = innerPadding)
                                    2 -> BreathingScreen(viewModel = vm, innerPadding = innerPadding)
                                    3 -> SettingsScreen(viewModel = vm, innerPadding = innerPadding)
                                }
                            }

                            // ANIMATED HEADS-UP WARNING & REMEDY OVERLAY
                            AnimatedVisibility(
                                visible = vm.activeThresholdAlert != null,
                                enter = slideInVertically(
                                    initialOffsetY = { -it },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) + fadeIn(),
                                exit = slideOutVertically(
                                    targetOffsetY = { -it },
                                    animationSpec = tween(300)
                                ) + fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .statusBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                vm.activeThresholdAlert?.let { alert ->
                                    AnimatedScreenWarningCard(
                                        alert = alert,
                                        isDark = vm.isDarkTheme,
                                        isTtsSpeaking = vm.isTtsSpeaking,
                                        onListenTts = {
                                            if (vm.isTtsSpeaking) {
                                                vm.stopTTS()
                                            } else {
                                                vm.speakRemedy("Warning! ${alert.title}. Exceeds threshold limit. Actionable remedy: ${alert.remedy}")
                                            }
                                        },
                                        onBreathing = {
                                            vm.selectTab(2)
                                        },
                                        onDismiss = {
                                            vm.dismissThresholdAlert()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedScreenWarningCard(
    alert: ThresholdAlert,
    isDark: Boolean,
    isTtsSpeaking: Boolean,
    onListenTts: () -> Unit,
    onBreathing: () -> Unit,
    onDismiss: () -> Unit
) {
    val isCritical = alert.severity == "critical"
    val accentColor = if (isCritical) Color(0xFFEF4444) else Color(0xFFF59E0B)
    val cardBg = if (isDark) Color(0xFF0F172A).copy(alpha = 0.98f) else Color.White.copy(alpha = 0.98f)
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    // Pulse & Shake Animation for the warning icon & glowing aura
    val infiniteTransition = rememberInfiniteTransition(label = "alert_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val shakeAngle by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val maxCardHeight = (LocalConfiguration.current.screenHeightDp * 0.8f).dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxCardHeight)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = accentColor,
                spotColor = accentColor
            )
            .clip(RoundedCornerShape(26.dp))
            .background(cardBg)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        accentColor.copy(alpha = glowAlpha),
                        accentColor.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header: pulsing icon + badge + close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .scale(pulseScale)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape)
                        .border(1.5.dp, accentColor.copy(alpha = glowAlpha), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (alert.isIncreasing) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                        contentDescription = "Warning",
                        tint = accentColor,
                        modifier = Modifier
                            .size(30.dp)
                            .rotate(shakeAngle)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Box(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isCritical) "🚨 CRITICAL LIMIT" else "⚠ THRESHOLD ALERT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Dismiss",
                        tint = subTextColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = alert.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                lineHeight = 28.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Current reading, large and clear
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(accentColor.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("YOUR READING", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = subTextColor, letterSpacing = 0.5.sp)
                    Text(alert.currentValue, fontSize = 26.sp, fontWeight = FontWeight.Black, color = accentColor)
                }
                Text(
                    text = alert.threshold,
                    fontSize = 13.sp,
                    color = subTextColor,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Remedy as numbered steps
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isDark) Color(0xFF1E293B).copy(alpha = 0.8f) else Color(0xFFFFFBEB))
                    .border(
                        1.dp,
                        if (isDark) Color(0xFFF59E0B).copy(alpha = 0.35f) else Color(0xFFFDE68A),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier
                            .size(26.dp)
                            .scale(pulseScale)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WHAT TO DO NOW",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                remedySteps(alert.remedy).forEachIndexed { i, step ->
                    Row(
                        modifier = Modifier.padding(bottom = 10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(Color(0xFFF59E0B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${i + 1}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = step,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF451A03),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onListenTts,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTtsSpeaking) Color(0xFFEF4444) else accentColor
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(
                        imageVector = if (isTtsSpeaking) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isTtsSpeaking) "Stop" else "Listen",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (alert.parameterName.contains("Pulse", ignoreCase = true) ||
                    alert.parameterName.contains("Heart", ignoreCase = true) ||
                    alert.parameterName.contains("Stress", ignoreCase = true)
                ) {
                    Button(
                        onClick = onBreathing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF0891B2) else Color(0xFF0284C7)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Calm Breathing", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("I'm OK, dismiss", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = accentColor)
            }
        }
    }
}

@Composable
fun FloatingBottomNavigation(
    currentTab: Int,
    isDark: Boolean,
    onTabSelected: (Int) -> Unit
) {
    val navItems = listOf(
        Pair("Home", Icons.Rounded.Home),
        Pair("Flow", Icons.Rounded.Leaderboard), // Bar chart / analytics
        Pair("Scan", Icons.Rounded.Fingerprint), // TinyML scanner
        Pair("Insight", Icons.Rounded.AutoAwesome) // Calm recommendations
    )

    val barBackground = if (isDark) {
        Color(0xFF0F172A).copy(alpha = 0.90f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.96f)
    }

    val barBorderBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.15f), Color(0xFF06B6D4).copy(alpha = 0.2f))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFFCBD5E1), Color(0xFF0284C7).copy(alpha = 0.25f))
        )
    }

    val activeColor = if (isDark) Color(0xFF06B6D4) else Color(0xFF0284C7)
    val inactiveColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)

    // Floating Glassmorphic bar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(barBackground)
                .border(
                    width = 1.dp,
                    brush = barBorderBrush,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 8.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, (label, icon) ->
                val isSelected = currentTab == index
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent
                        )
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) activeColor else inactiveColor,
                            modifier = Modifier.size(22.dp)
                        )
                        
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                color = activeColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
