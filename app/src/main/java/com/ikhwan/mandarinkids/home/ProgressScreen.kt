package com.ikhwan.mandarinkids.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.ikhwan.mandarinkids.ui.theme.AppThemes
import com.ikhwan.mandarinkids.ui.theme.appColors
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
import com.ikhwan.mandarinkids.parent.LockMode
import com.ikhwan.mandarinkids.parent.MathDifficulty
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import com.ikhwan.mandarinkids.onboarding.LocalOnboardingCoords
import com.ikhwan.mandarinkids.onboarding.OnboardingKey
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(navController: NavController, onParentClick: () -> Unit = {}) {
    val context = LocalContext.current
    val repo = remember { ProgressRepository.getInstance(context) }
    val userPrefs = remember { UserPreferencesRepository.getInstance(context) }
    val scenarios = remember { JsonScenarioRepository.getAll() }
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.appColors
    val labelColor = colors.onLightTile
    val themeIndex by userPrefs.colorThemeIndex.collectAsState(initial = 0)
    val currentVariant = AppThemes[themeIndex.coerceIn(0, AppThemes.lastIndex)]

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
    // Count of scenarios where the user achieved 3★ at each mastery level.
    // Passing level N requires 3★ at that level, which advances masteryLevel to N+1.
    val level1Count = remember(progressMap) { progressMap.values.count { it.masteryLevel >= 2 } }
    val level2Count = remember(progressMap) { progressMap.values.count { it.masteryLevel >= 3 } }
    val level3Count = remember(progressMap) { progressMap.values.count { it.masteryLevel >= 4 } }
    val level4Count = remember(progressMap) { progressMap.values.count { it.masteryLevel >= 5 } }
    val level5Count = remember(progressMap) {
        progressMap.values.count { it.masteryLevel == 5 && it.starsAtCurrentLevel == 3 }
    }
    val highMasteryCount by repo.getHighMasteryWordCount().collectAsState(initial = 0)
    val earnedBadgeIds = remember(masteredCount, streak) { repo.getEarnedBadges() }
    val toneCorrectTotal = remember { repo.getToneCorrectTotal() }
    val sentenceCorrectTotal = remember { repo.getSentenceCorrectTotal() }
    val progressForCondition: (MilestoneCondition) -> Int = remember(
        perfectScenarioCount, highMasteryCount, highMasteryDefault,
        highMasteryListening, highMasteryReading, xp, earnedBadgeIds,
        level1Count, level2Count, level3Count, level4Count, level5Count,
        toneCorrectTotal, sentenceCorrectTotal
    ) {
        { cond ->
            when (MilestoneType.entries.find { it.name == cond.type }) {
                MilestoneType.MASTERY_DEFAULT        -> highMasteryDefault
                MilestoneType.MASTERY_LISTENING      -> highMasteryListening
                MilestoneType.MASTERY_READING        -> highMasteryReading
                MilestoneType.TOTAL_XP               -> xp
                MilestoneType.SPECIFIC_BADGE         -> if (cond.badgeId != null && cond.badgeId in earnedBadgeIds) 1 else 0
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

    val tourCoords = LocalOnboardingCoords.current
    val scenariosByCategory = remember(scenarios) { scenarios.groupBy { it.category } }
    val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }

    // ── Parent mode state for Milestone Rewards ───────────────────────────
    var rewardsUnlocked by remember { mutableStateOf(false) }
    var showPinForRewards by remember { mutableStateOf(false) }
    var showAddReward by remember { mutableStateOf(false) }

    // PIN overlay — renders over the whole screen
    if (showPinForRewards) {
        val lockModeStr = repo.getLockMode()
        val lockMode = if (lockModeStr == "MATH") LockMode.MATH else LockMode.PIN
        val mathDiff = when (repo.getMathDifficulty()) {
            "EASY" -> MathDifficulty.EASY
            "HARD" -> MathDifficulty.HARD
            else -> MathDifficulty.MEDIUM
        }
        PinScreen(
            mode = when {
                lockMode == LockMode.MATH -> PinMode.VERIFY
                repo.isPinSet() -> PinMode.VERIFY
                else -> PinMode.SET
            },
            lockMode = lockMode,
            mathDifficulty = mathDiff,
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
                    // Theme cycle button — tap to step through the 5 colour themes
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        currentVariant.palette.tileBlue.start,
                                        currentVariant.palette.tileGreen.start
                                    )
                                )
                            )
                            .clickable {
                                val next = (themeIndex + 1) % AppThemes.size
                                scope.launch { userPrefs.saveColorThemeIndex(next) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(currentVariant.emoji, fontSize = 18.sp)
                    }
                    IconButton(
                        onClick = onParentClick,
                        modifier = Modifier.onGloballyPositioned { lc ->
                            tourCoords[OnboardingKey.PARENT_LOCK] = lc.boundsInRoot()
                        }
                    ) {
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
                val statsGradient = colors.tileGreen.asList()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(statsGradient))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(
                                        painter = painterResource(streakIconRes(streak)),
                                        contentDescription = "Day streak",
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Text("$streak", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = labelColor)
                                    Text("day streak", fontSize = 11.sp, color = labelColor.copy(alpha = 0.7f))
                                }
                                HorizontalDivider(modifier = Modifier.height(72.dp).width(1.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(
                                        painter = painterResource(xpIconRes(xp)),
                                        contentDescription = "XP",
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Text("$xp XP", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = labelColor)
                                    Text(ProgressManager.getLevelLabel(xp), fontSize = 11.sp, color = labelColor.copy(alpha = 0.7f))
                                }
                                HorizontalDivider(modifier = Modifier.height(72.dp).width(1.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(
                                        painter = painterResource(R.drawable.words),
                                        contentDescription = "Words encountered",
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Text("$masteredCount", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = labelColor)
                                    Text("words", fontSize = 11.sp, color = labelColor.copy(alpha = 0.7f))
                                }
                            }

                            if (masteredCountDefault + masteredCountListening + masteredCountReading > 0) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                                Text(
                                    "★7+ mastered words",
                                    fontSize = 11.sp,
                                    color = labelColor.copy(alpha = 0.7f),
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                        .padding(bottom = 6.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    MasteryTypeChip(emoji = "🔊字", label = "Default", count = masteredCountDefault, labelColor = labelColor)
                                    MasteryTypeChip(emoji = "🔊", label = "Listening", count = masteredCountListening, labelColor = labelColor)
                                    MasteryTypeChip(emoji = "字", label = "Reading", count = masteredCountReading, labelColor = labelColor)
                                }
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
                    val catGradient = colors.categoryGradient(category.name).asList()
                    Surface(
                        onClick = { expandedCategories[category.name] = !isExpanded },
                        color = Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.verticalGradient(catGradient))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val iconRes = categoryIconRes(category)
                                if (iconRes != null) {
                                    Image(
                                        painter = painterResource(iconRes),
                                        contentDescription = category.displayName,
                                        modifier = Modifier.size(22.dp)
                                    )
                                } else {
                                    Text(category.emoji, fontSize = 16.sp)
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    category.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = labelColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "$completedInCat / ${categoryScenarios.size}",
                                    fontSize = 12.sp,
                                    color = labelColor.copy(alpha = 0.75f)
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    modifier = Modifier.size(18.dp),
                                    tint = labelColor
                                )
                            }
                        }
                    }
                }

                if (isExpanded) {
                    items(categoryScenarios, key = { it.id }) { scenario ->
                        val progress = progressMap[scenario.id]
                        val starsAtLevel = progress?.starsAtCurrentLevel ?: 0
                        val everPlayed = (progress?.stars ?: 0) > 0 || (progress?.masteryLevel ?: 1) > 1
                        val masteryLevel = progress?.masteryLevel ?: 1
                        val newLevelPending = everPlayed && starsAtLevel == 0
                        ScenarioStarRow(
                            emoji = scenario.characterEmoji,
                            title = scenario.title,
                            starsAtCurrentLevel = starsAtLevel,
                            everPlayed = everPlayed,
                            newLevelPending = newLevelPending,
                            masteryLevel = masteryLevel,
                            onClick = { navController.navigate(Routes.flashcard(scenario.id, masteryLevel)) }
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
            toneCorrectTotal = toneCorrectTotal,
            sentenceCorrectTotal = sentenceCorrectTotal,
            onDismiss = { showAddReward = false },
            onAdd = { conditions, logic, text ->
                scope.launch { repo.addReward(conditions, logic, text) }
                showAddReward = false
            }
        )
    }
}

// categoryProgressGradient removed — replaced by colors.categoryGradient(category.name)

@Composable
private fun MasteryTypeChip(emoji: String, label: String, count: Int, labelColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 16.sp, color = labelColor)
        Text("$count", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = labelColor)
        Text(label, fontSize = 10.sp, color = labelColor.copy(alpha = 0.7f))
    }
}

@Composable
private fun BadgeCard(badge: Badge, earned: Boolean, modifier: Modifier = Modifier) {
    var showInfo by remember { mutableStateOf(false) }
    val colors = MaterialTheme.appColors

    val badgeGradient = if (earned) colors.achievementUnlocked.asList()
                        else        colors.achievementLocked.asList()
    val badgeLabelColor = if (earned) colors.achievementUnlockedText
                          else        colors.textSecondary

    Card(
        onClick = { showInfo = true },
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(badgeGradient))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val badgeRes = badgeIconRes(badge)
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (badgeRes != null) {
                        Image(
                            painter = painterResource(badgeRes),
                            contentDescription = badge.label,
                            modifier = (if (earned) Modifier else Modifier.alpha(0.25f)).size(48.dp)
                        )
                    } else {
                        Text(
                            badge.emoji, fontSize = 26.sp,
                            modifier = if (earned) Modifier else Modifier.alpha(0.25f)
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    badge.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = badgeLabelColor
                )
                Spacer(Modifier.height(2.dp))
                // Always rendered to keep card height uniform; visible only when locked
                Text("🔒", fontSize = 11.sp, modifier = Modifier.alpha(if (earned) 0f else 1f))
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            icon = {
            val badgeRes = badgeIconRes(badge)
            if (badgeRes != null) {
                Image(painter = painterResource(badgeRes), contentDescription = badge.label, modifier = Modifier.size(48.dp))
            } else {
                Text(badge.emoji, fontSize = 36.sp)
            }
        },
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
    val colors = MaterialTheme.appColors
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
                            color = if (met) colors.xpGainText else MaterialTheme.colorScheme.onSurfaceVariant
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
                    color = if (isComplete) colors.xpGainText else MaterialTheme.colorScheme.primary
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
private fun ScenarioStarRow(
    emoji: String,
    title: String,
    starsAtCurrentLevel: Int,
    everPlayed: Boolean,
    newLevelPending: Boolean,
    masteryLevel: Int,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val rowGradient = if (everPlayed) colors.tileAmber.asList() else colors.tileGrey.asList()
    val rowLabelColor = colors.onLightTile

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(rowGradient))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emoji, fontSize = 26.sp, modifier = Modifier.padding(end = 12.dp))
                Text(title, fontSize = 14.sp, modifier = Modifier.weight(1f), color = rowLabelColor)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Level badge
                    if (everPlayed) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = when {
                                masteryLevel >= 5 -> Color(0xFFFFC107)
                                masteryLevel >= 3 -> MaterialTheme.colorScheme.primary
                                masteryLevel >= 2 -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ) {
                            Text(
                                text = "Lv.$masteryLevel",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    // Stars
                    Row {
                        repeat(3) { i ->
                            val filled = i < starsAtCurrentLevel
                            Text(
                                text = if (filled) "★" else "☆",
                                fontSize = 18.sp,
                                color = when {
                                    filled -> colors.starFilled
                                    newLevelPending -> colors.starFilled  // amber outline = new level waiting
                                    else -> colors.starEmpty
                                }
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Play scenario",
                        modifier = Modifier.size(18.dp),
                        tint = rowLabelColor.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
