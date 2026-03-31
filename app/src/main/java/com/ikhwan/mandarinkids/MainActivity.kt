package com.ikhwan.mandarinkids

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.navigation.MandarinKidsApp
import com.ikhwan.mandarinkids.preferences.UserPreferencesRepository
import com.ikhwan.mandarinkids.ui.theme.MandarinKidsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JsonScenarioRepository.init(applicationContext)
        setContent {
            val userPrefs = remember { UserPreferencesRepository.getInstance(this) }

            // Always light mode — force dark status bar icons
            val view = LocalView.current
            SideEffect {
                val window = (view.context as Activity).window
                WindowCompat.getInsetsController(window, window.decorView)
                    .isAppearanceLightStatusBars = true
            }

            MandarinKidsTheme(darkTheme = false) {
                MandarinKidsApp()
            }
        }
    }
}
