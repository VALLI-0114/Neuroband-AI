package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun VaporwaveBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    // Animate color gradient offsets for dynamic WebGL-like pulsing effect
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

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Base deep navy background
            drawRect(color = Color(0xFF0B1120))

            // Pulse glowing blob 1: Cyan (#06B6D4)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF06B6D4).copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                radius = width * 0.7f,
                center = Offset(width * xPulse1, height * yPulse1)
            )

            // Pulse glowing blob 2: Purple (#8B5CF6)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = 0.18f),
                        Color.Transparent
                    )
                ),
                radius = width * 0.8f,
                center = Offset(width * xPulse2, height * yPulse2)
            )

            // Dynamic Blue blob 3: (#3B82F6)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF3B82F6).copy(alpha = 0.12f),
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
