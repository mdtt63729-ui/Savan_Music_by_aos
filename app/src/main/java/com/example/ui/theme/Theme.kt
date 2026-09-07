package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val CleanMinimalLightColorScheme =
  lightColorScheme(
    primary = CleanMinimalPrimary,
    onPrimary = Color.White,
    primaryContainer = CleanMinimalPrimaryContainer,
    onPrimaryContainer = CleanMinimalOnPrimaryContainer,
    secondary = CleanMinimalSecondary,
    onSecondary = Color.White,
    secondaryContainer = CleanMinimalSecondaryContainer,
    onSecondaryContainer = CleanMinimalOnSecondaryContainer,
    background = CleanMinimalBackground,
    onBackground = CleanMinimalTextPrimary,
    surface = CleanMinimalSurface,
    onSurface = CleanMinimalTextPrimary,
    surfaceVariant = CleanMinimalBorder,
    onSurfaceVariant = CleanMinimalTextSecondary,
    surfaceContainer = CleanMinimalSurfaceContainer,
    surfaceContainerHigh = CleanMinimalSurfaceContainerHigh,
    surfaceContainerLowest = CleanMinimalSurfaceContainerLowest,
    outline = CleanMinimalBorderSubtle,
    outlineVariant = CleanMinimalBorder
  )

private val CleanMinimalDarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E0E9),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E0E9),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceContainer = Color(0xFF211F26),
    surfaceContainerHigh = Color(0xFF2B2930),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> CleanMinimalDarkColorScheme
      else -> CleanMinimalLightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

