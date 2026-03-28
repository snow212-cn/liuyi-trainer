package com.liuyi.trainer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LightColors = darkColorScheme(
    primary = Clay,
    onPrimary = Chalk,
    primaryContainer = Bronze,
    onPrimaryContainer = Chalk,
    secondary = Bronze,
    onSecondary = Chalk,
    secondaryContainer = Smoke,
    onSecondaryContainer = Chalk,
    background = Asphalt,
    onBackground = Sand,
    surface = ColorPaletteSurface,
    onSurface = Sand,
    surfaceVariant = ColorPalettePanel,
    onSurfaceVariant = Sand.copy(alpha = 0.72f),
)

@Composable
fun LiuyiTrainerTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = LiuyiTypography,
        content = content,
    )
}
