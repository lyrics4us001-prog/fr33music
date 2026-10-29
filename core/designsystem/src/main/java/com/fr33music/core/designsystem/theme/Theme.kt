package com.fr33music.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ColorPrimary,
    onPrimary = ColorTextPrimary,
    primaryContainer = ColorPrimaryDark,
    onPrimaryContainer = ColorTextPrimary,
    background = ColorBackground,
    onBackground = ColorTextPrimary,
    surface = ColorSurface,
    onSurface = ColorTextPrimary,
    surfaceVariant = ColorSurfaceVariant,
    onSurfaceVariant = ColorTextSecondary,
    error = ColorError,
    onError = ColorTextPrimary
)

@Composable
fun Fr33MusicTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Fr33Typography,
        content = content
    )
}
