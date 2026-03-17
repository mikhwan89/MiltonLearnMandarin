package com.ikhwan.mandarinkids.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.ikhwan.mandarinkids.db.Badge
import com.ikhwan.mandarinkids.db.MasteredWordEntity
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.tts.rememberTtsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCategoryClick: (ScenarioCategory) -> Unit,
    onPracticeClick: () -> Unit
) {
    val context = LocalContext.current
    val scenarios = remember { JsonScenarioRepository.getAll() }
    val repo = remember { ProgressRepository.getInstance(context) }
    val tts = rememberTtsManager()

    val xp by repo.getTotalXp().collectAsState(initial = 0)
    val streak = remember { repo.getStreak() }
    val masteredCount by repo.getMasteredWordCount().collectAsState(initial = 0)
    val dueCount by repo.getDueWordCount().collectAsState(initial = 0)
    val allMasteredWords by repo.getAllMasteredWords().collectAsState(initial = emptyList())

    // Badges — read once and update when composition changes
    val earnedBadges = remember(masteredCount, streak) { repo.getEarnedBadges() }
    val earnedBadgeList = remember(earnedBadges) {
        Badge.entries.filter { it.id in earnedBadges }
    }

    // Word of the Day — computed once per day from mastered words
    val wordOfDay: MasteredWordEntity? = remember(allMasteredWords) {
        if (allMasteredWords.isEmpty()) null
        else repo.getOrPickWordOfDay(allMasteredWords)
    }

    var showWordOfDayDialog by remember { mutableStateOf(false) }

    val activeCategories = remember(scenarios) {
        ScenarioCategory.entries.filter { cat -> scenarios.any { it.category == cat } }
    }

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
                },
                actions = {
                    IconButton(onClick = onPracticeClick) {
                        Icon(Icons.Default.Star, contentDescription = "Practice Mode")
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
            // ── Progress summary card ─────────────────────────────────────
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
                            Text("$streak", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "day streak",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalDivider(modifier = Modifier.height(64.dp).width(1.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

            // ── Badges row ────────────────────────────────────────────────
            if (earnedBadgeList.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "🏅 Your Badges",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                earnedBadgeList.forEach { badge ->
                                    BadgeChip(badge)
                                }
                            }
                        }
                    }
                }
            }

            // ── Word of the Day ───────────────────────────────────────────
            if (wordOfDay != null) {
                item {
                    Card(
                        onClick = { showWordOfDayDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📅", fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Word of the Day",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    wordOfDay.chinese,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    wordOfDay.english,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { tts.speak(wordOfDay.chinese) }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                            }
                        }
                    }
                }
            }

            // ── Practice Mode banner ──────────────────────────────────────
            if (masteredCount > 0) {
                item {
                    Card(
                        onClick = onPracticeClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🃏", fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Practice Mode",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    if (dueCount > 0) "$dueCount word${if (dueCount != 1) "s" else ""} due today"
                                    else "$masteredCount word${if (masteredCount != 1) "s" else ""} in library",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text("▶", fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }

            // ── Section header ────────────────────────────────────────────
            item {
                Text(
                    text = "📚 Choose a category:",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
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

    // ── Word of the Day dialog ────────────────────────────────────────────
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
private fun BadgeChip(badge: Badge) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.08f),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(badge.emoji, fontSize = 22.sp)
            Text(badge.label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
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
                        shape = RoundedCornerShape(12.dp),
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
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
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
fun CategoryCard(category: ScenarioCategory, scenarioCount: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
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
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$scenarioCount scenario${if (scenarioCount != 1) "s" else ""}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("▶", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
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
            Text("▶", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}
