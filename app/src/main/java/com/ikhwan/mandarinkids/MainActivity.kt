package com.ikhwan.mandarinkids

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.navigation.MandarinKidsApp
import com.ikhwan.mandarinkids.ui.theme.MandarinKidsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Light background — use dark status bar icons so clock/battery/signal are readable
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        JsonScenarioRepository.init(applicationContext)
        setContent {
            MandarinKidsTheme {
                MandarinKidsApp()
            }
        }
    }
}
