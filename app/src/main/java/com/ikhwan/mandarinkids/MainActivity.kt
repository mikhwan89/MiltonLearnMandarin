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
    val scenarios = remember { ScenarioRepository.getAllScenarios() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "学中文",
                            fontSize = 24.sp
                        )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "📚 Choose a scenario to practice:",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(scenarios) { scenario ->
                    ScenarioCard(
                        scenario = scenario,
                        onClick = { onScenarioClick(scenario) }
                    )
                }
            }
        }
    }
}

@Composable
fun ScenarioCard(scenario: Scenario, onClick: () -> Unit) {
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
                // Chinese title
                Text(
                    text = scenario.title,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Description
                Text(
                    text = scenario.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Character info
                Text(
                    text = "With: ${scenario.characterName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Arrow or indicator
            Text(
                text = "▶",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}