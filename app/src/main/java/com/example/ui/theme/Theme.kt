package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ColorPrimary,
    onPrimary = ColorOnPrimary,
    primaryContainer = ColorPrimaryContainer,
    onPrimaryContainer = ColorOnPrimaryContainer,
    secondary = ColorSecondary,
    onSecondary = ColorOnSecondary,
    secondaryContainer = ColorSecondaryContainer,
    onSecondaryContainer = ColorOnSecondaryContainer,
    tertiary = ColorTertiary,
    onTertiary = ColorOnTertiary,
    tertiaryContainer = ColorTertiaryContainer,
    onTertiaryContainer = ColorOnTertiaryContainer,
    background = ColorBackground,
    onBackground = ColorOnBackground,
    surface = ColorSurface,
    onSurface = ColorOnSurface,
    surfaceVariant = ColorSurfaceVariant,
    onSurfaceVariant = ColorOnSurfaceVariant,
    outline = ColorOutline,
    outlineVariant = ColorOutlineVariant,
    error = ColorError,
    onError = ColorOnError,
    errorContainer = ColorErrorContainer,
    onErrorContainer = ColorOnErrorContainer
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LightColorPrimary,
    onPrimary = LightColorOnPrimary,
    primaryContainer = LightColorPrimaryContainer,
    onPrimaryContainer = LightColorOnPrimaryContainer,
    secondary = LightColorSecondary,
    onSecondary = LightColorOnSecondary,
    secondaryContainer = LightColorSecondaryContainer,
    onSecondaryContainer = LightColorOnSecondaryContainer,
    tertiary = LightColorTertiary,
    onTertiary = LightColorOnTertiary,
    tertiaryContainer = LightColorTertiaryContainer,
    onTertiaryContainer = LightColorOnTertiaryContainer,
    background = LightColorBackground,
    onBackground = LightColorOnBackground,
    surface = LightColorSurface,
    onSurface = LightColorOnSurface,
    surfaceVariant = LightColorSurfaceVariant,
    onSurfaceVariant = LightColorOnSurfaceVariant,
    outline = LightColorOutline,
    outlineVariant = LightColorOutlineVariant,
    error = LightColorError,
    onError = LightColorOnError,
    errorContainer = LightColorErrorContainer,
    onErrorContainer = LightColorOnErrorContainer
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Configurable light/dark theme (defaults to clean light theme)
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
