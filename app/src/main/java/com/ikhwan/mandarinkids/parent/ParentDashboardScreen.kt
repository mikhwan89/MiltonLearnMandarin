package com.ikhwan.mandarinkids.parent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.ikhwan.mandarinkids.ui.theme.appColors
import androidx.compose.ui.res.painterResource
import com.ikhwan.mandarinkids.ProgressManager
import com.ikhwan.mandarinkids.R
import com.ikhwan.mandarinkids.home.categoryIconRes
import com.ikhwan.mandarinkids.home.streakIconRes
import com.ikhwan.mandarinkids.home.xpIconRes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikhwan.mandarinkids.data.models.ScenarioCategory
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.preferences.UserPreferencesRepository
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.ikhwan.mandarinkids.db.Badge
import com.ikhwan.mandarinkids.db.MilestoneCondition
import com.ikhwan.mandarinkids.db.MilestoneReward
import com.ikhwan.mandarinkids.db.MilestoneType
import com.ikhwan.mandarinkids.db.PracticeType
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.db.decodeConditions
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
    val highMasteryDefault by repo.getHighMasteryCountByType(PracticeType.DEFAULT).collectAsState(initial = 0)
    val highMasteryListening by repo.getHighMasteryCountByType(PracticeType.LISTENING).collectAsState(initial = 0)
    val highMasteryReading by repo.getHighMasteryCountByType(PracticeType.READING).collectAsState(initial = 0)
    val earnedBadges = remember(masteredCount, allProgress) { repo.getEarnedBadges() }
    val perfectCount = allProgress.count { it.stars >= 3 }
    val progressForCondition: (MilestoneCondition) -> Int = remember(
        perfectCount, highMasteryCount, highMasteryDefault,
        highMasteryListening, highMasteryReading, xp, earnedBadges
    ) {
        { cond ->
            when (MilestoneType.entries.find { it.name == cond.type }) {
                MilestoneType.PERFECT_SCENARIOS  -> perfectCount
                MilestoneType.HIGH_MASTERY_WORDS -> highMasteryCount
                MilestoneType.MASTERY_DEFAULT    -> highMasteryDefault
                MilestoneType.MASTERY_LISTENING  -> highMasteryListening
                MilestoneType.MASTERY_READING    -> highMasteryReading
                MilestoneType.TOTAL_XP           -> xp
                MilestoneType.SPECIFIC_BADGE     -> if (cond.badgeId != null && cond.badgeId in earnedBadges) 1 else 0
                null                             -> 0
            }
        }
    }
    val userPrefs = remember { UserPreferencesRepository.getInstance(context) }
    val colors = MaterialTheme.appColors
    val labelColor = colors.onLightTile
    val showIndonesian by userPrefs.showIndonesian.collectAsState(initial = true)
    val disabledTabs by userPrefs.disabledTabs.collectAsState(initial = emptySet())
    val disabledCategories by userPrefs.disabledCategories.collectAsState(initial = emptySet())
    val disabledScenarios by userPrefs.disabledScenarios.collectAsState(initial = emptySet())
    var expandedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showAddReward by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parental Control") },
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
            // ── 1. Progress Summary ───────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.nav_progress),
                        contentDescription = "Progress Summary",
                        tint = androidx.compose.ui.graphics.Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Progress Summary",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                val summaryGradient = colors.tileBlue.asList()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(summaryGradient))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatColumn("", "$streak", "Streak", labelColor = labelColor, drawableRes = streakIconRes(streak))
                            StatColumn("", "$xp XP", ProgressManager.getLevelLabel(xp), labelColor = labelColor, drawableRes = xpIconRes(xp))
                            StatColumn("", "$masteredCount", "Words", labelColor = labelColor, drawableRes = R.drawable.words)
                            StatColumn("", "$perfectCount/${scenarios.size}", "3-star", labelColor = labelColor, drawableRes = R.drawable.three_star)
                        }
                    }
                }
            }

            // ── 2. Milestone Rewards ──────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("🎁 Milestone Rewards", modifier = Modifier.weight(1f))
                    IconButton(onClick = { showAddReward = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Reward")
                    }
                }
            }

            if (allRewards.isEmpty()) {
                item {
                    Text(
                        "No rewards set yet. Tap + to add one.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                }
            } else {
                items(allRewards, key = { it.id }) { reward ->
                    RewardItem(
                        reward = reward,
                        progressForCondition = progressForCondition,
                        unlocked = true,
                        onClaim = { scope.launch { repo.claimReward(reward.id) } },
                        onDelete = { scope.launch { repo.deleteReward(reward.id) } }
                    )
                }
            }

            // ── 3. Content Settings ───────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader("⚙️ Content Settings")
                val settingsGradient = colors.tilePurple.asList()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(settingsGradient))
                    ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

                        // ── Translation ───────────────────────────────────────
                        Text("🌐 Translation", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Bahasa Indonesia", fontSize = 14.sp)
                            Switch(
                                checked = showIndonesian,
                                onCheckedChange = { scope.launch { userPrefs.saveShowIndonesian(it) } }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // ── Practice Tabs ─────────────────────────────────────
                        Text("📱 Practice Tabs", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp))
                        val tabDefs = listOf(
                            Triple("roleplay", "💬 Roleplay", "Conversation practice"),
                            Triple("flashcard", "🃏 Flashcard", "Word review"),
                            Triple("tone", "🎵 Tones", "Tone ear training"),
                            Triple("build", "🧩 Build", "Sentence building")
                        )
                        tabDefs.forEach { (id, label, desc) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(label, fontSize = 14.sp)
                                    Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = id !in disabledTabs,
                                    onCheckedChange = { enabled ->
                                        val updated = if (enabled) disabledTabs - id else disabledTabs + id
                                        scope.launch { userPrefs.saveDisabledTabs(updated) }
                                    }
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // ── Roleplay Content ──────────────────────────────────
                        Text("📖 Roleplay Content", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp))
                        val categoriesWithScenarios = remember(scenarios) {
                            ScenarioCategory.entries
                                .filter { cat -> scenarios.any { it.category == cat } }
                                .map { cat -> cat to scenarios.filter { it.category == cat } }
                        }
                        categoriesWithScenarios.forEach { (cat, catScenarios) ->
                            val catEnabled = cat.name !in disabledCategories
                            val isExpanded = cat.name in expandedCategories
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        expandedCategories = if (isExpanded)
                                            expandedCategories - cat.name
                                        else
                                            expandedCategories + cat.name
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val iconRes = categoryIconRes(cat)
                                        if (iconRes != null) {
                                            Image(
                                                painter = painterResource(iconRes),
                                                contentDescription = cat.displayName,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            Text(cat.emoji, fontSize = 14.sp)
                                        }
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "${cat.displayName}  ${if (isExpanded) "▲" else "▼"}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                Switch(
                                    checked = catEnabled,
                                    onCheckedChange = { enabled ->
                                        val updated = if (enabled) disabledCategories - cat.name
                                                      else disabledCategories + cat.name
                                        scope.launch { userPrefs.saveDisabledCategories(updated) }
                                    }
                                )
                            }
                            if (isExpanded) {
                                catScenarios.forEachIndexed { idx, scenario ->
                                    if (idx > 0) HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                                    val scenarioEnabled = scenario.id !in disabledScenarios
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "${scenario.characterEmoji} ${scenario.description}",
                                            fontSize = 13.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Switch(
                                            checked = scenarioEnabled,
                                            onCheckedChange = { enabled ->
                                                val updated = if (enabled) disabledScenarios - scenario.id
                                                              else disabledScenarios + scenario.id
                                                scope.launch { userPrefs.saveDisabledScenarios(updated) }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // ── Reset ─────────────────────────────────────────────
                        TextButton(
                            onClick = { showResetConfirm = true },
                            modifier = Modifier.padding(horizontal = 0.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("🗑️ Reset All Progress")
                        }
                    }
                    } // Box
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

    if (showAddReward) {
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
private fun RewardItem(
    reward: MilestoneReward,
    progressForCondition: (MilestoneCondition) -> Int,
    unlocked: Boolean,
    onClaim: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val conditions = reward.decodeConditions()
    val results = conditions.map { cond ->
        val cur = progressForCondition(cond)
        Triple(cond, cur, cur >= cond.targetValue)
    }
    val reached = when (reward.logic) {
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
            containerColor = if (reached && !reward.isClaimed)
                MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(reward.rewardText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    if (results.size > 1) {
                        Text(
                            if (reward.logic == "OR") "Any of:" else "All of:",
                            fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            fontSize = 11.sp,
                            color = if (met) colors.xpGainText else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (reward.isClaimed) {
                    Text("✅ Claimed", fontSize = 12.sp, color = colors.xpGainText)
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
                    progress = { overallProgress },
                    modifier = Modifier.fillMaxWidth()
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
        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(
                onClick = { typeExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    "${selectedType.emoji} ${selectedType.label}",
                    fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
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
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun StatColumn(emoji: String, value: String, label: String, labelColor: Color = Color.Unspecified, drawableRes: Int? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (drawableRes != null) {
            Image(
                painter = painterResource(drawableRes),
                contentDescription = label,
                modifier = Modifier.size(56.dp)
            )
        } else {
            Text(emoji, fontSize = 24.sp)
        }
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = labelColor)
        Text(label, fontSize = 11.sp, color = labelColor.copy(alpha = if (labelColor == Color.Unspecified) 1f else 0.7f))
    }
}
