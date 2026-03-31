package com.ikhwan.mandarinkids.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal carrying the current [AppColorScheme].
 * Defaults to [DefaultPalette]. Override via [androidx.compose.runtime.CompositionLocalProvider].
 */
val LocalAppColors = staticCompositionLocalOf<AppColorScheme> { DefaultPalette }

/**
 * Convenience accessor on MaterialTheme — use [MaterialTheme.appColors] in composables.
 */
val MaterialTheme.appColors: AppColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current
