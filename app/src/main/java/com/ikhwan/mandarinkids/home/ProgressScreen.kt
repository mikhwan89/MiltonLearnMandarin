package com.ikhwan.mandarinkids.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikhwan.mandarinkids.R
import com.ikhwan.mandarinkids.ProgressManager
import com.ikhwan.mandarinkids.data.models.ScenarioCategory
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.ikhwan.mandarinkids.db.Badge
import com.ikhwan.mandarinkids.db.MilestoneCondition
import com.ikhwan.mandarinkids.db.MilestoneReward
import com.ikhwan.mandarinkids.db.MilestoneType
import com.ikhwan.mandarinkids.db.PracticeType
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.db.decodeConditions
import com.ikhwan.mandarinkids.db.encodeConditions
import com.ikhwan.mandarinkids.preferences.UserPreferencesRepository
import com.ikhwan.mandarinkids.navigation.Routes
import com.ikhwan.mandarinkids.parent.PinMode
import com.ikhwan.mandarinkids.parent.PinScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(navController: NavController, onParentClick: () -> Unit = {}) {
    val context = LocalContext.current
    val repo = remember { ProgressRepository.getInstance(context) }
    val userPrefs = remember { UserPreferencesRepository.getInstance(context) }
    val scenarios = remember { JsonScenarioRepository.getAll() }
    val scope = rememberCoroutineScope()
    val isDarkMode by userPrefs.darkMode.collectAsState(initial = false)

    val xp by repo.getTotalXp().collectAsState(initial = 0)
    val streak = remember { repo.getStreak() }
    val masteredCount by repo.getMasteredWordCount().collectAsState(initial = 0)
    val masteredDefault by repo.getAllMasteredWords(PracticeType.DEFAULT).collectAsState(initial = emptyList())
    val masteredListening by repo.getAllMasteredWords(PracticeType.LISTENING).collectAsState(initial = emptyList())
    val masteredReading by repo.getAllMasteredWords(PracticeType.READING).collectAsState(initial = emptyList())
    val masteredCountDefault = remember(masteredDefault) { masteredDefault.count { it.boxLevel >= 7 } }
    val masteredCountListening = remember(masteredListening) { masteredListening.count { it.boxLevel >= 7 } }
    val masteredCountReading = remember(masteredReading) { masteredReading.count { it.boxLevel >= 7 } }
    val highMasteryDefault by repo.getHighMasteryCountByType(PracticeType.DEFAULT).collectAsState(initial = 0)
    val highMasteryListening by repo.getHighMasteryCountByType(PracticeType.LISTENING).collectAsState(initial = 0)
    val highMasteryReading by repo.getHighMasteryCountByType(PracticeType.READING).collectAsState(initial = 0)
    val allProgress by repo.getAllProgress().collectAsState(initial = emptyList())
    val progressMap = remember(allProgress) { allProgress.associateBy { it.scenarioId } }
    val allRewards by repo.getAllRewards().collectAsState(initial = emptyList())

    val perfectScenarioCount = remember(progressMap) {
        progressMap.values.count { it.stars == 3 }
    }
    val highMasteryCount by repo.getHighMasteryWordCount().collectAsState(initial = 0)
    val earnedBadgeIds = remember(masteredCount, streak) { repo.getEarnedBadges() }
    val progressForCondition: (MilestoneCondition) -> Int = remember(
        perfectScenarioCount, highMasteryCount, highMasteryDefault,
        highMasteryListening, highMasteryReading, xp, earnedBadgeIds
    ) {
        { cond ->
            when (MilestoneType.entries.find { it.name == cond.type }) {
                MilestoneType.PERFECT_SCENARIOS  -> perfectScenarioCount
                MilestoneType.HIGH_MASTERY_WORDS -> highMasteryCount
                MilestoneType.MASTERY_DEFAULT    -> highMasteryDefault
                MilestoneType.MASTERY_LISTENING  -> highMasteryListening
                MilestoneType.MASTERY_READING    -> highMasteryReading
                MilestoneType.TOTAL_XP           -> xp
                MilestoneType.SPECIFIC_BADGE     -> if (cond.badgeId != null && cond.badgeId in earnedBadgeIds) 1 else 0
                null                             -> 0
            }
        }
    }

    val scenariosByCategory = remember(scenarios) { scenarios.groupBy { it.category } }
    val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }

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
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.nav_progress),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("My Progress", fontSize = 20.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { scope.launch { userPrefs.saveDarkMode(!isDarkMode) } }) {
                        Icon(
                            if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "Switch to light mode" else "Switch to dark mode"
                        )
                    }
                    IconButton(onClick = onParentClick) {
                        Icon(Icons.Default.Lock, contentDescription = "Parental Control")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
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
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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

                        if (masteredCountDefault + masteredCountListening + masteredCountReading > 0) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                            Text(
                                "★7+ mastered words",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                    .padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MasteryTypeChip(emoji = "🔊字", label = "Default", count = masteredCountDefault)
                                MasteryTypeChip(emoji = "🔊", label = "Listening", count = masteredCountListening)
                                MasteryTypeChip(emoji = "字", label = "Reading", count = masteredCountReading)
                            }
                        }
                    }
                }
            }

            // ── Badges ────────────────────────────────────────────────────
            item {
                val earnedCount = earnedBadgeIds.size
                val totalCount = Badge.entries.size
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏅 Badges", fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f))
                    Text("$earnedCount / $totalCount", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            items(Badge.entries.chunked(3)) { rowBadges ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowBadges.forEach { badge ->
                        BadgeCard(
                            badge = badge,
                            earned = badge.id in earnedBadgeIds,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - rowBadges.size) { Spacer(modifier = Modifier.weight(1f)) }
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
                        color = MaterialTheme.colorScheme.onSurface,
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
                        progressForCondition = progressForCondition,
                        parentUnlocked = rewardsUnlocked,
                        onDelete = { scope.launch { repo.deleteReward(reward.id) } },
                        onClaim = { scope.launch { repo.claimReward(reward.id) } }
                    )
                }
            }

            // ── Scenario stars ────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📚 Scenario Stars",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "$perfectScenarioCount / ${scenarios.size}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            scenariosByCategory.forEach { (category, categoryScenarios) ->
                val isExpanded = expandedCategories[category.name] ?: true
                val completedInCat = categoryScenarios.count { (progressMap[it.id]?.stars ?: 0) == 3 }

                item(key = "cat_${category.name}") {
                    Surface(
                        onClick = { expandedCategories[category.name] = !isExpanded },
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category.emoji, fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                category.displayName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "$completedInCat / ${categoryScenarios.size}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                if (isExpanded) {
                    items(categoryScenarios, key = { it.id }) { scenario ->
                        val stars = progressMap[scenario.id]?.stars ?: 0
                        ScenarioStarRow(
                            emoji = scenario.characterEmoji,
                            title = scenario.title,
                            stars = stars,
                            onClick = { navController.navigate(Routes.roleplay(scenario.id)) }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    // ── Add Reward dialog ─────────────────────────────────────────────────
    if (showAddReward && rewardsUnlocked) {
        AddRewardDialog(
            onDismiss = { showAddReward = false },
            onAdd = { conditions, logic, text ->
                scope.launch { repo.addReward(conditions, logic, text) }
                showAddReward = false
            }
        )
    }
}

@Composable
private fun MasteryTypeChip(emoji: String, label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 16.sp)
        Text("$count", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BadgeCard(badge: Badge, earned: Boolean, modifier: Modifier = Modifier) {
    var showInfo by remember { mutableStateOf(false) }

    Card(
        onClick = { showInfo = true },
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (earned) Color(0xFFD5EDD5)
                             else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                badge.emoji, fontSize = 26.sp,
                modifier = if (earned) Modifier else Modifier.alpha(0.25f)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                badge.label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                color = if (earned) Color(0xFF1A3D1A)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(2.dp))
            // Always rendered to keep card height uniform; visible only when locked
            Text("🔒", fontSize = 11.sp, modifier = Modifier.alpha(if (earned) 0f else 1f))
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            icon = { Text(badge.emoji, fontSize = 36.sp) },
            title = { Text(badge.label, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(badge.description, fontSize = 14.sp, textAlign = TextAlign.Center)
                    if (earned) {
                        Spacer(Modifier.height(8.dp))
                        Text("✅ Earned!", fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun MilestoneRewardCard(
    reward: MilestoneReward,
    progressForCondition: (MilestoneCondition) -> Int,
    parentUnlocked: Boolean,
    onDelete: () -> Unit,
    onClaim: () -> Unit
) {
    val conditions = reward.decodeConditions()
    // (condition, currentValue, isMet)
    val results = conditions.map { cond ->
        val cur = progressForCondition(cond)
        Triple(cond, cur, cur >= cond.targetValue)
    }
    val isComplete = when (reward.logic) {
        "OR"  -> results.any { it.third }
        else  -> results.all { it.third }
    }
    val overallProgress = if (results.isEmpty()) 0f else when (reward.logic) {
        "OR"  -> results.maxOf { (it.second.toFloat() / it.first.targetValue).coerceIn(0f, 1f) }
        else  -> results.minOf { (it.second.toFloat() / it.first.targetValue).coerceIn(0f, 1f) }
    }

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
                    if (results.size > 1) {
                        Text(
                            if (reward.logic == "OR") "Any of:" else "All of:",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    results.forEach { (cond, current, met) ->
                        val t = MilestoneType.entries.find { it.name == cond.type }
                        val bullet = if (results.size > 1) (if (met) "✅ " else "◻ ") else ""
                        val lineText = if (t == MilestoneType.SPECIFIC_BADGE) {
                            val b = Badge.entries.find { it.id == cond.badgeId }
                            "$bullet${b?.emoji ?: "🏅"} ${b?.label ?: "Badge"}"
                        } else {
                            "$bullet${t?.label ?: cond.type}: $current / ${cond.targetValue} ${t?.unit ?: ""}"
                        }
                        Text(
                            lineText,
                            fontSize = 12.sp,
                            color = if (met) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (reward.isClaimed) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(50)) {
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
                    progress = { overallProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isComplete) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AddRewardDialog(
    onDismiss: () -> Unit,
    onAdd: (List<MilestoneCondition>, String, String) -> Unit
) {
    var conditions by remember {
        mutableStateOf(listOf(MilestoneCondition(MilestoneType.PERFECT_SCENARIOS.name, 1)))
    }
    var logic by remember { mutableStateOf("AND") }
    var rewardText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Milestone Reward") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "When your child achieves:",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                conditions.forEachIndexed { idx, cond ->
                    ConditionRow(
                        condition = cond,
                        onUpdate = { updated ->
                            conditions = conditions.toMutableList().also { it[idx] = updated }
                        },
                        onRemove = if (conditions.size > 1) {
                            { conditions = conditions.toMutableList().also { it.removeAt(idx) } }
                        } else null
                    )
                    // AND / OR toggle between consecutive conditions
                    if (conditions.size > 1 && idx < conditions.size - 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = logic == "AND",
                                onClick = { logic = "AND" },
                                label = { Text("AND", fontSize = 11.sp) }
                            )
                            Spacer(Modifier.width(8.dp))
                            FilterChip(
                                selected = logic == "OR",
                                onClick = { logic = "OR" },
                                label = { Text("OR", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                if (conditions.size < 3) {
                    TextButton(onClick = {
                        conditions = conditions + MilestoneCondition(
                            MilestoneType.PERFECT_SCENARIOS.name, 1
                        )
                    }) { Text("+ Add another condition", fontSize = 12.sp) }
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = rewardText,
                    onValueChange = { rewardText = it },
                    label = { Text("Reward description") },
                    placeholder = { Text("e.g. Ice cream trip! 🍦") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (rewardText.isBlank()) return@TextButton
                val invalid = conditions.any { cond ->
                    if (MilestoneType.entries.find { it.name == cond.type } == MilestoneType.SPECIFIC_BADGE)
                        cond.badgeId == null
                    else cond.targetValue <= 0
                }
                if (invalid) return@TextButton
                onAdd(conditions, logic, rewardText)
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ConditionRow(
    condition: MilestoneCondition,
    onUpdate: (MilestoneCondition) -> Unit,
    onRemove: (() -> Unit)?
) {
    val selectedType = MilestoneType.entries.find { it.name == condition.type }
        ?: MilestoneType.PERFECT_SCENARIOS
    var typeExpanded by remember { mutableStateOf(false) }
    var badgeExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Type dropdown button
        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(
                onClick = { typeExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    "${selectedType.emoji} ${selectedType.label}",
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            DropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                MilestoneType.entries.forEach { t ->
                    DropdownMenuItem(
                        text = { Text("${t.emoji} ${t.label}", fontSize = 12.sp) },
                        onClick = {
                            onUpdate(
                                if (t == MilestoneType.SPECIFIC_BADGE)
                                    condition.copy(type = t.name, targetValue = 1, badgeId = null)
                                else
                                    condition.copy(type = t.name, badgeId = null)
                            )
                            typeExpanded = false
                        }
                    )
                }
            }
        }

        if (selectedType == MilestoneType.SPECIFIC_BADGE) {
            // Badge picker
            val selectedBadge = Badge.entries.find { it.id == condition.badgeId }
            Box(modifier = Modifier.width(88.dp)) {
                OutlinedButton(
                    onClick = { badgeExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text(
                        selectedBadge?.let { "${it.emoji} ${it.label}" } ?: "Pick badge",
                        fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                DropdownMenu(
                    expanded = badgeExpanded,
                    onDismissRequest = { badgeExpanded = false }
                ) {
                    Badge.entries.forEach { b ->
                        DropdownMenuItem(
                            text = { Text("${b.emoji} ${b.label}", fontSize = 12.sp) },
                            onClick = {
                                onUpdate(condition.copy(badgeId = b.id, targetValue = 1))
                                badgeExpanded = false
                            }
                        )
                    }
                }
            }
        } else {
            // Numeric target input
            OutlinedTextField(
                value = if (condition.targetValue == 0) "" else condition.targetValue.toString(),
                onValueChange = { v ->
                    onUpdate(condition.copy(targetValue = v.filter { it.isDigit() }.toIntOrNull() ?: 0))
                },
                label = { Text(selectedType.unit, fontSize = 9.sp) },
                modifier = Modifier.width(88.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Remove button
        if (onRemove != null) {
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Close, "Remove condition",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Spacer(modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun ScenarioStarRow(emoji: String, title: String, stars: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { i ->
                    Text(
                        text = if (i < stars) "★" else "☆",
                        fontSize = 18.sp,
                        color = if (i < stars) Color(0xFFFFC107) else Color(0xFFBDBDBD)
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Play scenario",
                    modifier = Modifier.size(18.dp).padding(start = 4.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
