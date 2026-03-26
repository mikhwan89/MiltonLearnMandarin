package com.ikhwan.mandarinkids.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ikhwan.mandarinkids.FlashcardScreen
import com.ikhwan.mandarinkids.QuizScreen
import com.ikhwan.mandarinkids.RolePlayScreen
import com.ikhwan.mandarinkids.data.models.ScenarioCategory
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.home.HomeScreen
import com.ikhwan.mandarinkids.home.ProgressScreen
import com.ikhwan.mandarinkids.home.ScenarioListScreen
import com.ikhwan.mandarinkids.parent.ParentDashboardScreen
import com.ikhwan.mandarinkids.practice.PracticeScreen
import com.ikhwan.mandarinkids.practice.SentenceBuilderScreen

@Composable
fun MandarinKidsApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Track whether the word-of-day popup has been shown this session
    var wordOfDayShownThisSession by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        ProgressRepository.getInstance(context).checkAndUpdateStreak()
    }

    // Determine current route to decide whether to show the bottom nav
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val topLevelRoutes = setOf(Routes.HOME, Routes.PRACTICE, Routes.SENTENCE_BUILDER, Routes.PROGRESS)

    Scaffold(
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.School, contentDescription = "Learn") },
                        label = { Text("Learn") },
                        selected = currentRoute == Routes.HOME,
                        onClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Style, contentDescription = "Flashcard") },
                        label = { Text("Flashcard") },
                        selected = currentRoute == Routes.PRACTICE,
                        onClick = {
                            navController.navigate(Routes.PRACTICE) {
                                popUpTo(Routes.HOME)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Extension, contentDescription = "Build") },
                        label = { Text("Build") },
                        selected = currentRoute == Routes.SENTENCE_BUILDER,
                        onClick = {
                            navController.navigate(Routes.SENTENCE_BUILDER) {
                                popUpTo(Routes.HOME)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Progress") },
                        label = { Text("Progress") },
                        selected = currentRoute == Routes.PROGRESS,
                        onClick = {
                            navController.navigate(Routes.PROGRESS) {
                                popUpTo(Routes.HOME)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {

            composable(Routes.HOME) {
                HomeScreen(
                    onCategoryClick = { category ->
                        navController.navigate(Routes.category(category.name))
                    },
                    showWordOfDayOnLaunch = !wordOfDayShownThisSession,
                    onWordOfDayShown = { wordOfDayShownThisSession = true }
                )
            }

            composable(Routes.PRACTICE) {
                PracticeScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.SENTENCE_BUILDER) {
                SentenceBuilderScreen()
            }

            composable(Routes.PROGRESS) {
                ProgressScreen(navController = navController)
            }

            composable(Routes.PARENT_DASHBOARD) {
                ParentDashboardScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Routes.CATEGORY,
                arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: return@composable
                val category = ScenarioCategory.entries.find { it.name == categoryName } ?: return@composable
                ScenarioListScreen(
                    category = category,
                    onScenarioClick = { scenario ->
                        navController.navigate(Routes.flashcard(scenario.id))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.FLASHCARD,
                arguments = listOf(navArgument("scenarioId") { type = NavType.StringType })
            ) { backStackEntry ->
                val scenarioId = backStackEntry.arguments?.getString("scenarioId") ?: return@composable
                val scenario = remember(scenarioId) { JsonScenarioRepository.getById(scenarioId) }
                    ?: return@composable
                FlashcardScreen(
                    scenario = scenario,
                    onComplete = { navController.navigate(Routes.roleplay(scenarioId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.ROLEPLAY,
                arguments = listOf(navArgument("scenarioId") { type = NavType.StringType })
            ) { backStackEntry ->
                val scenarioId = backStackEntry.arguments?.getString("scenarioId") ?: return@composable
                val scenario = remember(scenarioId) { JsonScenarioRepository.getById(scenarioId) }
                    ?: return@composable
                RolePlayScreen(
                    scenario = scenario,
                    onComplete = { score ->
                        navController.navigate(Routes.quiz(scenarioId, score))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.QUIZ,
                arguments = listOf(
                    navArgument("scenarioId") { type = NavType.StringType },
                    navArgument("rolePlayScore") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val scenarioId = backStackEntry.arguments?.getString("scenarioId") ?: return@composable
                val rolePlayScore = backStackEntry.arguments?.getInt("rolePlayScore") ?: 0
                val scenario = remember(scenarioId) { JsonScenarioRepository.getById(scenarioId) }
                    ?: return@composable
                QuizScreen(
                    scenario = scenario,
                    rolePlayScore = rolePlayScore,
                    onComplete = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onTryAgain = {
                        navController.navigate(Routes.flashcard(scenarioId)) {
                            popUpTo(Routes.HOME)
                        }
                    }
                )
            }
        }
    }
}
