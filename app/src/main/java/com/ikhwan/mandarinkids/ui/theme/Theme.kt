package com.ikhwan.mandarinkids.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary                = ColorPrimary,
    onPrimary              = ColorOnPrimary,
    primaryContainer       = ColorPrimaryContainer,
    onPrimaryContainer     = ColorOnPrimaryContainer,
    secondary              = ColorSecondary,
    onSecondary            = ColorOnSecondary,
    secondaryContainer     = ColorSecondaryContainer,
    onSecondaryContainer   = ColorOnSecondaryContainer,
    tertiary               = ColorTertiary,
    onTertiary             = ColorOnTertiary,
    tertiaryContainer      = ColorTertiaryContainer,
    onTertiaryContainer    = ColorOnTertiaryContainer,
    error                  = ColorError,
    onError                = ColorOnError,
    background             = AppBackground,
    onBackground           = ColorOnSurface,
    surface                = AppSurface,
    onSurface              = ColorOnSurface,
    surfaceVariant         = ColorSurfaceVariant,
    onSurfaceVariant       = ColorOnSurfaceVariant,
    outline                = ColorOutline,
    outlineVariant         = ColorOutlineVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary                = DarkColorPrimary,
    onPrimary              = DarkColorOnPrimary,
    primaryContainer       = DarkColorPrimaryContainer,
    onPrimaryContainer     = DarkColorOnPrimaryContainer,
    secondary              = DarkColorSecondary,
    onSecondary            = DarkColorOnSecondary,
    secondaryContainer     = DarkColorSecondaryContainer,
    onSecondaryContainer   = DarkColorOnSecondaryContainer,
    tertiary               = DarkColorTertiary,
    onTertiary             = DarkColorOnTertiary,
    tertiaryContainer      = DarkColorTertiaryContainer,
    onTertiaryContainer    = DarkColorOnTertiaryContainer,
    error                  = ColorError,
    onError                = ColorOnError,
    background             = DarkBackground,
    onBackground           = DarkColorOnSurface,
    surface                = DarkSurface,
    onSurface              = DarkColorOnSurface,
    surfaceVariant         = DarkColorSurfaceVariant,
    onSurfaceVariant       = DarkColorOnSurfaceVariant,
    outline                = DarkColorOutline,
    outlineVariant         = DarkColorOutlineVariant,
)

@Composable
fun MandarinKidsTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = Typography,
        content     = content
    )
}
