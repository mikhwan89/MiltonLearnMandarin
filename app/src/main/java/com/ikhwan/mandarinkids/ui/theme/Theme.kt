package com.ikhwan.mandarinkids.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun MandarinKidsTheme(
    variant: AppThemeVariant = AppThemes[0],
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppColors provides variant.palette) {
        MaterialTheme(
            colorScheme = variant.md3,
            typography  = Typography,
            content     = content
        )
    }
}
