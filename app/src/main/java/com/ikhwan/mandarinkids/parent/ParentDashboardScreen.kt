package com.ikhwan.mandarinkids.parent

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
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
import com.ikhwan.mandarinkids.data.scenarios.CustomScenarioRepository
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
import kotlinx.serialization.json.Json

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
    val toneCorrectTotal = remember { repo.getToneCorrectTotal() }
    val sentenceCorrectTotal = remember { repo.getSentenceCorrectTotal() }
    val perfectCount = allProgress.count { it.stars >= 3 }
    val level1Count = remember(allProgress) { allProgress.count { it.masteryLevel >= 2 } }
    val level2Count = remember(allProgress) { allProgress.count { it.masteryLevel >= 3 } }
    val level3Count = remember(allProgress) { allProgress.count { it.masteryLevel >= 4 } }
    val level4Count = remember(allProgress) { allProgress.count { it.masteryLevel >= 5 } }
    val level5Count = remember(allProgress) {
        allProgress.count { it.masteryLevel == 5 && it.starsAtCurrentLevel == 3 }
    }
    val progressForCondition: (MilestoneCondition) -> Int = remember(
        perfectCount, highMasteryCount, highMasteryDefault,
        highMasteryListening, highMasteryReading, xp, earnedBadges,
        level1Count, level2Count, level3Count, level4Count, level5Count,
        toneCorrectTotal, sentenceCorrectTotal
    ) {
        { cond ->
            when (MilestoneType.entries.find { it.name == cond.type }) {
                MilestoneType.MASTERY_DEFAULT        -> highMasteryDefault
                MilestoneType.MASTERY_LISTENING      -> highMasteryListening
                MilestoneType.MASTERY_READING        -> highMasteryReading
                MilestoneType.TOTAL_XP               -> xp
                MilestoneType.SPECIFIC_BADGE         -> if (cond.badgeId != null && cond.badgeId in earnedBadges) 1 else 0
                MilestoneType.LEVEL_1_COMPLETIONS    -> level1Count
                MilestoneType.LEVEL_2_COMPLETIONS    -> level2Count
                MilestoneType.LEVEL_3_COMPLETIONS    -> level3Count
                MilestoneType.LEVEL_4_COMPLETIONS    -> level4Count
                MilestoneType.LEVEL_5_COMPLETIONS    -> level5Count
                MilestoneType.TONE_CORRECT_SINCE     -> (toneCorrectTotal - cond.baselineValue).coerceAtLeast(0)
                MilestoneType.SENTENCE_CORRECT_SINCE -> (sentenceCorrectTotal - cond.baselineValue).coerceAtLeast(0)
                null                                 -> 0
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
    val customRepo = remember { CustomScenarioRepository.getInstance(context) }
    val customScenarios by customRepo.scenariosFlow.collectAsState(initial = emptyList())
    var showAddCustomScenario by remember { mutableStateOf(false) }
    var currentLockMode by remember { mutableStateOf(repo.getLockMode()) }
    var mathDifficulty by remember { mutableStateOf(repo.getMathDifficulty()) }
    var showChangePinOverlay by remember { mutableStateOf(false) }
    var showSwitchModeAuth by remember { mutableStateOf(false) }
    var pendingLockMode by remember { mutableStateOf("") }
    var showSetPinAfterSwitch by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parental Control") },
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

            // ── 3. Custom Scenarios ───────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("📝 Custom Scenarios", modifier = Modifier.weight(1f))
                    IconButton(onClick = { showAddCustomScenario = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Custom Scenario")
                    }
                }
            }

            if (customScenarios.isEmpty()) {
                item {
                    Text(
                        "No custom scenarios yet. Tap + to add one using an AI-generated JSON.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                }
            } else {
                items(customScenarios, key = { it.id }) { scenario ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${scenario.characterEmoji} ${scenario.title}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${scenario.category.displayName} · ${scenario.dialogues.size} dialogues · ${scenario.quizQuestions.size} questions",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                scope.launch { customRepo.delete(scenario.id) }
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // ── 4. Security Settings ──────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader("🔐 Security Settings")
                val securityGradient = colors.tileGrey.asList()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(securityGradient))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                            Text("🔒 Lock Method", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)

                            val lockOptions = listOf("PIN", "Math Question")
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                lockOptions.forEachIndexed { index, label ->
                                    val isSelected = (currentLockMode == "MATH") == (index == 1)
                                    SegmentedButton(
                                        selected = isSelected,
                                        onClick = {
                                            val chosen = if (index == 0) "PIN" else "MATH"
                                            if (chosen != currentLockMode) {
                                                pendingLockMode = chosen
                                                showSwitchModeAuth = true
                                            }
                                        },
                                        shape = SegmentedButtonDefaults.itemShape(index, lockOptions.size)
                                    ) { Text(label) }
                                }
                            }

                            if (currentLockMode == "MATH") {
                                HorizontalDivider()
                                Text("🧮 Math Difficulty", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val diffOptions = listOf("Easy", "Medium", "Hard")
                                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                    diffOptions.forEachIndexed { index, label ->
                                        val diffKey = label.uppercase()
                                        SegmentedButton(
                                            selected = mathDifficulty == diffKey,
                                            onClick = {
                                                mathDifficulty = diffKey
                                                repo.setMathDifficulty(diffKey)
                                            },
                                            shape = SegmentedButtonDefaults.itemShape(index, diffOptions.size)
                                        ) { Text(label) }
                                    }
                                }
                            }

                            if (currentLockMode == "PIN") {
                                HorizontalDivider()
                                TextButton(
                                    onClick = { showChangePinOverlay = true },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("🔑 Change PIN")
                                }
                            }
                        }
                    }
                }
            }

            // ── 5. Content Settings ───────────────────────────────────────────
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
            toneCorrectTotal = toneCorrectTotal,
            sentenceCorrectTotal = sentenceCorrectTotal,
            onDismiss = { showAddReward = false },
            onAdd = { conditions, logic, text ->
                scope.launch { repo.addReward(conditions, logic, text) }
                showAddReward = false
            }
        )
    }

    if (showAddCustomScenario) {
        AddCustomScenarioDialog(
            onDismiss = { showAddCustomScenario = false },
            onAdd = { json ->
                val error = customRepo.addFromJson(json)
                if (error == null) showAddCustomScenario = false
                error
            }
        )
    }

    // Change PIN overlay
    if (showChangePinOverlay) {
        PinScreen(
            mode = PinMode.CHANGE,
            lockMode = LockMode.PIN,
            onSuccess = { showChangePinOverlay = false },
            onBack = { showChangePinOverlay = false },
            onVerify = { repo.verifyPin(it) },
            onSetPin = { repo.setPin(it) }
        )
        return
    }

    // Auth overlay for lock mode switch
    if (showSwitchModeAuth) {
        val currentDifficulty = when (mathDifficulty) {
            "EASY" -> MathDifficulty.EASY
            "HARD" -> MathDifficulty.HARD
            else -> MathDifficulty.MEDIUM
        }
        PinScreen(
            mode = PinMode.VERIFY,
            lockMode = if (currentLockMode == "MATH") LockMode.MATH else LockMode.PIN,
            mathDifficulty = currentDifficulty,
            onSuccess = {
                showSwitchModeAuth = false
                repo.setLockMode(pendingLockMode)
                currentLockMode = pendingLockMode
                if (pendingLockMode == "PIN") showSetPinAfterSwitch = true
            },
            onBack = { showSwitchModeAuth = false },
            onVerify = { repo.verifyPin(it) },
            onSetPin = {}
        )
        return
    }

    // Set up PIN after switching from MATH to PIN mode
    if (showSetPinAfterSwitch) {
        PinScreen(
            mode = PinMode.SET,
            lockMode = LockMode.PIN,
            onSuccess = { showSetPinAfterSwitch = false },
            onBack = { showSetPinAfterSwitch = false },
            onVerify = { false },
            onSetPin = { repo.setPin(it) }
        )
        return
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
    toneCorrectTotal: Int,
    sentenceCorrectTotal: Int,
    onDismiss: () -> Unit,
    onAdd: (List<MilestoneCondition>, String, String) -> Unit
) {
    var conditions by remember {
        mutableStateOf(listOf(MilestoneCondition(MilestoneType.TOTAL_XP.name, 1)))
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
                            MilestoneType.TOTAL_XP.name, 1
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
                // Stamp baselines for goal-based counters so progress is measured from now.
                val finalConditions = conditions.map { cond ->
                    when (MilestoneType.entries.find { it.name == cond.type }) {
                        MilestoneType.TONE_CORRECT_SINCE     -> cond.copy(baselineValue = toneCorrectTotal)
                        MilestoneType.SENTENCE_CORRECT_SINCE -> cond.copy(baselineValue = sentenceCorrectTotal)
                        else -> cond
                    }
                }
                onAdd(finalConditions, logic, rewardText)
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
        ?: MilestoneType.TOTAL_XP
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
                    onDismissRequest = { badgeExpanded = false },
                    modifier = Modifier.heightIn(max = 280.dp)
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

private val SCENARIO_AI_PROMPT = """
You are a Mandarin learning content creator for a children's app (ages 4–8). Generate a scenario JSON following this specification EXACTLY. Any deviation will cause the app to reject the file.

════════════════════════════════════════
VALID ENUM VALUES — USE ONLY THESE EXACT STRINGS
════════════════════════════════════════

"category" — pick exactly one:
  ESSENTIALS | AT_SCHOOL | SCHOOL_SUBJECTS | FOOD_AND_EATING |
  FEELINGS_AND_HEALTH | PLAY_AND_HOBBIES | HOME | OUT_AND_ABOUT

"speaker" — pick exactly one:
  CHARACTER | STUDENT

"responseType" — pick exactly one:
  LISTEN_ONLY | SINGLE_CHOICE | MULTIPLE_OPTIONS | TEXT_INPUT

"direction" (quiz) — pick exactly one:
  CHINESE_TO_TRANSLATION | TRANSLATION_TO_CHINESE | AUDIO_TO_TRANSLATION

⛔ DO NOT USE any other values. In particular, these are INVALID and will break the app:
  PINYIN_TO_CHINESE, CHINESE_TO_PINYIN, AUDIO_TO_CHINESE, PINYIN_TO_TRANSLATION,
  LISTEN, CHOICE, MULTIPLE, INPUT, AUDIO, PINYIN — none of these exist.

════════════════════════════════════════
COMPLETE JSON FORMAT
════════════════════════════════════════

{
  "id": "custom_unique_snake_case_id",
  "title": "Chinese title — e.g. 今天天气怎么样？",
  "description": "One-line English description",
  "characterName": "Character display name",
  "characterEmoji": "🌤️",
  "characterRole": "friend",
  "category": "OUT_AND_ABOUT",
  "dialogues": [
    {
      "id": 1,
      "speaker": "CHARACTER",
      "textChinese": "你好！今天天气很好。",
      "textPinyin": "Nǐ hǎo! Jīntiān tiānqì hěn hǎo.",
      "textEnglish": "Hello! The weather is great today.",
      "textIndonesian": "Halo! Cuaca hari ini sangat bagus.",
      "pinyinWords": [
        { "pinyin": "Nǐ hǎo", "chinese": "你好", "english": "Hello", "indonesian": "Halo" },
        { "pinyin": "jīntiān", "chinese": "今天", "english": "today", "indonesian": "hari ini" },
        { "pinyin": "tiānqì", "chinese": "天气", "english": "weather", "indonesian": "cuaca" },
        { "pinyin": "hěn", "chinese": "很", "english": "very", "indonesian": "sangat" },
        { "pinyin": "hǎo", "chinese": "好", "english": "good", "indonesian": "bagus" }
      ],
      "responseType": "LISTEN_ONLY",
      "options": []
    },
    {
      "id": 2,
      "speaker": "CHARACTER",
      "textChinese": "你喜欢什么天气？",
      "textPinyin": "Nǐ xǐhuān shénme tiānqì?",
      "textEnglish": "What kind of weather do you like?",
      "textIndonesian": "Cuaca seperti apa yang kamu suka?",
      "pinyinWords": [
        { "pinyin": "Nǐ", "chinese": "你", "english": "you", "indonesian": "kamu" },
        { "pinyin": "xǐhuān", "chinese": "喜欢", "english": "like", "indonesian": "suka" },
        { "pinyin": "shénme", "chinese": "什么", "english": "what", "indonesian": "apa" },
        { "pinyin": "tiānqì", "chinese": "天气", "english": "weather", "indonesian": "cuaca" }
      ],
      "responseType": "SINGLE_CHOICE",
      "options": [
        {
          "chinese": "我喜欢晴天！",
          "pinyin": "Wǒ xǐhuān qíngtiān!",
          "english": "I like sunny days!",
          "indonesian": "Aku suka hari cerah!",
          "pinyinWords": [
            { "pinyin": "Wǒ", "chinese": "我", "english": "I", "indonesian": "Aku" },
            { "pinyin": "xǐhuān", "chinese": "喜欢", "english": "like", "indonesian": "suka" },
            { "pinyin": "qíngtiān", "chinese": "晴天", "english": "sunny day", "indonesian": "hari cerah" }
          ],
          "isCorrect": true
        },
        {
          "chinese": "我喜欢下雨！",
          "pinyin": "Wǒ xǐhuān xià yǔ!",
          "english": "I like rain!",
          "indonesian": "Aku suka hujan!",
          "pinyinWords": [
            { "pinyin": "Wǒ", "chinese": "我", "english": "I", "indonesian": "Aku" },
            { "pinyin": "xǐhuān", "chinese": "喜欢", "english": "like", "indonesian": "suka" },
            { "pinyin": "xià yǔ", "chinese": "下雨", "english": "rain", "indonesian": "hujan" }
          ],
          "isCorrect": false
        }
      ]
    },
    {
      "id": 3,
      "speaker": "CHARACTER",
      "textChinese": "下雨的时候你带雨伞吗？",
      "textPinyin": "Xià yǔ de shíhou nǐ dài yǔsǎn ma?",
      "textEnglish": "Do you bring an umbrella when it rains?",
      "textIndonesian": "Kamu bawa payung saat hujan?",
      "pinyinWords": [
        { "pinyin": "xià yǔ", "chinese": "下雨", "english": "rain", "indonesian": "hujan" },
        { "pinyin": "de", "chinese": "的", "english": "(particle)", "indonesian": "(partikel)" },
        { "pinyin": "shíhou", "chinese": "时候", "english": "when", "indonesian": "saat" },
        { "pinyin": "nǐ", "chinese": "你", "english": "you", "indonesian": "kamu" },
        { "pinyin": "dài", "chinese": "带", "english": "bring", "indonesian": "membawa" },
        { "pinyin": "yǔsǎn", "chinese": "雨伞", "english": "umbrella", "indonesian": "payung" },
        { "pinyin": "ma", "chinese": "吗", "english": "(question particle)", "indonesian": "(partikel tanya)" }
      ],
      "responseType": "SINGLE_CHOICE",
      "options": [
        {
          "chinese": "带，我带雨伞。",
          "pinyin": "Dài, wǒ dài yǔsǎn.",
          "english": "Yes, I bring an umbrella.",
          "indonesian": "Ya, aku bawa payung.",
          "pinyinWords": [
            { "pinyin": "Dài", "chinese": "带", "english": "bring", "indonesian": "membawa" },
            { "pinyin": "wǒ", "chinese": "我", "english": "I", "indonesian": "aku" },
            { "pinyin": "dài", "chinese": "带", "english": "bring", "indonesian": "membawa" },
            { "pinyin": "yǔsǎn", "chinese": "雨伞", "english": "umbrella", "indonesian": "payung" }
          ],
          "isCorrect": true
        },
        {
          "chinese": "不带。",
          "pinyin": "Bù dài.",
          "english": "No, I don't.",
          "indonesian": "Tidak.",
          "pinyinWords": [
            { "pinyin": "Bù", "chinese": "不", "english": "no/not", "indonesian": "tidak" },
            { "pinyin": "dài", "chinese": "带", "english": "bring", "indonesian": "membawa" }
          ],
          "isCorrect": false
        }
      ]
    }
  ],
  "quizQuestions": [
    {
      "direction": "TRANSLATION_TO_CHINESE",
      "questionText": "How do you say 'weather' in Mandarin?",
      "questionChinese": "",
      "questionPinyin": "",
      "options": [
        { "chinese": "天空", "pinyin": "tiānkōng", "translation": "sky" },
        { "chinese": "下雨", "pinyin": "xià yǔ", "translation": "raining" },
        { "chinese": "天气", "pinyin": "tiānqì", "translation": "weather" },
        { "chinese": "太阳", "pinyin": "tàiyáng", "translation": "sun" }
      ],
      "correctAnswerIndex": 2,
      "explanation": "天气 (tiānqì) means weather. It is at index 2 (the 3rd option)."
    },
    {
      "direction": "CHINESE_TO_TRANSLATION",
      "questionText": "What does this mean?",
      "questionChinese": "晴天",
      "questionPinyin": "qíngtiān",
      "options": [
        { "chinese": "", "pinyin": "", "translation": "cloudy day" },
        { "chinese": "", "pinyin": "", "translation": "sunny day" },
        { "chinese": "", "pinyin": "", "translation": "rainy day" },
        { "chinese": "", "pinyin": "", "translation": "snowy day" }
      ],
      "correctAnswerIndex": 1,
      "explanation": "晴天 (qíngtiān) means sunny day. It is at index 1 (the 2nd option)."
    },
    {
      "direction": "AUDIO_TO_TRANSLATION",
      "questionText": "Listen and choose the correct meaning!",
      "questionChinese": "雨伞",
      "questionPinyin": "yǔsǎn",
      "options": [
        { "chinese": "hat", "pinyin": "", "translation": "hat" },
        { "chinese": "raincoat", "pinyin": "", "translation": "raincoat" },
        { "chinese": "boots", "pinyin": "", "translation": "boots" },
        { "chinese": "umbrella", "pinyin": "", "translation": "umbrella" }
      ],
      "correctAnswerIndex": 3,
      "explanation": "雨伞 (yǔsǎn) means umbrella. It is at index 3 (the 4th option)."
    }
  ]
}

════════════════════════════════════════
RULES — follow all of these strictly
════════════════════════════════════════

1. "id": must start with "custom_", use only lowercase letters, digits, underscores. Example: custom_at_the_park
2. "dialogues": include at least 3 steps. Mix responseType values (some LISTEN_ONLY, some SINGLE_CHOICE).
3. "pinyinWords": REQUIRED on every dialogue step and every option. Cover every word — do NOT skip particles (吗, 了, 的, 啊, 呢, etc.).
4. "quizQuestions": include 3–5 questions. Use a mix of CHINESE_TO_TRANSLATION, TRANSLATION_TO_CHINESE, and AUDIO_TO_TRANSLATION.
5. Each quiz question must have EXACTLY 4 options. "correctAnswerIndex" is 0-based (0 = first option, 3 = last option).
   ⚠️ RANDOMIZE correctAnswerIndex across all questions — NEVER put the correct answer at index 0 for every question.
   Spread the correct answer across different positions. Example: Q1→index 2, Q2→index 0, Q3→index 3, Q4→index 1.
   The example above intentionally uses indices 2, 1, 3 to show this. Always place the correct answer text at the position matching correctAnswerIndex.
6. For CHINESE_TO_TRANSLATION and AUDIO_TO_TRANSLATION questions, the options only need "translation" filled in; "chinese" and "pinyin" can be empty strings "".
7. For TRANSLATION_TO_CHINESE questions, fill in "chinese" and "pinyin" for each option; "translation" is the English label.
8. All Chinese text must use simplified characters.
9. Include both English AND Indonesian (Bahasa Indonesia) translations for ALL text fields.
10. Output ONLY the raw JSON object. No markdown code fences, no explanation text around it.

Now generate a scenario about: [DESCRIBE YOUR TOPIC HERE — e.g. "going to the market" or "playing at the park"]
""".trimIndent()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomScenarioDialog(
    onDismiss: () -> Unit,
    onAdd: suspend (String) -> String?   // returns error string or null on success
) {
    var jsonInput by remember { mutableStateOf("") }
    var validatedScenario by remember { mutableStateOf<com.ikhwan.mandarinkids.data.models.Scenario?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val json = remember { Json { ignoreUnknownKeys = true; coerceInputValues = true } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Scenario") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── Instructions ─────────────────────────────────────────────────
                Text(
                    "1. Copy the AI prompt below.\n2. Paste it into Claude or ChatGPT and describe your topic.\n3. Copy the generated JSON and paste it here.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // ── Copy prompt button ────────────────────────────────────────────
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(SCENARIO_AI_PROMPT))
                        Toast.makeText(context, "AI prompt copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copy AI Prompt", fontSize = 13.sp)
                }

                HorizontalDivider()

                // ── JSON input ────────────────────────────────────────────────────
                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = {
                        jsonInput = it
                        validatedScenario = null
                        validationError = null
                    },
                    label = { Text("Paste JSON here") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 260.dp),
                    maxLines = 30,
                    isError = validationError != null
                )

                // ── Validation error ──────────────────────────────────────────────
                if (validationError != null) {
                    Text(
                        "⚠️ ${validationError}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // ── Validate button ───────────────────────────────────────────────
                if (validatedScenario == null) {
                    Button(
                        onClick = {
                            val trimmed = jsonInput.trim()
                            if (trimmed.isBlank()) {
                                validationError = "Please paste the JSON first."
                                return@Button
                            }
                            try {
                                val parsed = json.decodeFromString<com.ikhwan.mandarinkids.data.models.Scenario>(trimmed)
                                // Basic field checks (full checks happen in repo on save)
                                when {
                                    parsed.id.isBlank() -> validationError = "\"id\" field is missing or blank."
                                    parsed.title.isBlank() -> validationError = "\"title\" field is missing or blank."
                                    parsed.dialogues.isEmpty() -> validationError = "No dialogue steps found."
                                    parsed.quizQuestions.isEmpty() -> validationError = "No quiz questions found."
                                    parsed.dialogues.any { it.pinyinWords.isEmpty() } ->
                                        validationError = "Some dialogue steps are missing pinyinWords."
                                    else -> {
                                        validatedScenario = parsed
                                        validationError = null
                                    }
                                }
                            } catch (e: Exception) {
                                validationError = "Cannot parse JSON: ${e.message?.take(100)}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Validate JSON") }
                }

                // ── Preview (after successful validation) ─────────────────────────
                validatedScenario?.let { s ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("✅ Valid scenario", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("${s.characterEmoji} ${s.title}", fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text(s.description, fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("Category: ${s.category.displayName}", fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("${s.dialogues.size} dialogues · ${s.quizQuestions.size} quiz questions",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (validatedScenario != null) {
                Button(
                    onClick = {
                        if (isSaving) return@Button
                        isSaving = true
                        scope.launch {
                            val error = onAdd(jsonInput.trim())
                            isSaving = false
                            if (error != null) {
                                validationError = error
                                validatedScenario = null
                            }
                            // success: dialog dismissed by caller
                        }
                    },
                    enabled = !isSaving
                ) { Text(if (isSaving) "Saving…" else "Save Scenario") }
            } else {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
        dismissButton = {
            if (validatedScenario != null) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
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
