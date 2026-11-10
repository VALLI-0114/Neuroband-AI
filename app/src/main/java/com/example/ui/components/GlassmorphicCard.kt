package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ColorBackground

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    glowColor: Color? = null,
    borderAlpha: Float = 0.35f,
    backgroundColors: List<Color>? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val currentBg = MaterialTheme.colorScheme.background
    val isDark = currentBg == ColorBackground

    val resolvedGlowColor = glowColor ?: if (isDark) Color(0x22AF0A78) else Color(0x140F172A)

    val shadowModifier = modifier.drawBehind {
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                style = PaintingStyle.Fill
                color = resolvedGlowColor
            }
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(
                24.dp.toPx(),
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
            val shadowColor = resolvedGlowColor.toArgb()
            frameworkPaint.color = shadowColor
            
            canvas.drawRoundRect(
                left = 6.dp.toPx(),
                top = 10.dp.toPx(),
                right = size.width - 6.dp.toPx(),
                bottom = size.height + 4.dp.toPx(),
                radiusX = cornerRadius.toPx(),
                radiusY = cornerRadius.toPx(),
                paint = paint
            )
        }
    }

    val defaultBgColors = if (isDark) {
        listOf(
            Color(0xE50F172A),  // 90% opacity Slate 900
            Color(0xCC090D16)   // 80% opacity Dark Navy
        )
    } else {
        listOf(
            Color(0xF8FFFFFF),  // Pure white card
            Color(0xF0F8FAFC)   // Slate 50 light surface
        )
    }

    val borderColors = if (isDark) {
        listOf(
            Color.White.copy(alpha = borderAlpha * 0.35f),
            Color(0xFF06B6D4).copy(alpha = borderAlpha * 0.25f),
            Color(0xFF8B5CF6).copy(alpha = borderAlpha * 0.1f)
        )
    } else {
        listOf(
            Color(0xFF0F172A).copy(alpha = 0.08f),
            Color(0xFF0284C7).copy(alpha = 0.20f),
            Color(0xFF7C3AED).copy(alpha = 0.12f)
        )
    }

    Box(
        modifier = shadowModifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = backgroundColors ?: defaultBgColors
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(colors = borderColors)
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(20.dp)
    ) {
        content()
    }
}
