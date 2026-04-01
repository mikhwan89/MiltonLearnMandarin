package com.ikhwan.mandarinkids

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.navigation.MandarinKidsApp
import com.ikhwan.mandarinkids.preferences.UserPreferencesRepository
import com.ikhwan.mandarinkids.ui.theme.AppThemes
import com.ikhwan.mandarinkids.ui.theme.MandarinKidsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JsonScenarioRepository.init(applicationContext)
        setContent {
            val userPrefs = remember { UserPreferencesRepository.getInstance(this) }
            val themeIndex by userPrefs.colorThemeIndex.collectAsState(initial = 0)
            val variant = AppThemes[themeIndex.coerceIn(0, AppThemes.lastIndex)]

            // Status bar + navigation bar colours follow the active theme
            val view = LocalView.current
            SideEffect {
                val window = (view.context as Activity).window
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                val bgColor = variant.md3.background.toArgb()
                // Status bar
                insetsController.isAppearanceLightStatusBars = !variant.isDark
                // Navigation bar — match theme background, flip button icons accordingly
                window.navigationBarColor = bgColor
                insetsController.isAppearanceLightNavigationBars = !variant.isDark
            }

            MandarinKidsTheme(variant = variant) {
                MandarinKidsApp()
            }
        }
    }
}
