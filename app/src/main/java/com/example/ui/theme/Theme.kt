package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CobaltContainer,
    onPrimary = CobaltPrimary,
    primaryContainer = CobaltPrimary,
    onPrimaryContainer = CobaltContainer,
    secondary = AmberYellow,
    onSecondary = Color.Black,
    secondaryContainer = AmberYellowContainer,
    onSecondaryContainer = AlertOnYellow,
    background = MidnightBlueDark,
    onBackground = Color.White,
    surface = CardSurfaceDark,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val LightColorScheme = lightColorScheme(
    primary = CobaltPrimary,
    onPrimary = Color.White,
    primaryContainer = CobaltContainer,
    onPrimaryContainer = AlertOnYellow,
    secondary = AmberYellow,
    onSecondary = Color.Black,
    secondaryContainer = AmberYellowContainer,
    onSecondaryContainer = AlertOnYellow,
    background = SurfaceBaseLight,
    onBackground = TextColOnSurface,
    surface = CardSurfaceLight,
    onSurface = TextColOnSurface,
    surfaceVariant = SurfaceContainerLow,
    onSurfaceVariant = TextColVariant
)

@Composable
fun PrecoNaBombaTheme(
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
