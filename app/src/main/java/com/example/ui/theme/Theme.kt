package com.example.ui.theme

import androidx.compose.ui.graphics.Color
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
    primary = Color(0xFFD0BCFF),        // Soft light purple
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    background = Color(0xFF141218),     // Dark geometric slate
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF6750A4),        // M3 deep standard purple
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF), // Beautiful active light selection
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),      // Secondary soft gray-purple
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFFD0BCFF),       // Light highlight purple / FAB
    onTertiary = Color(0xFF381E72),
    background = Color(0xFFFDF8FD),     // Authentic Geometric theme beige-white
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFDF8FD),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF3EDF7), // M3 Surface Variant
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFFCAC4D0),        // Borders
    outlineVariant = Color(0xFFE6E0E9), // Softer borders
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to strictly enforce Geometric Balance colors
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
