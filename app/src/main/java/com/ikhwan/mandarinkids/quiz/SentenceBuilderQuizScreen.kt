package com.ikhwan.mandarinkids.quiz

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikhwan.mandarinkids.ProgressManager
import com.ikhwan.mandarinkids.data.models.PinyinWord
import com.ikhwan.mandarinkids.data.models.Scenario
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.playSuccessSound
import com.ikhwan.mandarinkids.playWrongSound
import com.ikhwan.mandarinkids.tts.rememberTtsManager
import com.ikhwan.mandarinkids.ui.ConfettiEffect
import com.ikhwan.mandarinkids.ui.theme.appColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class SentenceChallenge(
    val words: List<PinyinWord>,
    val originalText: String,   // full punctuated Chinese sentence — used for TTS
    val english: String,
    val indonesian: String
)

/**
 * Sentence builder quiz for mastery levels 4 and 5.
 * Level 4: build sentences from response options only.
 * Level 5: build sentences from ALL dialogue sentences.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SentenceBuilderQuizScreen(
    scenario: Scenario,
    level: Int,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val tts = rememberTtsManager()
    val scope = rememberCoroutineScope()
    val repo = remember { ProgressRepository.getInstance(context) }

    val colors = MaterialTheme.appColors
    val labelColor = colors.onLightTile
    val promptGradient = colors.tileBlue.asList()

    // Build challenge pool based on level
    val challenges: List<SentenceChallenge> = remember(scenario, level) {
        val sentences = mutableListOf<SentenceChallenge>()
        for (step in scenario.dialogues) {
            if (level == 5 && step.pinyinWords.size >= 2) {
                sentences.add(SentenceChallenge(step.pinyinWords, step.textChinese, step.textEnglish, step.textIndonesian))
            }
            for (option in step.options) {
                if (option.pinyinWords.size >= 2) {
                    sentences.add(SentenceChallenge(option.pinyinWords, option.chinese, option.english, option.indonesian))
                }
            }
        }
        sentences.distinctBy { it.words.joinToString("") { w -> w.chinese } }.shuffled()
    }

    val totalQuestions = challenges.size

    var questionIndex by remember { mutableStateOf(0) }
    var stateKey by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    var showSummary by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    val challenge = challenges.getOrNull(questionIndex)

    // Fill-in-the-blanks setup
    val setup = remember(questionIndex, stateKey) {
        val w = challenge?.words ?: emptyList()
        val preFilledPositions = (0 until w.size step 2).toSet()
        val blankPositions = (0 until w.size).filter { it % 2 != 0 }
        Triple(preFilledPositions, blankPositions, blankPositions.map { w[it] }.shuffled())
    }
    val words = challenge?.words ?: emptyList()
    val preFilledSet = setup.first
    val blankPosList = setup.second
    val blankTiles = setup.third

    var placedSlots by remember(stateKey) {
        mutableStateOf(List<Int?>(blankPosList.size) { null })
    }
    var checkResult by remember(stateKey) { mutableStateOf<Boolean?>(null) }

    val allBlanksFilled = blankPosList.isNotEmpty() && placedSlots.all { it != null }

    // Auto-check
    LaunchedEffect(allBlanksFilled, stateKey) {
        if (!allBlanksFilled || checkResult != null || challenge == null) return@LaunchedEffect

        val isCorrect = blankPosList.indices.all { slotIdx ->
            val tileIdx = placedSlots[slotIdx] ?: return@all false
            blankTiles[tileIdx].chinese == words[blankPosList[slotIdx]].chinese
        }
        checkResult = isCorrect

        if (isCorrect) {
            correctCount++
            showConfetti = true
            playSuccessSound()
            tts.speakAndAwait(
                challenge?.originalText ?: words.joinToString("") { it.chinese },
                "sbq_correct_$stateKey",
                1.0f
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
            // Keep user's placed tiles visible (red/green per slot) — correct answer shown below
            playWrongSound()
            // Show feedback for 2.5 s then auto-advance (no per-question retry)
            delay(2500)
            val next = questionIndex + 1
            if (next >= totalQuestions) {
                showSummary = true
            } else {
                questionIndex = next
                stateKey++
            }
        }
    }

    // Save progress when summary shows
    LaunchedEffect(showSummary) {
        if (!showSummary) return@LaunchedEffect
        val stars = ProgressManager.calculateStars(correctCount, totalQuestions)
        repo.saveProgress(scenario.id, stars, level)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Level $level: Build Sentences", fontSize = 16.sp)
                            Text(
                                "${scenario.title} — ${questionIndex + 1}/$totalQuestions",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { padding ->
            when {
                challenges.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📝", fontSize = 64.sp)
                            Text("No sentences to build!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onComplete) { Text("Back") }
                        }
                    }
                }

                showSummary -> {
                    val isPerfect = correctCount == totalQuestions
                    val stars = ProgressManager.calculateStars(correctCount, totalQuestions)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .navigationBarsPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            if (isPerfect) "🎉🏆" else "📝✨",
                            fontSize = 64.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            if (isPerfect) "Perfect!" else "Good effort!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "$correctCount / $totalQuestions sentences built correctly",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            repeat(3) { i ->
                                Text(
                                    if (i < stars) "★" else "☆",
                                    fontSize = 40.sp,
                                    color = if (i < stars) colors.starFilled else colors.starEmpty
                                )
                            }
                        }
                        if (isPerfect && level < 6) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "🔓 Level ${level + 1} Unlocked!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Surface(
                            onClick = onComplete,
                            shape = RoundedCornerShape(50),
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize()
                                    .background(Brush.verticalGradient(colors.actionPositive.asList())),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Done 完成", fontSize = 18.sp,
                                    color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                challenge != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .navigationBarsPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Sentence ${questionIndex + 1} / $totalQuestions",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "✅ $correctCount correct",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Prompt card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                                    .background(Brush.verticalGradient(promptGradient))
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text("Build this sentence:", fontSize = 14.sp,
                                        color = labelColor.copy(alpha = 0.7f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(challenge.english, fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.contentColorFor(colors.tileBlue))
                                    Text(challenge.indonesian, fontSize = 14.sp,
                                        color = colors.contentColorFor(colors.tileBlue).copy(alpha = 0.7f))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Sentence slots
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (i in words.indices) {
                                if (i in preFilledSet) {
                                    // Pre-filled word
                                    SbqTile(
                                        word = words[i],
                                        isPreFilled = true,
                                        isCorrect = null
                                    )
                                } else {
                                    val slotIdx = blankPosList.indexOf(i)
                                    val tileIdx = placedSlots.getOrNull(slotIdx)
                                    val placed = tileIdx?.let { blankTiles[it] }
                                    val correct = if (checkResult != null && placed != null) {
                                        placed.chinese == words[i].chinese
                                    } else null

                                    SbqSlot(
                                        placed = placed,
                                        isCorrect = correct,
                                        onClick = {
                                            if (checkResult == null && tileIdx != null) {
                                                placedSlots = placedSlots.toMutableList().also {
                                                    it[slotIdx] = null
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Tile bank
                        Text("Tap a tile to place it:", fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for ((idx, tile) in blankTiles.withIndex()) {
                                val isUsed = idx in placedSlots.filterNotNull()
                                SbqBankTile(
                                    word = tile,
                                    isUsed = isUsed,
                                    onClick = {
                                        if (checkResult != null || isUsed) return@SbqBankTile
                                        val emptySlot = placedSlots.indexOfFirst { it == null }
                                        if (emptySlot >= 0) {
                                            placedSlots = placedSlots.toMutableList().also {
                                                it[emptySlot] = idx
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        // Correct answer row — shown only after a wrong submission
                        if (checkResult == false) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                "✅ Correct answer:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (word in words) {
                                    SbqTile(word = word, isPreFilled = false, isCorrect = true)
                                }
                            }
                        }

                    }
                }
            }
        }

        if (showConfetti) {
            ConfettiEffect()
        }
    }
}

@Composable
private fun SbqTile(word: PinyinWord, isPreFilled: Boolean, isCorrect: Boolean?) {
    val colors = MaterialTheme.appColors
    val bg = when {
        isCorrect == true -> colors.actionPositive.asList()
        isCorrect == false -> colors.actionNegative.asList()
        isPreFilled -> colors.tileGrey.asList()
        else -> colors.tileAmber.asList()
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier.padding(horizontal = 3.dp)
    ) {
        Box(modifier = Modifier.background(Brush.verticalGradient(bg)).padding(horizontal = 12.dp, vertical = 8.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(word.chinese, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = if (isCorrect != null) Color.White else colors.onLightTile)
                Text(word.pinyin, fontSize = 11.sp,
                    color = if (isCorrect != null) Color.White.copy(0.8f) else colors.onLightTile.copy(0.7f))
            }
        }
    }
}

@Composable
private fun SbqSlot(placed: PinyinWord?, isCorrect: Boolean?, onClick: () -> Unit) {
    val colors = MaterialTheme.appColors
    val borderColor by animateColorAsState(
        when (isCorrect) {
            true -> Color(0xFF4CAF50)
            false -> Color(0xFFF44336)
            null -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        label = "slotBorder"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (placed != null && isCorrect != null) Color.Transparent
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .defaultMinSize(minWidth = 56.dp, minHeight = 48.dp)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .pointerInput(Unit) { detectTapGestures { onClick() } }
    ) {
        if (placed != null) {
            val bg = when (isCorrect) {
                true -> colors.actionPositive.asList()
                false -> colors.actionNegative.asList()
                null -> colors.tileAmber.asList()
            }
            Box(modifier = Modifier.background(Brush.verticalGradient(bg)).padding(horizontal = 12.dp, vertical = 8.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(placed.chinese, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = if (isCorrect != null) Color.White else colors.onLightTile)
                    Text(placed.pinyin, fontSize = 11.sp,
                        color = if (isCorrect != null) Color.White.copy(0.8f) else colors.onLightTile.copy(0.7f))
                }
            }
        } else {
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("___", fontSize = 18.sp, color = MaterialTheme.colorScheme.outline.copy(0.4f))
            }
        }
    }
}

@Composable
private fun SbqBankTile(word: PinyinWord, isUsed: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.appColors
    val alpha = if (isUsed) 0.3f else 1f
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        enabled = !isUsed,
        modifier = Modifier.padding(horizontal = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.verticalGradient(
                    colors.tileAmber.asList().map { it.copy(alpha = alpha) }
                ))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(word.chinese, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = colors.onLightTile.copy(alpha = alpha))
                Text(word.pinyin, fontSize = 11.sp,
                    color = colors.onLightTile.copy(alpha = alpha * 0.7f))
            }
        }
    }
}
