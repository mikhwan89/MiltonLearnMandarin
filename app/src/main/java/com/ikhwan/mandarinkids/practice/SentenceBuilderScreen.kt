package com.ikhwan.mandarinkids.practice

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.ikhwan.mandarinkids.ui.theme.appColors
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.ikhwan.mandarinkids.data.models.PinyinWord
import com.ikhwan.mandarinkids.data.models.ScenarioCategory
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.db.Badge
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.playSuccessSound
import com.ikhwan.mandarinkids.playWrongSound
import com.ikhwan.mandarinkids.preferences.UserPreferencesRepository
import com.ikhwan.mandarinkids.tts.rememberTtsManager
import com.ikhwan.mandarinkids.ui.ConfettiEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SESSION_LENGTH = 10

private data class SentenceQuestion(
    val words: List<PinyinWord>,
    val english: String,
    val indonesian: String,
    val category: ScenarioCategory
)

private data class FillInBlanksSetup(
    val words: List<PinyinWord>,
    val preFilledSet: Set<Int>,      // positions shown pre-filled (even indices)
    val blankPosList: List<Int>,     // positions the user must fill (odd indices)
    val blankTiles: List<PinyinWord> // shuffled tiles shown in the bank
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SentenceBuilderScreen() {
    val context = LocalContext.current
    val tts = rememberTtsManager()
    val scope = rememberCoroutineScope()
    val repo      = remember { ProgressRepository.getInstance(context) }
    val userPrefs = remember { UserPreferencesRepository.getInstance(context) }
    val showIndonesian by userPrefs.showIndonesian.collectAsState(initial = true)
    val density = LocalDensity.current

    val colors = MaterialTheme.appColors
    val labelColor = colors.onLightTile
    val promptGradient = colors.tileBlue.asList()

    // ── Question pool — built once from all scenarios ─────────────────────────
    val questionPool: List<SentenceQuestion> = remember {
        JsonScenarioRepository.getAll()
            .flatMap { scenario ->
                scenario.dialogues
                    .filter { step -> step.pinyinWords.size >= 2 }
                    .map { step ->
                        SentenceQuestion(
                            words      = step.pinyinWords,
                            english    = step.textEnglish,
                            indonesian = step.textIndonesian,
                            category   = scenario.category
                        )
                    }
            }
            .shuffled()
    }

    // ── Category filter ───────────────────────────────────────────────────────
    var selectedCategory by remember { mutableStateOf<ScenarioCategory?>(null) }
    val availableCategories = remember(questionPool) {
        questionPool.map { it.category }.distinct()
    }
    val activePool = remember(selectedCategory) {
        if (selectedCategory == null) questionPool
        else questionPool.filter { it.category == selectedCategory }
    }

    // ── Speed toggle ──────────────────────────────────────────────────────────
    var slowMode by remember { mutableStateOf(false) }
    val ttsRate = if (slowMode) 0.7f else 1.0f

    // ── Drawer ────────────────────────────────────────────────────────────────
    val drawerWidthPx = with(density) { 220.dp.toPx() }
    val closedPx = -drawerWidthPx
    val offsetX = remember { Animatable(closedPx) }

    // ── Session state — sessionKey resets everything when category changes ────
    var sessionKey by remember { mutableStateOf(0) }
    var questionIndex by remember(sessionKey) { mutableStateOf(0) }
    var stateKey     by remember(sessionKey) { mutableStateOf(0) }
    var correctCount by remember(sessionKey) { mutableStateOf(0) }
    var totalAnswered by remember(sessionKey) { mutableStateOf(0) }
    var showSummary  by remember(sessionKey) { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var showInfo     by remember { mutableStateOf(false) }

    val totalQuestions = minOf(SESSION_LENGTH, activePool.size)
    val question = if (activePool.isNotEmpty()) activePool[questionIndex] else null

    // ── Fill-in-the-blanks setup ──────────────────────────────────────────────
    // Keyed on both sessionKey and questionIndex so it always matches activePool.
    val setup: FillInBlanksSetup = remember(sessionKey, questionIndex) {
        val w = activePool.getOrNull(questionIndex)?.words ?: emptyList()
        val preFilledPositions = (0 until w.size step 2).toSet()
        val blankPositions = (0 until w.size).filter { it % 2 != 0 }
        FillInBlanksSetup(
            words        = w,
            preFilledSet = preFilledPositions,
            blankPosList = blankPositions,
            blankTiles   = blankPositions.map { w[it] }.shuffled()
        )
    }
    val words        = setup.words
    val preFilledSet = setup.preFilledSet
    val blankPosList = setup.blankPosList
    val blankTiles   = setup.blankTiles

    // Keyed on sessionKey too — avoids stale state when session resets to stateKey=0
    var placedSlots by remember(sessionKey, stateKey) {
        mutableStateOf(List<Int?>(setup.blankPosList.size) { null })
    }
    var checkResult by remember(sessionKey, stateKey) { mutableStateOf<Boolean?>(null) }

    val allBlanksFilled = blankPosList.isNotEmpty() && placedSlots.all { it != null }

    // Auto-check when every blank is filled
    LaunchedEffect(allBlanksFilled, stateKey, sessionKey) {
        if (!allBlanksFilled || checkResult != null || question == null) return@LaunchedEffect

        val isCorrect = blankPosList.indices.all { slotIdx ->
            val tileIdx = placedSlots[slotIdx] ?: return@all false
            blankTiles[tileIdx].chinese == words[blankPosList[slotIdx]].chinese
        }
        checkResult = isCorrect

        if (isCorrect) {
            correctCount++
            totalAnswered++
            repo.addSentenceBuilderXp(10)
            showConfetti = true
            playSuccessSound()
            tts.speakAndAwait(
                words.joinToString("") { it.chinese },
                "sb_correct_$stateKey",
                ttsRate
            )
            delay(600)
            showConfetti = false
            val next = questionIndex + 1
            if (next >= totalQuestions) {
                showSummary = true
            } else {
                questionIndex = next
                stateKey++
            }
        } else {
            playWrongSound()
        }
    }

    LaunchedEffect(showSummary) {
        if (showSummary && totalAnswered > 0 && correctCount == totalAnswered) {
            repo.awardBadge(Badge.PERFECT_BUILDER.id)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            activePool.isEmpty() -> SbEmptyState()

            showSummary -> SentenceBuilderSummary(
                correct  = correctCount,
                total    = totalAnswered,
                onRestart = { sessionKey++ }
            )

            question != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── Progress row ──────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (availableCategories.size > 1) {
                                IconButton(
                                    onClick  = { scope.launch { offsetX.animateTo(0f, spring()) } },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = "Filter by category",
                                        modifier = Modifier.size(18.dp),
                                        tint = if (selectedCategory != null)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                    )
                                }
                            }
                            Text(
                                "Question ${questionIndex + 1} / $totalQuestions",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // ── Speed toggle ──────────────────────────────────
                            OutlinedButton(
                                onClick = { slowMode = !slowMode },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier.height(30.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (slowMode)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(if (slowMode) "0.7x 🐢" else "1x", fontSize = 12.sp)
                            }
                            Text(
                                "✅ $correctCount correct",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Prompt card ───────────────────────────────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.verticalGradient(promptGradient))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "🧩 Fill in the blanks:",
                                    fontSize = 13.sp,
                                    color = labelColor.copy(alpha = 0.75f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    question.english,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = labelColor,
                                    lineHeight = 28.sp
                                )
                                if (showIndonesian) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "🇮🇩 ${question.indonesian}",
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        color = labelColor.copy(alpha = 0.75f),
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Answer area ───────────────────────────────────────────
                    val answerBgColor by animateColorAsState(
                        targetValue = when (checkResult) {
                            true  -> colors.answerCorrect.start.copy(alpha = 0.15f)
                            false -> colors.actionNegative.start.copy(alpha = 0.12f)
                            null  -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        animationSpec = tween(300),
                        label = "answerBg"
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 88.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = answerBgColor
                    ) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(
                                6.dp, Alignment.CenterHorizontally
                            ),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            (0 until words.size).forEach { pos ->
                                if (pos in preFilledSet) {
                                    SbWordTile(
                                        word    = words[pos],
                                        enabled = false,
                                        tint    = if (checkResult == false) TileColor.Correct
                                                  else TileColor.Locked,
                                        colors  = colors,
                                        onClick = {}
                                    )
                                } else {
                                    val slotIdx = blankPosList.indexOf(pos)
                                    val tileIdx = placedSlots[slotIdx]
                                    if (tileIdx == null) {
                                        SbBlankSlot()
                                    } else {
                                        val tileColor = when (checkResult) {
                                            true  -> TileColor.Correct
                                            false -> if (blankTiles[tileIdx].chinese == words[pos].chinese)
                                                         TileColor.Correct else TileColor.Wrong
                                            null  -> TileColor.Normal
                                        }
                                        SbWordTile(
                                            word    = blankTiles[tileIdx],
                                            enabled = checkResult == null,
                                            tint    = tileColor,
                                            colors  = colors,
                                            onClick = {
                                                if (checkResult == null) {
                                                    placedSlots = placedSlots.toMutableList()
                                                        .also { it[slotIdx] = null }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Result feedback ───────────────────────────────────────
                    when (checkResult) {
                        true -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "🎉 Correct! Well done!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.answerCorrectText
                            )
                            Text(
                                "+10 XP ✨",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.xpGainText
                            )
                        }
                        false -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Not quite! Correct order:",
                                fontSize = 14.sp,
                                color = colors.answerWrongText,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(
                                    6.dp, Alignment.CenterHorizontally
                                ),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                question.words.forEach { word ->
                                    SbWordTile(
                                        word    = word,
                                        enabled = false,
                                        tint    = TileColor.Correct,
                                        colors  = colors,
                                        onClick = {}
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                onClick = {
                                    scope.launch {
                                        tts.speakAndAwait(
                                            question.words.joinToString("") { it.chinese },
                                            "sb_hear_$stateKey",
                                            ttsRate
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                color = Color.Transparent,
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.verticalGradient(colors.tileBlue.asList())),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Hear it",
                                        fontSize = 15.sp,
                                        color = colors.onLightTile,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    onClick = { stateKey++ },
                                    shape = RoundedCornerShape(50),
                                    color = Color.Transparent,
                                    modifier = Modifier.weight(1f).height(52.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Brush.verticalGradient(colors.tileAmber.asList())),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Try Again",
                                            fontSize = 15.sp,
                                            color = colors.onLightTile,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Surface(
                                    onClick = {
                                        totalAnswered++
                                        val next = questionIndex + 1
                                        if (next >= totalQuestions) {
                                            showSummary = true
                                        } else {
                                            questionIndex = next
                                            stateKey++
                                        }
                                    },
                                    shape = RoundedCornerShape(50),
                                    color = Color.Transparent,
                                    modifier = Modifier.weight(1f).height(52.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Brush.verticalGradient(colors.actionPositive.asList())),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Next →",
                                            fontSize = 15.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                        null -> {}
                    }

                    // ── Tile bank ─────────────────────────────────────────────
                    if (checkResult == null) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "Word tiles:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                8.dp, Alignment.CenterHorizontally
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            blankTiles.forEachIndexed { idx, tile ->
                                val isPlaced = idx in placedSlots.filterNotNull()
                                SbWordTile(
                                    word    = tile,
                                    enabled = !isPlaced,
                                    tint    = if (isPlaced) TileColor.Faded else TileColor.Normal,
                                    colors  = colors,
                                    onClick = {
                                        if (!isPlaced) {
                                            val firstEmpty = placedSlots.indexOfFirst { it == null }
                                            if (firstEmpty >= 0) {
                                                placedSlots = placedSlots.toMutableList()
                                                    .also { it[firstEmpty] = idx }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        if (showConfetti) ConfettiEffect()

        // ── Scrim ─────────────────────────────────────────────────────────────
        val scrimAlpha = ((offsetX.value - closedPx) / (-closedPx) * 0.4f).coerceIn(0f, 0.4f)
        if (scrimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            scope.launch { offsetX.animateTo(closedPx, spring()) }
                        }
                    }
            )
        }

        // ── Category drawer ───────────────────────────────────────────────────
        Surface(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                val target = if (offsetX.value > closedPx / 2) 0f else closedPx
                                offsetX.animateTo(target, spring())
                            }
                        }
                    ) { _, dragAmount ->
                        scope.launch {
                            offsetX.snapTo((offsetX.value + dragAmount).coerceIn(closedPx, 0f))
                        }
                    }
                },
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    "Filter by Category",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                NavigationDrawerItem(
                    label    = { Text("🌐 All Categories") },
                    selected = selectedCategory == null,
                    onClick  = {
                        selectedCategory = null
                        sessionKey++
                        scope.launch { offsetX.animateTo(closedPx, spring()) }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                availableCategories.forEach { cat ->
                    NavigationDrawerItem(
                        label    = { Text("${cat.emoji} ${cat.displayName}") },
                        selected = selectedCategory == cat,
                        onClick  = {
                            selectedCategory = cat
                            sessionKey++
                            scope.launch { offsetX.animateTo(closedPx, spring()) }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

        // Info icon — top right
        IconButton(
            onClick  = { showInfo = true },
            modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().size(36.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "Sentence structure guide",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            )
        }
        if (showInfo) { SentenceStructureDialog(onDismiss = { showInfo = false }) }
    }
}

// ── Blank slot placeholder ────────────────────────────────────────────────────

@Composable
private fun SbBlankSlot() {
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 64.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f),
                shape = RoundedCornerShape(20.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Mirror SbWordTile's padding and font sizes so height matches exactly
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = "?",
                fontSize   = 26.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f)
            )
            Text(
                text  = " ",   // invisible spacer matching the pinyin row height
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0f)
            )
        }
    }
}

// ── Word tile ─────────────────────────────────────────────────────────────────

private enum class TileColor { Normal, Correct, Wrong, Faded, Locked }

// Gradient and text color for SbWordTile are resolved via AppColorScheme (see SbWordTile below)

@Composable
private fun SbWordTile(
    word: PinyinWord,
    enabled: Boolean,
    tint: TileColor,
    colors: com.ikhwan.mandarinkids.ui.theme.AppColorScheme,
    onClick: () -> Unit
) {
    val gradientColors = when (tint) {
        TileColor.Normal  -> colors.tileAmber.asList()
        TileColor.Locked  -> colors.tilePurple.asList()
        TileColor.Correct -> colors.actionPositive.asList()
        TileColor.Wrong   -> colors.actionNegative.asList()
        TileColor.Faded   -> colors.tileGrey.asList()
    }
    val textColor = when (tint) {
        TileColor.Normal  -> colors.onLightTile
        TileColor.Locked  -> colors.onLightTile
        TileColor.Correct -> Color.White
        TileColor.Wrong   -> Color.White
        TileColor.Faded   -> Color.White.copy(alpha = 0.45f)
    }

    Surface(
        onClick         = onClick,
        enabled         = enabled,
        shape           = RoundedCornerShape(20.dp),
        color           = Color.Transparent,
        shadowElevation = if (enabled && tint == TileColor.Normal) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.background(Brush.verticalGradient(gradientColors))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text       = word.chinese,
                    fontSize   = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color      = textColor
                )
                Text(
                    text     = word.pinyin,
                    fontSize = 11.sp,
                    color    = textColor.copy(alpha = 0.75f)
                )
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun SbEmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("📭", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "No sentences yet",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Complete some scenario lessons first to unlock sentence building.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
        }
    }
}

// ── Summary screen ────────────────────────────────────────────────────────────

@Composable
private fun SentenceBuilderSummary(
    correct: Int,
    total: Int,
    onRestart: () -> Unit
) {
    val isPerfect = total > 0 && correct == total
    var showConfetti by remember { mutableStateOf(isPerfect) }
    LaunchedEffect(Unit) {
        if (isPerfect) {
            delay(2800)
            showConfetti = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(if (isPerfect) "🎉" else "👏", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                if (isPerfect) "Perfect Score!" else "Well Done!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SbStatItem("Sentences", "$total", "📋")
                SbStatItem("Correct", "$correct", "✅")
                SbStatItem(
                    "Score",
                    "${if (total > 0) correct * 100 / total else 0}%",
                    "🏆"
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                when {
                    isPerfect            -> "Every blank filled perfectly — you're a Mandarin star! 🌟"
                    correct >= total / 2 -> "Good work! Keep practising to master all the sentences."
                    else                 -> "Keep going — each attempt helps your brain remember!"
                },
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            val summaryColors = MaterialTheme.appColors
            Surface(
                onClick = onRestart,
                shape = RoundedCornerShape(50),
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(summaryColors.actionPositive.asList())),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Play Again", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (showConfetti) ConfettiEffect()
    }
}

@Composable
private fun SbStatItem(label: String, value: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 28.sp)
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SentenceStructureDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How Mandarin Sentences Work", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Mandarin follows the same SVO order as English.",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    "Mandarin uses a Subject + Verb + Object (SVO) structure, just like English, making basic sentences straightforward to form.",
                    fontSize = 13.sp
                )
                Text(
                    "For example, 'I eat apples' translates directly as:",
                    fontSize = 13.sp
                )
                Text(
                    "我吃苹果 (Wǒ chī píngguǒ)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "• 我 (wǒ) — I (subject)\n• 吃 (chī) — eat (verb)\n• 苹果 (píngguǒ) — apples (object)",
                    fontSize = 13.sp
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Key Differences", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    "Mandarin has no verb tenses or articles. Instead, it uses context, particles (like 了 for completed actions), or time words before the verb.",
                    fontSize = 13.sp
                )
                Text(
                    "Questions don't change word order — just add 吗 (ma) at the end:",
                    fontSize = 13.sp
                )
                Text(
                    "你吃苹果吗？(Nǐ chī píngguǒ ma?)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text("'Do you eat apples?'", fontSize = 13.sp)
                Text(
                    "With two objects (indirect + direct), use:\nSubject + Verb + Indirect Object + Direct Object",
                    fontSize = 13.sp
                )
                Text(
                    "他给我买书 (Tā gěi wǒ mǎi shū)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text("'He buys me a book'", fontSize = 13.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Got it!") }
        }
    )
}
