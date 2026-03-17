package com.ikhwan.mandarinkids.parent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.db.Badge
import com.ikhwan.mandarinkids.db.MilestoneReward
import com.ikhwan.mandarinkids.db.MilestoneType
import com.ikhwan.mandarinkids.db.ProgressRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { ProgressRepository.getInstance(context) }
    val scope = rememberCoroutineScope()
    val scenarios = remember { JsonScenarioRepository.getAll() }

    val xp by repo.getTotalXp().collectAsState(initial = 0)
    val streak = remember { repo.getStreak() }
    val masteredCount by repo.getMasteredWordCount().collectAsState(initial = 0)
    val allProgress by repo.getAllProgress().collectAsState(initial = emptyList())
    val allRewards by repo.getAllRewards().collectAsState(initial = emptyList())
    val highMasteryCount by repo.getHighMasteryWordCount().collectAsState(initial = 0)
    val earnedBadges = remember(masteredCount, allProgress) { repo.getEarnedBadges() }
    var showIndonesian by remember { mutableStateOf(repo.getShowIndonesian()) }
    var showResetConfirm by remember { mutableStateOf(false) }

    // Rewards section PIN state
    var rewardsUnlocked by remember { mutableStateOf(false) }
    var showPinForRewards by remember { mutableStateOf(false) }
    var showAddReward by remember { mutableStateOf(false) }

    val perfectCount = allProgress.count { it.stars >= 3 }

    // PIN overlay for rewards section
    if (showPinForRewards) {
        val pinMode = if (repo.isPinSet()) PinMode.VERIFY else PinMode.SET
        PinScreen(
            mode = pinMode,
            onSuccess = {
                showPinForRewards = false
                rewardsUnlocked = true
                if (showAddReward) { /* will open after unlock */ }
            },
            onBack = { showPinForRewards = false },
            onVerify = { repo.verifyPin(it) },
            onSetPin = { repo.setPin(it) }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            // ── Progress Summary ──────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader("📊 Progress Summary")
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
                        StatColumn("🔥", "$streak", "Streak")
                        StatColumn("⭐", "$xp XP", "Total XP")
                        StatColumn("📚", "$masteredCount", "Words")
                        StatColumn(
                            "🌟",
                            "$perfectCount/${scenarios.size}",
                            "3-star"
                        )
                    }
                }
            }

            // ── Per-scenario stars ────────────────────────────────────────
            item { SectionHeader("🎯 Scenario Progress") }
            items(scenarios) { scenario ->
                val progress = allProgress.firstOrNull { it.scenarioId == scenario.id }
                val stars = progress?.stars ?: 0
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(scenario.characterEmoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(scenario.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                scenario.category.displayName,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row {
                            repeat(3) { i ->
                                Text(
                                    text = if (i < stars) "★" else "☆",
                                    fontSize = 18.sp,
                                    color = if (i < stars) Color(0xFFFFC107)
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }

            // ── Badges ────────────────────────────────────────────────────
            item {
                SectionHeader("🏅 Badges Earned (${earnedBadges.size}/${Badge.entries.size})")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Badge.entries.forEach { badge ->
                            val earned = badge.id in earnedBadges
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(badge.emoji, fontSize = 22.sp, modifier = Modifier.width(36.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        badge.label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (earned) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        badge.description,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    if (earned) "✅" else "○",
                                    fontSize = 16.sp,
                                    color = if (earned) Color(0xFF4CAF50)
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }

            // ── Milestone Rewards By Parent ───────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("🎁 Milestone Reward By Parent", modifier = Modifier.weight(1f))
                    if (rewardsUnlocked) {
                        IconButton(onClick = { showAddReward = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Reward")
                        }
                        IconButton(onClick = { rewardsUnlocked = false }) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        IconButton(onClick = { showPinForRewards = true }) {
                            Icon(Icons.Default.LockOpen, contentDescription = "Unlock to manage")
                        }
                    }
                }
            }

            if (!rewardsUnlocked) {
                item {
                    Text(
                        "🔐 Tap the unlock icon to manage rewards",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }
            }

            if (allRewards.isEmpty()) {
                item {
                    Text(
                        "No rewards set yet. Unlock and tap + to add one.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                }
            } else {
                items(allRewards, key = { it.id }) { reward ->
                    RewardItem(
                        reward = reward,
                        perfectCount = perfectCount,
                        highMasteryCount = highMasteryCount,
                        unlocked = rewardsUnlocked,
                        onClaim = { scope.launch { repo.claimReward(reward.id) } },
                        onDelete = { scope.launch { repo.deleteReward(reward.id) } }
                    )
                }
            }

            // ── Settings ──────────────────────────────────────────────────
            item {
                SectionHeader("⚙️ Settings")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Show Indonesian", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Show Indonesian translations in quiz & flashcards",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = showIndonesian,
                                onCheckedChange = {
                                    showIndonesian = it
                                    repo.setShowIndonesian(it)
                                }
                            )
                        }
                        HorizontalDivider()
                        TextButton(
                            onClick = { showResetConfirm = true },
                            modifier = Modifier.padding(horizontal = 8.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("🗑️ Reset All Progress")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset All Progress?") },
            text = { Text("This will delete all stars, XP, mastered words, streak, badges, and rewards. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirm = false
                        scope.launch { repo.resetAllProgress() }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddReward && rewardsUnlocked) {
        AddRewardDialog(
            totalScenarios = JsonScenarioRepository.getAll().size,
            onDismiss = { showAddReward = false },
            onAdd = { type, target, text ->
                scope.launch { repo.addReward(type, target, text) }
                showAddReward = false
            }
        )
    }
}

@Composable
private fun RewardItem(
    reward: MilestoneReward,
    perfectCount: Int,
    highMasteryCount: Int,
    unlocked: Boolean,
    onClaim: () -> Unit,
    onDelete: () -> Unit
) {
    val milestoneType = MilestoneType.entries.find { it.name == reward.milestoneType }
    val currentProgress = when (milestoneType) {
        MilestoneType.PERFECT_SCENARIOS -> perfectCount
        MilestoneType.HIGH_MASTERY_WORDS -> highMasteryCount
        null -> 0
    }
    val progress = (currentProgress.toFloat() / reward.targetValue.toFloat()).coerceIn(0f, 1f)
    val reached = currentProgress >= reward.targetValue

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (reached && !reward.isClaimed)
                MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(reward.rewardText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${milestoneType?.label ?: reward.milestoneType}: $currentProgress / ${reward.targetValue}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (reward.isClaimed) {
                    Text("✅ Claimed", fontSize = 12.sp, color = Color(0xFF4CAF50))
                } else if (reached && unlocked) {
                    TextButton(onClick = onClaim) { Text("Claim!") }
                }
                if (unlocked) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (!reward.isClaimed) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "$currentProgress / ${reward.targetValue} ${milestoneType?.unit ?: ""}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun AddRewardDialog(
    totalScenarios: Int,
    onDismiss: () -> Unit,
    onAdd: (MilestoneType, Int, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(MilestoneType.PERFECT_SCENARIOS) }
    var targetText by remember { mutableStateOf("") }
    var rewardText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Milestone Reward") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Choose what Milton needs to achieve:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MilestoneType.entries.forEach { type ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type; targetText = "" }
                        )
                        Text("${type.emoji} ${type.label}", fontSize = 13.sp)
                    }
                }
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it.filter { c -> c.isDigit() } },
                    label = {
                        Text(when (selectedType) {
                            MilestoneType.PERFECT_SCENARIOS -> "Number of 3-star scenarios (max $totalScenarios)"
                            MilestoneType.HIGH_MASTERY_WORDS -> "Number of words at mastery 7+"
                        })
                    },
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
            TextButton(
                onClick = {
                    val maxTarget = if (selectedType == MilestoneType.PERFECT_SCENARIOS) totalScenarios else 999
                    val target = targetText.toIntOrNull()?.coerceIn(1, maxTarget) ?: return@TextButton
                    if (rewardText.isBlank()) return@TextButton
                    onAdd(selectedType, target, rewardText)
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun StatColumn(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 24.sp)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
