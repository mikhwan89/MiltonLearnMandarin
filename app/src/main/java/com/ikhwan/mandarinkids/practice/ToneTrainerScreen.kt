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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikhwan.mandarinkids.ToneUtils
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.db.Badge
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.playSuccessSound
import com.ikhwan.mandarinkids.playWrongSound
import com.ikhwan.mandarinkids.tts.rememberTtsManager
import com.ikhwan.mandarinkids.ui.ConfettiEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TONE_SESSION_LENGTH = 10

private data class ToneQuestion(
    val chinese: String,
    val pinyinWithTone: String,
    val pinyinBare: String,
    val correctTone: Int,
    val english: String,
    val note: String? = null
)

@Composable
fun ToneTrainerScreen() {
    val context = LocalContext.current
    val tts = rememberTtsManager()
    val scope = rememberCoroutineScope()
    val repo = remember { ProgressRepository.getInstance(context) }

    val questionPool: List<ToneQuestion> = remember {
        JsonScenarioRepository.getAll()
            .flatMap { scenario ->
                scenario.dialogues.flatMap { step ->
                    step.pinyinWords + step.options.flatMap { it.pinyinWords }
                }
            }
            .filter { word -> ToneUtils.splitSyllables(word.pinyin).size == 1 }
            .distinctBy { it.chinese }
            .map { word ->
                ToneQuestion(
                    chinese        = word.chinese,
                    pinyinWithTone = word.pinyin,
                    pinyinBare     = ToneUtils.stripTones(word.pinyin),
                    correctTone    = ToneUtils.detectTone(word.pinyin),
                    english        = word.english,
                    note           = word.note
                )
            }
            .shuffled()
    }

    val totalQuestions = minOf(TONE_SESSION_LENGTH, questionPool.size)

    // Session state
    var questionIndex by remember { mutableStateOf(0) }
    var stateKey      by remember { mutableStateOf(0) }
    var correctCount  by remember { mutableStateOf(0) }
    var totalAnswered by remember { mutableStateOf(0) }
    var showSummary   by remember { mutableStateOf(false) }
    var showConfetti  by remember { mutableStateOf(false) }

    val question = if (questionPool.isNotEmpty()) questionPool[questionIndex] else null

    // Per-attempt state (reset by stateKey)
    var selectedTone by remember(stateKey) { mutableStateOf<Int?>(null) }
    val isAnswered       = selectedTone != null
    val answeredCorrectly = isAnswered && selectedTone == question?.correctTone

    // Auto-play TTS when a new question loads
    LaunchedEffect(stateKey) {
        if (question != null) {
            delay(400)
            tts.speak(question.chinese)
        }
    }

    // Auto-advance after a correct answer
    LaunchedEffect(selectedTone, stateKey) {
        val sel = selectedTone ?: return@LaunchedEffect
        if (question == null) return@LaunchedEffect
        if (sel == question.correctTone) {
            correctCount++
            totalAnswered++
            repo.addToneTrainerXp(2)
            showConfetti = true
            delay(1600)
            showConfetti = false
            val next = questionIndex + 1
            if (next >= totalQuestions) showSummary = true
            else { questionIndex = next; stateKey++ }
        }
    }

    // Perfect Pitch badge when session ends
    LaunchedEffect(showSummary) {
        if (showSummary && totalAnswered > 0 && correctCount == totalAnswered) {
            repo.awardBadge(Badge.PERFECT_PITCH.id)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            questionPool.isEmpty() -> TtEmptyState()

            showSummary -> ToneTrainerSummary(
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
                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Character card ────────────────────────────────────
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape  = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(
                            modifier         = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                // Bare pinyin — the puzzle for the child
                                Text(
                                    text       = question.pinyinBare,
                                    fontSize   = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onPrimaryContainer,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Chinese character
                                Text(
                                    text       = question.chinese,
                                    fontSize   = 56.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                // English — revealed after answering
                                if (isAnswered) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text     = "🇬🇧 ${question.english}",
                                        fontSize = 14.sp,
                                        color    = MaterialTheme.colorScheme.onPrimaryContainer
                                            .copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            // Replay button
                            IconButton(
                                onClick  = { tts.speak(question.chinese) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Hear pronunciation",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Instruction / XP feedback
                    when {
                        !isAnswered -> Text(
                            "What tone do you hear?",
                            fontSize = 14.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        answeredCorrectly -> Text(
                            "+2 XP ✨",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color(0xFF4CAF50)
                        )
                        else -> Text(
                            "Not quite — correct tone highlighted in green",
                            fontSize = 13.sp,
                            color    = Color(0xFFC62828)
                        )
                    }

                    // Note bubble — shown after answering if available
                    if (isAnswered && question.note != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color  = MaterialTheme.colorScheme.tertiaryContainer,
                            shape  = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "💡 ${question.note}",
                                fontSize  = 13.sp,
                                color     = MaterialTheme.colorScheme.onTertiaryContainer,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp,
                                modifier  = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Tone choice buttons ───────────────────────────────
                    listOf(1, 2, 3, 4, 0).forEach { tone ->
                        val isSelected = selectedTone == tone
                        val isCorrectTone = isAnswered && tone == question.correctTone
                        val isWrongTone   = isAnswered && isSelected && !answeredCorrectly
                        ToneChoiceButton(
                            tone          = tone,
                            isCorrect     = isCorrectTone,
                            isWrong       = isWrongTone,
                            isIdle        = isAnswered && !isSelected && !isCorrectTone,
                            enabled       = !isAnswered,
                            onClick       = {
                                if (!isAnswered) {
                                    selectedTone = tone
                                    if (tone == question.correctTone) playSuccessSound()
                                    else playWrongSound()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Next button for wrong answers
                    if (isAnswered && !answeredCorrectly) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                totalAnswered++
                                val next = questionIndex + 1
                                if (next >= totalQuestions) showSummary = true
                                else { questionIndex = next; stateKey++ }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("Next →", fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        if (showConfetti) ConfettiEffect()
    }
}

// ── Tone choice button ────────────────────────────────────────────────────────

private val TONE_META = mapOf(
    1 to Triple("ā", "Tone 1 →",   "flat"),
    2 to Triple("á", "Tone 2 ↗",   "rising"),
    3 to Triple("ǎ", "Tone 3 ↘↗",  "dip"),
    4 to Triple("à", "Tone 4 ↘",   "falling"),
    0 to Triple("•", "Neutral",    "light")
)

@Composable
private fun ToneChoiceButton(
    tone: Int,
    isCorrect: Boolean,
    isWrong: Boolean,
    isIdle: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val baseColor = ToneUtils.toneColor(tone)
    val (symbol, label, sublabel) = TONE_META[tone]!!

    val containerColor by animateColorAsState(
        targetValue = when {
            isCorrect -> Color(0xFF4CAF50)
            isWrong   -> Color(0xFFF44336)
            isIdle    -> MaterialTheme.colorScheme.surfaceVariant
            else      -> baseColor.copy(alpha = 0.14f)
        },
        animationSpec = tween(250),
        label = "toneBtn_$tone"
    )
    val contentColor = when {
        isCorrect || isWrong -> Color.White
        isIdle               -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        else                 -> baseColor
    }

    Surface(
        onClick   = onClick,
        enabled   = enabled,
        shape     = RoundedCornerShape(16.dp),
        color     = containerColor,
        shadowElevation = if (!isIdle && !isCorrect && !isWrong) 2.dp else 0.dp,
        modifier  = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text       = symbol,
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                color      = contentColor
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = label,
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = contentColor
                )
                Text(
                    text     = sublabel,
                    fontSize = 12.sp,
                    color    = contentColor.copy(alpha = 0.75f)
                )
            }
            // Checkmark / cross indicator after answering
            Text(
                text     = when {
                    isCorrect -> "✓"
                    isWrong   -> "✗"
                    else      -> ""
                },
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun TtEmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("🎵", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "No words to train yet",
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Complete some scenario lessons first to unlock Tone Trainer.",
                fontSize  = 16.sp,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
        }
    }
}

// ── Summary screen ────────────────────────────────────────────────────────────

@Composable
private fun ToneTrainerSummary(
    correct: Int,
    total: Int,
    onRestart: () -> Unit
) {
    val isPerfect = total > 0 && correct == total
    var showConfetti by remember { mutableStateOf(isPerfect) }
    LaunchedEffect(Unit) {
        if (isPerfect) { delay(2800); showConfetti = false }
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
                if (isPerfect) "Perfect Pitch!" else "Great Listening!",
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TtStatItem("Questions", "$total", "🎵")
                TtStatItem("Correct", "$correct", "✅")
                TtStatItem(
                    "Score",
                    "${if (total > 0) correct * 100 / total else 0}%",
                    "🏆"
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                when {
                    isPerfect        -> "Every tone perfect — you have perfect pitch! 🌟"
                    correct >= total * 2 / 3 -> "Good ear! Keep listening to sharpen your tones."
                    else             -> "Tones take practice — keep going, you'll get it!"
                },
                fontSize  = 15.sp,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp,
                modifier  = Modifier.padding(bottom = 32.dp)
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
private fun TtStatItem(label: String, value: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 28.sp)
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
