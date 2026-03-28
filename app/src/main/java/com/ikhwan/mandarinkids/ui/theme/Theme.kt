package com.ikhwan.mandarinkids.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ColorPrimary,
    onPrimary = ColorOnPrimary,
    primaryContainer = ColorPrimaryContainer,
    onPrimaryContainer = ColorOnPrimaryContainer,
    secondary = ColorSecondary,
    onSecondary = ColorOnSecondary,
    secondaryContainer = ColorSecondaryContainer,
    onSecondaryContainer = ColorOnSecondaryContainer,
    tertiary = ColorTertiary,
    onTertiary = ColorOnTertiary,
    tertiaryContainer = ColorTertiaryContainer,
    onTertiaryContainer = ColorOnTertiaryContainer,
    error = ColorError,
    onError = ColorOnError,
    background = AppBackground,
    surface = AppSurface,
    surfaceVariant = ColorSurfaceVariant,
    onSurfaceVariant = ColorOnSurfaceVariant,
    outline = ColorOutline
)

@Composable
fun MandarinKidsTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
