package com.ikhwan.mandarinkids.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikhwan.mandarinkids.ProgressManager
import com.ikhwan.mandarinkids.ToneUtils
import com.ikhwan.mandarinkids.data.models.Scenario
import com.ikhwan.mandarinkids.data.models.ScenarioCategory
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.db.MasteredWordEntity
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.preferences.UserPreferencesRepository
import com.ikhwan.mandarinkids.tts.rememberTtsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCategoryClick: (ScenarioCategory) -> Unit,
    showWordOfDayOnLaunch: Boolean = false,
    onWordOfDayShown: () -> Unit = {}
) {
    val context = LocalContext.current
    val scenarios = remember { JsonScenarioRepository.getAll() }
    val repo = remember { ProgressRepository.getInstance(context) }
    val userPrefs = remember { UserPreferencesRepository.getInstance(context) }
    val tts = rememberTtsManager()
    val disabledCategories by userPrefs.disabledCategories.collectAsState(initial = emptySet())
    val disabledScenarios by userPrefs.disabledScenarios.collectAsState(initial = emptySet())

    val xp by repo.getTotalXp().collectAsState(initial = 0)
    val streak = remember { repo.getStreak() }
    val allMasteredWords by repo.getAllMasteredWords().collectAsState(initial = emptyList())

    val wordOfDay: MasteredWordEntity? = remember(allMasteredWords) {
        if (allMasteredWords.isEmpty()) null
        else repo.getOrPickWordOfDay(allMasteredWords)
    }

    var showWordOfDayDialog by remember { mutableStateOf(false) }

    // Show word-of-day popup automatically once per session
    LaunchedEffect(wordOfDay) {
        if (wordOfDay != null && showWordOfDayOnLaunch) {
            showWordOfDayDialog = true
            onWordOfDayShown()
        }
    }

    val activeCategories = remember(scenarios, disabledCategories, disabledScenarios) {
        ScenarioCategory.entries.filter { cat ->
            cat.name !in disabledCategories &&
            scenarios.any { it.category == cat && it.id !in disabledScenarios }
        }
    }

    Scaffold(
        containerColor = Color(0xFFFBF9F4),
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFBF9F4)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFBF9F4))
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Progress summary card ─────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 40.dp)
                        ) {
                            Text("🔥", fontSize = 28.sp)
                            Text("$streak", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "day streak",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 40.dp)
                        ) {
                            Text(
                                text = when {
                                    xp >= 180 -> "🌟"
                                    xp >= 60 -> "⭐"
                                    else -> "📚"
                                },
                                fontSize = 28.sp
                            )
                            Text("$xp XP", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text(
                                ProgressManager.getLevel(xp),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Section header ────────────────────────────────────────────
            item {
                SectionHeader(text = "📚 Choose a Category")
            }

            // ── Category cards ────────────────────────────────────────────
            items(activeCategories, key = { it.name }) { category ->
                val scenariosInCat = scenarios.filter { it.category == category }
                CategoryCard(
                    category = category,
                    scenarioCount = scenariosInCat.size,
                    onClick = { onCategoryClick(category) }
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }

    // ── Word of the Day dialog (launch-time popup) ────────────────────────
    if (showWordOfDayDialog && wordOfDay != null) {
        WordOfDayDialog(
            word = wordOfDay,
            onDismiss = { showWordOfDayDialog = false },
            onPlay = { tts.speak(wordOfDay.chinese) }
        )
        LaunchedEffect(Unit) { tts.speak(wordOfDay.chinese) }
    }
}

@Composable
private fun WordOfDayDialog(
    word: MasteredWordEntity,
    onDismiss: () -> Unit,
    onPlay: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("📅 Word of the Day", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    word.chinese,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    ToneUtils.coloredAnnotatedPinyin(word.pinyin),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text("🇬🇧  ${word.english}", fontSize = 16.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "🇮🇩  ${word.indonesian}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (word.note != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "💡 ${word.note}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onPlay) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play word pronunciation")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play again")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun SectionHeader(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
fun CategoryCard(category: ScenarioCategory, scenarioCount: Int, onClick: () -> Unit) {
    // Muted, tonal tints — saturated enough to differentiate, light enough for dark text
    val gradientColors = when (category) {
        ScenarioCategory.ESSENTIALS        -> listOf(Color(0xFFD0E8F8), Color(0xFFE4F2FB))  // slate blue tint
        ScenarioCategory.AT_SCHOOL         -> listOf(Color(0xFFD4EDD0), Color(0xFFE6F4E4))  // sage tint
        ScenarioCategory.SCHOOL_SUBJECTS   -> listOf(Color(0xFFE8E4F5), Color(0xFFF2EFF9))  // lavender tint
        ScenarioCategory.FOOD_AND_EATING   -> listOf(Color(0xFFFFDDB5), Color(0xFFFFEDD4))  // warm amber tint
        ScenarioCategory.FEELINGS_AND_HEALTH -> listOf(Color(0xFFF5E0E0), Color(0xFFFAEEEE)) // rose tint
        ScenarioCategory.PLAY_AND_HOBBIES  -> listOf(Color(0xFFD5EDD5), Color(0xFFE6F5E6))  // mint tint
        else -> listOf(Color(0xFFF5F4ED), Color(0xFFFBF9F4))
    }
    val shape = RoundedCornerShape(24.dp)
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 90.dp),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 90.dp)
                .background(Brush.horizontalGradient(gradientColors), shape = shape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.emoji,
                    fontSize = 48.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.displayName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF31332E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$scenarioCount scenario${if (scenarioCount != 1) "s" else ""}",
                        fontSize = 14.sp,
                        color = Color(0xFF4A4C47)
                    )
                }
                Text("▶", fontSize = 24.sp, color = Color(0xFF386A34))
            }
        }
    }
}

@Composable
fun ScenarioCard(scenario: Scenario, stars: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = scenario.characterEmoji,
                fontSize = 48.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scenario.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = scenario.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (stars > 0) {
                    Row {
                        repeat(3) { i ->
                            Text(
                                text = if (i < stars) "★" else "☆",
                                fontSize = 20.sp,
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
            Text("▶", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}
