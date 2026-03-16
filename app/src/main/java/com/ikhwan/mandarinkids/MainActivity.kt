package com.ikhwan.mandarinkids

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikhwan.mandarinkids.data.scenarios.ScenarioRepository
import com.ikhwan.mandarinkids.data.models.Scenario


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MandarinKidsApp()
            }
        }
    }
}

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

sealed class Screen {
    object Home : Screen()
    data class Flashcard(val scenario: Scenario) : Screen()
    data class RolePlay(val scenario: Scenario) : Screen()
    data class Quiz(val scenario: Scenario, val rolePlayScore: Int) : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onScenarioClick: (Scenario) -> Unit) {
    val context = LocalContext.current
    val scenarios = remember { ScenarioRepository.getAllScenarios() }
    val xp = remember { ProgressManager.getTotalXp(context) }
    val streak = remember { ProgressManager.getStreak(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("学中文", fontSize = 24.sp)
                        Text(
                            "Learn Mandarin for School",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Progress summary card
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔥", fontSize = 28.sp)
                            Text(
                                "$streak",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "day streak",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier
                                .height(64.dp)
                                .width(1.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when {
                                    xp >= 180 -> "🌟"
                                    xp >= 60 -> "⭐"
                                    else -> "📚"
                                },
                                fontSize = 28.sp
                            )
                            Text(
                                "$xp XP",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                ProgressManager.getLevel(xp),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📚 Choose a scenario to practice:",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(scenarios) { scenario ->
                ScenarioCard(
                    scenario = scenario,
                    stars = remember { ProgressManager.getStars(context, scenario.id) },
                    onClick = { onScenarioClick(scenario) }
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun ScenarioCard(scenario: Scenario, stars: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Character emoji
            Text(
                text = scenario.characterEmoji,
                fontSize = 48.sp,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scenario.title,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = scenario.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "With: ${scenario.characterName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Star rating
                if (stars > 0) {
                    Row {
                        repeat(3) { i ->
                            Text(
                                text = if (i < stars) "★" else "☆",
                                fontSize = 18.sp,
                                color = if (i < stars) Color(0xFFFFC107) else Color(0xFFBDBDBD)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Not played yet",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Arrow
            Text(
                text = "▶",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}