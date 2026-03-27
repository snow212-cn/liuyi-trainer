package com.liuyi.trainer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Clay,
    onPrimary = Chalk,
    primaryContainer = Sand,
    onPrimaryContainer = Iron,
    secondary = Bronze,
    onSecondary = Chalk,
    secondaryContainer = Sage.copy(alpha = 0.18f),
    onSecondaryContainer = Iron,
    background = Chalk,
    onBackground = Iron,
    surface = Chalk,
    onSurface = Iron,
    surfaceVariant = Sand.copy(alpha = 0.65f),
    onSurfaceVariant = Smoke,
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

