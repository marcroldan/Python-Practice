package com.marcroldan.rimemba.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = RimembaPrimary,
    onPrimary = RimembaOnPrimary,
    secondary = RimembaSecondary,
    background = RimembaBackgroundLight,
    surface = RimembaSurfaceLight,
    error = RimembaError
)

private val DarkColors = darkColorScheme(
    primary = RimembaPrimary,
    onPrimary = RimembaOnPrimary,
    secondary = RimembaSecondary,
    background = RimembaBackgroundDark,
    surface = RimembaSurfaceDark,
    error = RimembaError
)

@Composable
fun RimembaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = RimembaTypography,
        content = content
    )
}
