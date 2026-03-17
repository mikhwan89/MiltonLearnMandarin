package com.ikhwan.mandarinkids.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ikhwan.mandarinkids.FlashcardScreen
import com.ikhwan.mandarinkids.QuizScreen
import com.ikhwan.mandarinkids.RolePlayScreen
import com.ikhwan.mandarinkids.data.models.ScenarioCategory
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.home.HomeScreen
import com.ikhwan.mandarinkids.home.ScenarioListScreen
import com.ikhwan.mandarinkids.parent.ParentDashboardScreen
import com.ikhwan.mandarinkids.practice.PracticeScreen

@Composable
fun MandarinKidsApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        ProgressRepository.getInstance(context).checkAndUpdateStreak()
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onCategoryClick = { category ->
                    navController.navigate(Routes.category(category.name))
                },
                onPracticeClick = { navController.navigate(Routes.PRACTICE) },
                onParentClick = { navController.navigate(Routes.PARENT_DASHBOARD) }
            )
        }

        composable(Routes.PRACTICE) {
            PracticeScreen(onBack = { navController.popBackStack() })
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
