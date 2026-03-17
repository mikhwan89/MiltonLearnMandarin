package com.ikhwan.mandarinkids.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import com.ikhwan.mandarinkids.db.MilestoneReward
import com.ikhwan.mandarinkids.db.MilestoneType
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.parent.PinMode
import com.ikhwan.mandarinkids.parent.PinScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen() {
    val context = LocalContext.current
    val repo = remember { ProgressRepository.getInstance(context) }
    val scenarios = remember { JsonScenarioRepository.getAll() }
    val scope = rememberCoroutineScope()

    val xp by repo.getTotalXp().collectAsState(initial = 0)
    val streak = remember { repo.getStreak() }
    val masteredCount by repo.getMasteredWordCount().collectAsState(initial = 0)
    val allProgress by repo.getAllProgress().collectAsState(initial = emptyList())
    val progressMap = remember(allProgress) { allProgress.associateBy { it.scenarioId } }
    val allRewards by repo.getAllRewards().collectAsState(initial = emptyList())

    val perfectScenarioCount = remember(progressMap) {
        progressMap.values.count { it.stars == 3 }
    }

    val earnedBadgeList = remember(masteredCount, streak) {
        val earned = repo.getEarnedBadges()
        Badge.entries.filter { it.id in earned }
    }

    // ── Parent mode state for Milestone Rewards ───────────────────────────
    var rewardsUnlocked by remember { mutableStateOf(false) }
    var showPinForRewards by remember { mutableStateOf(false) }
    var showAddReward by remember { mutableStateOf(false) }

    // PIN overlay — renders over the whole screen
    if (showPinForRewards) {
        val pinMode = if (repo.isPinSet()) PinMode.VERIFY else PinMode.SET
        PinScreen(
            mode = pinMode,
            onSuccess = {
                showPinForRewards = false
                rewardsUnlocked = true
            },
            onBack = { showPinForRewards = false },
            onVerify = { repo.verifyPin(it) },
            onSetPin = { repo.setPin(it) }
        )
        return
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
                            Text("$streak", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            Text("day streak", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        HorizontalDivider(modifier = Modifier.height(72.dp).width(1.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when { xp >= 180 -> "🌟"; xp >= 60 -> "⭐"; else -> "📚" },
                                fontSize = 32.sp
                            )
                            Text("$xp XP", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            Text(ProgressManager.getLevel(xp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        HorizontalDivider(modifier = Modifier.height(72.dp).width(1.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🧠", fontSize = 32.sp)
                            Text("$masteredCount", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            Text("words", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ── Badges ────────────────────────────────────────────────────
            item {
                Text("🏅 Badges", fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp))
            }

            if (earnedBadgeList.isNotEmpty()) {
                items(earnedBadgeList.chunked(3)) { rowBadges ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowBadges.forEach { badge ->
                            BadgeCard(badge = badge, modifier = Modifier.weight(1f))
                        }
                        repeat(3 - rowBadges.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            } else {
                item {
                    Card(modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text("Complete scenarios and keep your streak to earn badges! 🎯",
                            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp))
                    }
                }
            }

            // ── Milestone Rewards header with parent lock ─────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🎁 Milestone Rewards",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (rewardsUnlocked) {
                        IconButton(onClick = { showAddReward = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Reward",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { rewardsUnlocked = false }) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock parent mode",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        IconButton(onClick = { showPinForRewards = true }) {
                            Icon(Icons.Default.LockOpen, contentDescription = "Parent: manage rewards",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (rewardsUnlocked) {
                item {
                    Text("🔓 Parent mode — tap + to add, 🗑 to delete",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 2.dp))
                }
            }

            if (allRewards.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text(
                            text = if (rewardsUnlocked)
                                "No rewards yet — tap + to add one!"
                            else
                                "No rewards set yet. Tap 🔓 to add one as a parent.",
                            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp))
                    }
                }
            } else {
                items(allRewards, key = { it.id }) { reward ->
                    MilestoneRewardCard(
                        reward = reward,
                        currentProgress = when (MilestoneType.entries.find { it.name == reward.milestoneType }) {
                            MilestoneType.PERFECT_SCENARIOS -> perfectScenarioCount
                            null -> 0
                        },
                        parentUnlocked = rewardsUnlocked,
                        onDelete = { scope.launch { repo.deleteReward(reward.id) } },
                        onClaim = { scope.launch { repo.claimReward(reward.id) } }
                    )
                }
            }

            // ── Scenario stars ────────────────────────────────────────────
            item {
                Text("📚 Scenario Stars", fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp))
            }

            items(scenarios, key = { it.id }) { scenario ->
                val stars = progressMap[scenario.id]?.stars ?: 0
                ScenarioStarRow(emoji = scenario.characterEmoji, title = scenario.title, stars = stars)
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    // ── Add Reward dialog ─────────────────────────────────────────────────
    if (showAddReward && rewardsUnlocked) {
        AddRewardDialog(
            totalScenarios = scenarios.size,
            onDismiss = { showAddReward = false },
            onAdd = { target, text ->
                scope.launch { repo.addReward(MilestoneType.PERFECT_SCENARIOS, target, text) }
                showAddReward = false
            }
        )
    }
}

@Composable
private fun BadgeCard(badge: Badge, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(badge.emoji, fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(badge.label, fontSize = 11.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    }
}

@Composable
private fun MilestoneRewardCard(
    reward: MilestoneReward,
    currentProgress: Int,
    parentUnlocked: Boolean,
    onDelete: () -> Unit,
    onClaim: () -> Unit
) {
    val milestoneType = MilestoneType.entries.find { it.name == reward.milestoneType }
    val progress = (currentProgress.toFloat() / reward.targetValue).coerceIn(0f, 1f)
    val isComplete = currentProgress >= reward.targetValue

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                reward.isClaimed -> MaterialTheme.colorScheme.surfaceVariant
                isComplete -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(if (reward.isClaimed) "✅" else if (isComplete) "🎉" else "🎯", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(reward.rewardText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${milestoneType?.label ?: reward.milestoneType}: $currentProgress / ${reward.targetValue}",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (reward.isClaimed) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp)) {
                        Text("Claimed!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                } else if (isComplete && parentUnlocked) {
                    TextButton(onClick = onClaim) { Text("Mark claimed") }
                }
                if (parentUnlocked) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete reward",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (!reward.isClaimed) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isComplete) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AddRewardDialog(totalScenarios: Int, onDismiss: () -> Unit, onAdd: (Int, String) -> Unit) {
    var targetText by remember { mutableStateOf("") }
    var rewardText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Milestone Reward") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Set a reward for when Milton gets 3 stars on a number of scenarios.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it.filter { c -> c.isDigit() } },
                    label = { Text("Number of 3-star scenarios (max $totalScenarios)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rewardText,
                    onValueChange = { rewardText = it },
                    label = { Text("Reward (e.g. Ice cream trip!)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val target = targetText.toIntOrNull()?.coerceIn(1, totalScenarios) ?: return@TextButton
                if (rewardText.isBlank()) return@TextButton
                onAdd(target, rewardText)
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ScenarioStarRow(emoji: String, title: String, stars: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (stars > 0) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 26.sp, modifier = Modifier.padding(end = 12.dp))
            Text(title, fontSize = 14.sp, modifier = Modifier.weight(1f))
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
