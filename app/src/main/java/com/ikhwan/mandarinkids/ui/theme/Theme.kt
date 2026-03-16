package com.ikhwan.mandarinkids.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary                = OrangePrimary,
    onPrimary              = OrangeOnPrimary,
    primaryContainer       = OrangePrimaryContainer,
    onPrimaryContainer     = OrangeOnPrimaryContainer,
    secondary              = TealSecondary,
    onSecondary            = TealOnSecondary,
    secondaryContainer     = TealSecondaryContainer,
    onSecondaryContainer   = TealOnSecondaryContainer,
    tertiary               = AmberTertiary,
    onTertiary             = AmberOnTertiary,
    tertiaryContainer      = AmberTertiaryContainer,
    onTertiaryContainer    = AmberOnTertiaryContainer,
    background             = LightBackground,
    onBackground           = LightOnBackground,
    surface                = LightSurface,
    onSurface              = LightOnSurface,
    surfaceVariant         = LightSurfaceVariant,
    onSurfaceVariant       = LightOnSurfaceVariant,
    outline                = LightOutline,
)

private val DarkColorScheme = darkColorScheme(
    primary                = OrangePrimaryDark,
    onPrimary              = OrangeOnPrimaryDark,
    primaryContainer       = OrangePrimaryContainerDark,
    onPrimaryContainer     = OrangeOnPrimaryContainerDark,
    secondary              = TealSecondaryDark,
    onSecondary            = TealOnSecondaryDark,
    secondaryContainer     = TealSecondaryContainerDark,
    onSecondaryContainer   = TealOnSecondaryContainerDark,
    tertiary               = AmberTertiaryDark,
    onTertiary             = AmberOnTertiaryDark,
    tertiaryContainer      = AmberTertiaryContainerDark,
    onTertiaryContainer    = AmberOnTertiaryContainerDark,
    background             = DarkBackground,
    onBackground           = DarkOnBackground,
    surface                = DarkSurface,
    onSurface              = DarkOnSurface,
    surfaceVariant         = DarkSurfaceVariant,
    onSurfaceVariant       = DarkOnSurfaceVariant,
    outline                = DarkOutline,
)

@Composable
fun MandarinKidsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dynamic color intentionally disabled — we use our own child-friendly palette.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
