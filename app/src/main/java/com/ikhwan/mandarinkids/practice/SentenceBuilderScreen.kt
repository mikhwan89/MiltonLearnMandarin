package com.ikhwan.mandarinkids.practice

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.ikhwan.mandarinkids.data.models.PinyinWord
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.db.Badge
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.playSuccessSound
import com.ikhwan.mandarinkids.playWrongSound
import com.ikhwan.mandarinkids.tts.rememberTtsManager
import com.ikhwan.mandarinkids.ui.ConfettiEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SESSION_LENGTH = 10

private data class SentenceQuestion(
    val words: List<PinyinWord>,
    val english: String,
    val scenarioTitle: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SentenceBuilderScreen() {
    val context = LocalContext.current
    val tts = rememberTtsManager()
    val scope = rememberCoroutineScope()
    val repo = remember { ProgressRepository.getInstance(context) }

    val questionPool: List<SentenceQuestion> = remember {
        JsonScenarioRepository.getAll()
            .flatMap { scenario ->
                scenario.dialogues
                    .filter { step -> step.pinyinWords.size >= 2 }
                    .map { step ->
                        SentenceQuestion(
                            words = step.pinyinWords,
                            english = step.textEnglish,
                            scenarioTitle = scenario.title
                        )
                    }
            }
            .shuffled()
    }

    val totalQuestions = minOf(SESSION_LENGTH, questionPool.size)

    // Session-level state
    var questionIndex by remember { mutableStateOf(0) }
    var stateKey     by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    var totalAnswered by remember { mutableStateOf(0) }
    var showSummary  by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    // Per-attempt state — all keyed on stateKey so they reset on advance or Try Again
    val question     = if (questionPool.isNotEmpty()) questionPool[questionIndex] else null
    val shuffledTiles = remember(stateKey) { question?.words?.shuffled() ?: emptyList() }
    var placedIndices by remember(stateKey) { mutableStateOf<List<Int>>(emptyList()) }
    var checkResult   by remember(stateKey) { mutableStateOf<Boolean?>(null) }

    val allPlaced = shuffledTiles.isNotEmpty() && placedIndices.size == shuffledTiles.size

    // Auto-check when every tile has been placed
    LaunchedEffect(allPlaced, stateKey) {
        if (!allPlaced || checkResult != null || question == null) return@LaunchedEffect

        val isCorrect = placedIndices.map { shuffledTiles[it].chinese } ==
            question.words.map { it.chinese }
        checkResult = isCorrect

        if (isCorrect) {
            correctCount++
            totalAnswered++
            repo.addSentenceBuilderXp(10)
            showConfetti = true
            playSuccessSound()
            tts.speakAndAwait(
                question.words.joinToString("") { it.chinese },
                "sb_correct_$stateKey"
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

    // Award Perfect Builder badge when a session ends with every sentence correct on first try
    LaunchedEffect(showSummary) {
        if (showSummary && totalAnswered > 0 && correctCount == totalAnswered) {
            repo.awardBadge(Badge.PERFECT_BUILDER.id)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            questionPool.isEmpty() -> SbEmptyState()

            showSummary -> SentenceBuilderSummary(
                correct = correctCount,
                total   = totalAnswered,
                onRestart = {
                    questionIndex = 0
                    correctCount  = 0
                    totalAnswered = 0
                    showSummary   = false
                    stateKey++
                }
            )

            question != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── Session progress ──────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Question ${questionIndex + 1} / $totalQuestions",
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
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { questionIndex.toFloat() / totalQuestions },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Prompt card ───────────────────────────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "🧩 Build this sentence:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                question.english,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                lineHeight = 28.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "— ${question.scenarioTitle}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.55f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Answer area ───────────────────────────────────────
                    val answerBgColor by animateColorAsState(
                        targetValue = when (checkResult) {
                            true  -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                            false -> Color(0xFFF44336).copy(alpha = 0.12f)
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
                        if (placedIndices.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Tap tiles below to build the sentence",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(
                                    6.dp, Alignment.CenterHorizontally
                                ),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                placedIndices.forEachIndexed { position, tileIdx ->
                                    SbWordTile(
                                        word    = shuffledTiles[tileIdx],
                                        enabled = checkResult == null,
                                        tint    = when (checkResult) {
                                            true  -> TileColor.Correct
                                            false -> TileColor.Wrong
                                            null  -> TileColor.Normal
                                        },
                                        onClick = {
                                            if (checkResult == null) {
                                                placedIndices = placedIndices
                                                    .toMutableList()
                                                    .also { it.removeAt(position) }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // ── Result feedback ───────────────────────────────────
                    when (checkResult) {
                        true -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "🎉 Correct! Well done!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF388E3C)
                            )
                            Text(
                                "+10 XP ✨",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        false -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Not quite! Correct order:",
                                fontSize = 14.sp,
                                color = Color(0xFFC62828),
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
                                        onClick = {}
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        tts.speakAndAwait(
                                            question.words.joinToString("") { it.chinese },
                                            "sb_hear_$stateKey"
                                        )
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Hear sentence")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Hear it")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { stateKey++ },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Try Again")
                                }
                                Button(
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
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Next →")
                                }
                            }
                        }
                        null -> {}
                    }

                    // ── Tile bank (hidden once answered) ──────────────────
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
                            shuffledTiles.forEachIndexed { idx, tile ->
                                val isPlaced = idx in placedIndices
                                SbWordTile(
                                    word    = tile,
                                    enabled = !isPlaced,
                                    tint    = if (isPlaced) TileColor.Faded else TileColor.Normal,
                                    onClick = {
                                        if (!isPlaced) placedIndices = placedIndices + idx
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
    }
}

private enum class TileColor { Normal, Correct, Wrong, Faded }

@Composable
private fun SbWordTile(
    word: PinyinWord,
    enabled: Boolean,
    tint: TileColor,
    onClick: () -> Unit
) {
    val containerColor = when (tint) {
        TileColor.Normal  -> MaterialTheme.colorScheme.secondaryContainer
        TileColor.Correct -> Color(0xFF4CAF50).copy(alpha = 0.18f)
        TileColor.Wrong   -> Color(0xFFF44336).copy(alpha = 0.12f)
        TileColor.Faded   -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }
    val textColor = when (tint) {
        TileColor.Normal  -> MaterialTheme.colorScheme.onSecondaryContainer
        TileColor.Correct -> Color(0xFF2E7D32)
        TileColor.Wrong   -> Color(0xFFC62828)
        TileColor.Faded   -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
    }

    Surface(
        onClick  = onClick,
        enabled  = enabled,
        shape    = RoundedCornerShape(12.dp),
        color    = containerColor,
        shadowElevation = if (enabled && tint != TileColor.Faded) 3.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = word.chinese,
                fontSize   = 24.sp,
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
                    isPerfect        -> "Every sentence perfect — you're a Mandarin star! 🌟"
                    correct >= total / 2 -> "Good work! Keep practising to master all the sentences."
                    else             -> "Keep going — each attempt helps your brain remember!"
                },
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Button(
                onClick  = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("Play Again 🔁", fontSize = 18.sp)
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
