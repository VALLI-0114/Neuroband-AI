package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ColorBackground

@Composable
fun VaporwaveBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    // Animate color gradient offsets for dynamic ambient pulsing effect
    val xPulse1 by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x1"
    )
    val yPulse1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y1"
    )
    
    val xPulse2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x2"
    )
    val yPulse2 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y2"
    )

    val currentBg = MaterialTheme.colorScheme.background
    val isDark = currentBg == ColorBackground
    val baseBgColor = if (isDark) Color(0xFF0B1120) else Color(0xFFF8FAFC)
    val blob1Color = if (isDark) Color(0xFF06B6D4).copy(alpha = 0.15f) else Color(0xFF0284C7).copy(alpha = 0.08f)
    val blob2Color = if (isDark) Color(0xFF8B5CF6).copy(alpha = 0.18f) else Color(0xFF7C3AED).copy(alpha = 0.07f)
    val blob3Color = if (isDark) Color(0xFF3B82F6).copy(alpha = 0.12f) else Color(0xFF38BDF8).copy(alpha = 0.06f)

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Base background
            drawRect(color = baseBgColor)

            // Pulse glowing blob 1: Cyan / Sky Blue
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        blob1Color,
                        Color.Transparent
                    )
                ),
                radius = width * 0.7f,
                center = Offset(width * xPulse1, height * yPulse1)
            )

            // Pulse glowing blob 2: Purple / Violet
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        blob2Color,
                        Color.Transparent
                    )
                ),
                radius = width * 0.8f,
                center = Offset(width * xPulse2, height * yPulse2)
            )

            // Dynamic Blue blob 3
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        blob3Color,
                        Color.Transparent
                    )
                ),
                radius = width * 0.6f,
                center = Offset(width * (1f - xPulse1), height * (0.5f + yPulse1 * 0.3f))
            )
        }
        
        content()
    }
}
