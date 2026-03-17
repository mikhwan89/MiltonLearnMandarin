package com.ikhwan.mandarinkids.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikhwan.mandarinkids.data.models.Scenario
import com.ikhwan.mandarinkids.data.models.ScenarioCategory
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.db.ProgressRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioListScreen(
    category: ScenarioCategory,
    onScenarioClick: (Scenario) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { ProgressRepository.getInstance(context) }
    val scenarios = remember(category) {
        JsonScenarioRepository.getAll().filter { it.category == category }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Text(category.emoji, fontSize = 22.sp, modifier = Modifier.padding(end = 8.dp))
                        Text(category.displayName, fontSize = 20.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                item {
                SectionHeader(
                    text = "${category.emoji} ${scenarios.size} scenario${if (scenarios.size != 1) "s" else ""}"
                )
            }

            items(scenarios, key = { it.id }) { scenario ->
                val stars by repo.getStars(scenario.id).collectAsState(initial = 0)
                ScenarioCard(
                    scenario = scenario,
                    stars = stars,
                    onClick = { onScenarioClick(scenario) }
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}
