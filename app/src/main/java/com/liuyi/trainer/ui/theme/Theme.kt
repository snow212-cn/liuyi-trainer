package com.liuyi.trainer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PrisonColors = darkColorScheme(
    primary = Clay,
    onPrimary = Chalk,
    primaryContainer = Bronze,
    onPrimaryContainer = Chalk,
    secondary = Ember,
    onSecondary = Iron,
    secondaryContainer = ColorPalettePanel,
    onSecondaryContainer = Chalk,
    background = Asphalt,
    onBackground = Sand,
    surface = ColorPaletteSurface,
    onSurface = Sand,
    surfaceVariant = ColorPalettePanel,
    onSurfaceVariant = Smoke,
    outline = Line,
    surfaceTint = Clay,
)

@Composable
fun LiuyiTrainerTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = PrisonColors,
        typography = LiuyiTypography,
        content = content,
    )
}

