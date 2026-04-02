package com.ikhwan.mandarinkids.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikhwan.mandarinkids.R
import com.ikhwan.mandarinkids.data.models.Scenario
import com.ikhwan.mandarinkids.data.models.ScenarioCategory
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.preferences.UserPreferencesRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioListScreen(
    category: ScenarioCategory,
    onScenarioClick: (Scenario, Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { ProgressRepository.getInstance(context) }
    val userPrefs = remember { UserPreferencesRepository.getInstance(context) }
    val disabledScenarios by userPrefs.disabledScenarios.collectAsState(initial = emptySet())
    val allProgress by repo.getAllProgress().collectAsState(initial = emptyList())
    val allScenarios = remember(category) {
        JsonScenarioRepository.getAll().filter { it.category == category }
    }
    val scenarios = remember(allScenarios, disabledScenarios) {
        allScenarios.filter { it.id !in disabledScenarios }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val iconRes = categoryIconRes(category)
                        if (iconRes != null) {
                            Image(
                                painter = painterResource(iconRes),
                                contentDescription = category.displayName,
                                modifier = Modifier.size(28.dp).padding(end = 8.dp)
                            )
                        } else {
                            Text(category.emoji, fontSize = 22.sp, modifier = Modifier.padding(end = 8.dp))
                        }
                        Text(category.displayName, fontSize = 20.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                item {
                SectionHeader(
                    text = "${scenarios.size} scenario${if (scenarios.size != 1) "s" else ""}"
                )
            }

            items(scenarios, key = { it.id }) { scenario ->
                val progress = allProgress.find { it.scenarioId == scenario.id }
                val starsAtLevel = progress?.starsAtCurrentLevel ?: 0
                val everPlayed = (progress?.stars ?: 0) > 0 || (progress?.masteryLevel ?: 1) > 1
                val masteryLevel = progress?.masteryLevel ?: 1
                ScenarioCard(
                    scenario = scenario,
                    starsAtCurrentLevel = starsAtLevel,
                    everPlayed = everPlayed,
                    masteryLevel = masteryLevel,
                    onClick = { onScenarioClick(scenario, masteryLevel) }
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}
