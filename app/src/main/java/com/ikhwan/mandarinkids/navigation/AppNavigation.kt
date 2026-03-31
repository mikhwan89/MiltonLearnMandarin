package com.ikhwan.mandarinkids.navigation

import androidx.compose.foundation.Image

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ikhwan.mandarinkids.R
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ikhwan.mandarinkids.FlashcardScreen
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import com.ikhwan.mandarinkids.onboarding.InteractiveOnboardingOverlay
import com.ikhwan.mandarinkids.onboarding.LocalOnboardingCoords
import com.ikhwan.mandarinkids.onboarding.OnboardingKey
import kotlinx.coroutines.launch
import com.ikhwan.mandarinkids.QuizScreen
import com.ikhwan.mandarinkids.RolePlayScreen
import com.ikhwan.mandarinkids.data.models.ScenarioCategory
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.home.HomeScreen
import com.ikhwan.mandarinkids.preferences.UserPreferencesRepository
import com.ikhwan.mandarinkids.home.ProgressScreen
import com.ikhwan.mandarinkids.home.ScenarioListScreen
import com.ikhwan.mandarinkids.parent.ParentDashboardScreen
import com.ikhwan.mandarinkids.parent.PinScreen
import com.ikhwan.mandarinkids.parent.PinMode
import com.ikhwan.mandarinkids.practice.PracticeScreen
import com.ikhwan.mandarinkids.practice.SentenceBuilderScreen
import com.ikhwan.mandarinkids.practice.ToneTrainerScreen

@Composable
fun MandarinKidsApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Track whether the word-of-day popup has been shown this session
    var wordOfDayShownThisSession by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val userPrefs = remember { UserPreferencesRepository.getInstance(context) }
    val onboardingCompleted by userPrefs.onboardingCompleted.collectAsState(initial = null)
    val disabledTabs by userPrefs.disabledTabs.collectAsState(initial = emptySet())

    LaunchedEffect(Unit) {
        ProgressRepository.getInstance(context).checkAndUpdateStreak()
    }

    // Determine current route to decide whether to show the bottom nav
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val topLevelRoutes = remember(disabledTabs) {
        buildSet {
            if ("roleplay" !in disabledTabs) add(Routes.HOME)
            if ("flashcard" !in disabledTabs) add(Routes.PRACTICE)
            if ("tone" !in disabledTabs) add(Routes.TONE_TRAINER)
            if ("build" !in disabledTabs) add(Routes.SENTENCE_BUILDER)
            add(Routes.PROGRESS)
        }
    }

    // Shared element-bounds map for the interactive onboarding tour
    val onboardingCoords = remember { mutableStateMapOf<String, Rect>() }

    // Shared icon colors — preserve original PNG colors, just dim when unselected
    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor   = Color.Unspecified,
        unselectedIconColor = Color.Unspecified,
        selectedTextColor   = MaterialTheme.colorScheme.primary,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        indicatorColor      = MaterialTheme.colorScheme.primaryContainer,
    )

    CompositionLocalProvider(LocalOnboardingCoords provides onboardingCoords) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // ── Bamboo accent pattern layered over warm almond background ─────────
        Image(
            painter      = painterResource(R.drawable.background_accent),
            contentDescription = null,
            modifier     = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha        = 0.50f
        )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.onGloballyPositioned { lc ->
                        onboardingCoords[OnboardingKey.NAV_BAR] = lc.boundsInRoot()
                    }
                ) {
                    if ("roleplay" !in disabledTabs) {
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.nav_roleplay),
                                    contentDescription = "Roleplay",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            label = { Text("Roleplay") },
                            selected = currentRoute == Routes.HOME,
                            colors = navItemColors,
                            onClick = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.HOME) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    if ("flashcard" !in disabledTabs) {
                        NavigationBarItem(
                            modifier = Modifier.onGloballyPositioned { lc ->
                                onboardingCoords[OnboardingKey.NAV_FLASHCARD] = lc.boundsInRoot()
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.nav_flashcard),
                                    contentDescription = "Flashcard",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            label = { Text("Flashcard") },
                            selected = currentRoute == Routes.PRACTICE,
                            colors = navItemColors,
                            onClick = {
                                navController.navigate(Routes.PRACTICE) {
                                    popUpTo(Routes.HOME)
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    if ("tone" !in disabledTabs) {
                        NavigationBarItem(
                            modifier = Modifier.onGloballyPositioned { lc ->
                                onboardingCoords[OnboardingKey.NAV_TONE] = lc.boundsInRoot()
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.nav_tone),
                                    contentDescription = "Tones",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            label = { Text("Tones") },
                            selected = currentRoute == Routes.TONE_TRAINER,
                            colors = navItemColors,
                            onClick = {
                                navController.navigate(Routes.TONE_TRAINER) {
                                    popUpTo(Routes.HOME)
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    if ("build" !in disabledTabs) {
                        NavigationBarItem(
                            modifier = Modifier.onGloballyPositioned { lc ->
                                onboardingCoords[OnboardingKey.NAV_BUILD] = lc.boundsInRoot()
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.nav_build),
                                    contentDescription = "Build",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            label = { Text("Build") },
                            selected = currentRoute == Routes.SENTENCE_BUILDER,
                            colors = navItemColors,
                            onClick = {
                                navController.navigate(Routes.SENTENCE_BUILDER) {
                                    popUpTo(Routes.HOME)
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    NavigationBarItem(
                        modifier = Modifier.onGloballyPositioned { lc ->
                            onboardingCoords[OnboardingKey.NAV_PROGRESS] = lc.boundsInRoot()
                        },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.nav_progress),
                                contentDescription = "Progress",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        label = { Text("Progress") },
                        selected = currentRoute == Routes.PROGRESS,
                        colors = navItemColors,
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
                    showWordOfDayOnLaunch = !wordOfDayShownThisSession && onboardingCompleted == true,
                    onWordOfDayShown = { wordOfDayShownThisSession = true }
                )
            }

            composable(Routes.PRACTICE) {
                PracticeScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.TONE_TRAINER) {
                ToneTrainerScreen()
            }

            composable(Routes.SENTENCE_BUILDER) {
                SentenceBuilderScreen()
            }

            composable(Routes.PROGRESS) {
                ProgressScreen(
                    navController = navController,
                    onParentClick = { navController.navigate(Routes.PIN) }
                )
            }

            composable(Routes.PIN) {
                val repo = remember { ProgressRepository.getInstance(context) }
                PinScreen(
                    mode = if (repo.isPinSet()) PinMode.VERIFY else PinMode.SET,
                    onSuccess = {
                        navController.navigate(Routes.PARENT_DASHBOARD) {
                            popUpTo(Routes.PIN) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onVerify = { pin -> repo.verifyPin(pin) },
                    onSetPin = { pin -> repo.setPin(pin) }
                )
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
        // ── Interactive onboarding tour overlay ───────────────────────────
        // Shown on top of everything until the user completes or skips it.
        // onboardingCompleted == null while DataStore is loading (show nothing).
        if (onboardingCompleted == false) {
            InteractiveOnboardingOverlay(
                coords            = onboardingCoords,
                onNavigateToRoute = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.HOME)
                        launchSingleTop = true
                    }
                },
                onComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                    scope.launch { userPrefs.saveOnboardingCompleted(true) }
                }
            )
        }
    } // end Surface
    } // end CompositionLocalProvider
}
