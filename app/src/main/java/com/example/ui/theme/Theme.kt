package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
  primary = PureWhite,
  onPrimary = PureBlack,
  primaryContainer = DarkCard,
  onPrimaryContainer = PureWhite,
  secondary = DarkTextSecondary,
  onSecondary = PureWhite,
  background = PureBlack,
  onBackground = PureWhite,
  surface = DarkSurface,
  onSurface = PureWhite,
  surfaceVariant = DarkCard,
  onSurfaceVariant = PureWhite,
  outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
  primary = PureBlack,
  onPrimary = PureWhite,
  primaryContainer = LightSurface,
  onPrimaryContainer = PureBlack,
  secondary = LightTextSecondary,
  onSecondary = PureBlack,
  background = PureWhite,
  onBackground = PureBlack,
  surface = LightSurface,
  onSurface = PureBlack,
  surfaceVariant = LightCard,
  onSurfaceVariant = PureBlack,
  outline = LightBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
