package com.example.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = darkColorScheme(
  primary = PureWhite,
  onPrimary = PureBlack,
  primaryContainer = DarkCard,
  onPrimaryContainer = PureWhite,
  secondary = PureWhite,
  onSecondary = PureBlack,
  secondaryContainer = DarkSurface,
  onSecondaryContainer = PureWhite,
  tertiary = PureWhite,
  onTertiary = PureBlack,
  background = PureBlack,
  onBackground = PureWhite,
  surface = PureBlack,
  onSurface = PureWhite,
  surfaceVariant = DarkCard,
  onSurfaceVariant = PureWhite,
  surfaceContainer = DarkCard,
  surfaceContainerHigh = DarkSurface,
  surfaceContainerHighest = DarkCard,
  surfaceContainerLow = PureBlack,
  surfaceContainerLowest = PureBlack,
  outline = DarkBorder,
  outlineVariant = DarkBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit
) {
  CompositionLocalProvider(LocalContentColor provides PureWhite) {
    MaterialTheme(
      colorScheme = DarkColorScheme,
      typography = Typography,
      content = content
    )
  }
}
