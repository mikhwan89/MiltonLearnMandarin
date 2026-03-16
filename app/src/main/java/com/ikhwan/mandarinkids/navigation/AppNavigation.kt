package com.ikhwan.mandarinkids.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.ikhwan.mandarinkids.FlashcardScreen
import com.ikhwan.mandarinkids.ProgressManager
import com.ikhwan.mandarinkids.QuizScreen
import com.ikhwan.mandarinkids.RolePlayScreen
import com.ikhwan.mandarinkids.home.HomeScreen

@Composable
fun MandarinKidsApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val context = LocalContext.current

    // Update daily streak once on launch
    LaunchedEffect(Unit) {
        ProgressManager.checkAndUpdateStreak(context)
    }

    // Handle back button
    BackHandler(enabled = currentScreen != Screen.Home) {
        currentScreen = Screen.Home
    }

    when (currentScreen) {
        Screen.Home -> HomeScreen(
            onScenarioClick = { scenario ->
                currentScreen = Screen.Flashcard(scenario)
            }
        )
        is Screen.Flashcard -> FlashcardScreen(
            scenario = (currentScreen as Screen.Flashcard).scenario,
            onComplete = {
                currentScreen = Screen.RolePlay((currentScreen as Screen.Flashcard).scenario)
            },
            onBack = { currentScreen = Screen.Home }
        )
        is Screen.RolePlay -> RolePlayScreen(
            scenario = (currentScreen as Screen.RolePlay).scenario,
            onComplete = { score ->
                currentScreen = Screen.Quiz((currentScreen as Screen.RolePlay).scenario, score)
            },
            onBack = { currentScreen = Screen.Home }
        )
        is Screen.Quiz -> QuizScreen(
            scenario = (currentScreen as Screen.Quiz).scenario,
            rolePlayScore = (currentScreen as Screen.Quiz).rolePlayScore,
            onComplete = { currentScreen = Screen.Home },
            onBack = { currentScreen = Screen.Home },
            onTryAgain = {
                currentScreen = Screen.Flashcard((currentScreen as Screen.Quiz).scenario)
            }
        )
    }
}
