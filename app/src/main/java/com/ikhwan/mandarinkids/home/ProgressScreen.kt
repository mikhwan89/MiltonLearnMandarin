package com.ikhwan.mandarinkids.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikhwan.mandarinkids.ProgressManager
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.db.Badge
import com.ikhwan.mandarinkids.db.ProgressRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen() {
    val context = LocalContext.current
    val repo = remember { ProgressRepository.getInstance(context) }
    val scenarios = remember { JsonScenarioRepository.getAll() }

    val xp by repo.getTotalXp().collectAsState(initial = 0)
    val streak = remember { repo.getStreak() }
    val masteredCount by repo.getMasteredWordCount().collectAsState(initial = 0)
    val allProgress by repo.getAllProgress().collectAsState(initial = emptyList())
    val progressMap = remember(allProgress) { allProgress.associateBy { it.scenarioId } }

    val earnedBadgeList = remember(masteredCount, streak) {
        val earned = repo.getEarnedBadges()
        Badge.entries.filter { it.id in earned }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Progress 📊", fontSize = 20.sp) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // ── XP + Streak + Words card ──────────────────────────────────
            item {
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
                            Text("🔥", fontSize = 32.sp)
                            Text(
                                "$streak",
                                fontSize = 26.sp,
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
                                .height(72.dp)
                                .width(1.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when {
                                    xp >= 180 -> "🌟"
                                    xp >= 60 -> "⭐"
                                    else -> "📚"
                                },
                                fontSize = 32.sp
                            )
                            Text(
                                "$xp XP",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                ProgressManager.getLevel(xp),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier
                                .height(72.dp)
                                .width(1.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🧠", fontSize = 32.sp)
                            Text(
                                "$masteredCount",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "words",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Badges ────────────────────────────────────────────────────
            item {
                Text(
                    "🏅 Badges",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (earnedBadgeList.isNotEmpty()) {
                items(earnedBadgeList.chunked(3)) { rowBadges ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowBadges.forEach { badge ->
                            BadgeCard(badge = badge, modifier = Modifier.weight(1f))
                        }
                        repeat(3 - rowBadges.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "Complete scenarios and keep your streak to earn badges! 🎯",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // ── Scenario stars ────────────────────────────────────────────
            item {
                Text(
                    "📚 Scenario Stars",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(scenarios, key = { it.id }) { scenario ->
                val stars = progressMap[scenario.id]?.stars ?: 0
                ScenarioStarRow(
                    emoji = scenario.characterEmoji,
                    title = scenario.title,
                    stars = stars
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun BadgeCard(badge: Badge, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(badge.emoji, fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                badge.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun ScenarioStarRow(emoji: String, title: String, stars: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (stars > 0)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 26.sp, modifier = Modifier.padding(end = 12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Row {
                repeat(3) { i ->
                    Text(
                        text = if (i < stars) "★" else "☆",
                        fontSize = 18.sp,
                        color = if (i < stars) Color(0xFFFFC107) else Color(0xFFBDBDBD)
                    )
                }
            }
        }
    }
}
