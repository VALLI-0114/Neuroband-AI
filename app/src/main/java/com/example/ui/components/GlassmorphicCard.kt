package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    glowColor: Color = Color(0x22AF0A78),
    borderAlpha: Float = 0.35f,
    backgroundColors: List<Color>? = null,
    content: @Composable BoxScope.() -> Unit
) {
    // Elegant glow shadow draw behind
    val shadowModifier = modifier.drawBehind {
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                style = PaintingStyle.Fill
                color = glowColor
            }
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(
                24.dp.toPx(),
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
            val shadowColor = glowColor.toArgb()
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

    val defaultBgColors = listOf(
        Color(0xE50F172A),  // 90% opacity Slate 900 (Sleek deep navy-slate)
        Color(0xCC090D16)   // 80% opacity Dark Navy
    )

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
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = borderAlpha * 0.35f),
                            Color(0xFF06B6D4).copy(alpha = borderAlpha * 0.25f), // cyan edge
                            Color(0xFF8B5CF6).copy(alpha = borderAlpha * 0.1f)   // purple trace
                        )
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(20.dp)
    ) {
        content()
    }
}
