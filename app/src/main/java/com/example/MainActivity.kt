package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BiometricViewModel
import com.example.ui.components.VaporwaveBackground
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val vm: BiometricViewModel = viewModel()
                
                VaporwaveBackground {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent, // fully transclucent over background
                        bottomBar = {
                            FloatingBottomNavigation(
                                currentTab = vm.currentTab,
                                onTabSelected = { vm.selectTab(it) }
                            )
                        }
                    ) { innerPadding ->
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
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingBottomNavigation(
    currentTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val navItems = listOf(
        Pair("Home", Icons.Rounded.Home),
        Pair("Flow", Icons.Rounded.Leaderboard), // Bar chart / analytics
        Pair("Scan", Icons.Rounded.Fingerprint), // TinyML scanner
        Pair("Insight", Icons.Rounded.AutoAwesome) // Calm recommendations
    )

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
                .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color(0xFF06B6D4).copy(alpha = 0.15f))
                    ),
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
                            if (isSelected) Color(0xFF06B6D4).copy(alpha = 0.18f) else Color.Transparent
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
                            tint = if (isSelected) Color(0xFF06B6D4) else Color(0xFF64748B),
                            modifier = Modifier.size(22.dp)
                        )
                        
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                color = Color(0xFF06B6D4),
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
