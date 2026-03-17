package com.ikhwan.mandarinkids

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.animation.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.ikhwan.mandarinkids.data.models.*
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.tts.TtsManager
import com.ikhwan.mandarinkids.tts.rememberTtsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    scenario: Scenario,
    rolePlayScore: Int,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    onTryAgain: (() -> Unit)? = null  // NEW: Callback for try again
) {
    val context = LocalContext.current
    val tts = rememberTtsManager()
    val coroutineScope = rememberCoroutineScope()

    val vm: QuizViewModel = viewModel(
        key = "${scenario.id}_$rolePlayScore",
        factory = QuizViewModel.factory(scenario, rolePlayScore)
    )

    val currentQuestion = vm.currentQuestion

    // Auto-play TTS when an audio-mode question loads
    LaunchedEffect(vm.currentQuestionIndex) {
        val q = vm.currentQuestion
        if (q != null && q.direction == QuizDirection.AUDIO_TO_TRANSLATION) {
            delay(300)
            tts.speak(q.questionChinese)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Quiz Time! 测验", fontSize = 16.sp)
                        Text(
                            "Question ${vm.currentQuestionIndex + 1}/${vm.scenario.quizQuestions.size}",
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
        if (vm.showResults) {
            QuizResultsScreen(
                scenario = vm.scenario,
                rolePlayScore = vm.rolePlayScore,
                quizScore = vm.correctAnswersCount,
                totalQuestions = vm.scenario.quizQuestions.size,
                onComplete = onComplete,
                onTryAgain = onTryAgain  // Pass through the callback
            )
        } else if (currentQuestion != null) {
            val question = currentQuestion
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LinearProgressIndicator(
                    progress = { (vm.currentQuestionIndex + 1).toFloat() / vm.scenario.quizQuestions.size },
                    modifier = Modifier.fillMaxWidth(),
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Question ${vm.currentQuestionIndex + 1}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Display question based on direction
                                when (question.direction) {
                                    QuizDirection.CHINESE_TO_TRANSLATION -> {
                                        // Show Chinese + Pinyin
                                        Text(
                                            text = question.questionChinese,
                                            fontSize = 28.sp,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = question.questionPinyin,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = question.questionText,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    QuizDirection.TRANSLATION_TO_CHINESE -> {
                                        // Show English question only
                                        Text(
                                            text = question.questionText,
                                            fontSize = 20.sp,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                    QuizDirection.AUDIO_TO_TRANSLATION -> {
                                        // Show speaker UI — no text hint
                                        Text(
                                            text = "🎧 Listen and choose!",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                        Button(
                                            onClick = { tts.speak(question.questionChinese) },
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Play again", fontSize = 15.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Select your answer:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Answer options
                    items(question.options.size) { index ->
                        QuizOptionButton(
                            option = question.options[index],
                            index = index,
                            isSelected = vm.selectedAnswerIndex == index,
                            isCorrect = index == question.correctAnswerIndex,
                            showFeedback = vm.showFeedback,
                            onClick = {
                                if (!vm.showFeedback) {
                                    vm.selectAnswer(index)
                                    if (index == question.correctAnswerIndex) playSuccessSound()
                                    else playWrongSound()
                                    coroutineScope.launch {
                                        delay(3000)
                                        vm.advanceQuestion()
                                    }
                                }
                            },
                            tts = tts,
                            direction = question.direction  // NEW: Pass direction
                        )
                    }

                    if (vm.showFeedback) {
                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically()
                            ) {
                                FeedbackCard(
                                    isCorrect = vm.selectedAnswerIndex == question.correctAnswerIndex,
                                    explanation = question.explanation
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // No quiz questions available for this scenario
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("🎉", fontSize = 64.sp)
                    Text(
                        "No quiz for this scenario yet!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onComplete) {
                        Text("Back to Home")
                    }
                }
            }
        }
    }
}

@Composable
fun QuizOptionButton(
    option: QuizOption,
    index: Int,
    isSelected: Boolean,
    isCorrect: Boolean,
    showFeedback: Boolean,
    onClick: () -> Unit,
    tts: TtsManager,
    direction: QuizDirection  // NEW: Pass direction
) {
    val backgroundColor = when {
        !showFeedback -> MaterialTheme.colorScheme.surface
        isSelected && isCorrect -> Color(0xFF4CAF50)
        isSelected && !isCorrect -> Color(0xFFF44336)
        !isSelected && isCorrect -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        showFeedback && (isCorrect || isSelected) -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        enabled = !showFeedback
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (showFeedback && (isCorrect || isSelected))
                    Color.White.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = (index + 1).toString(),
                        fontSize = 18.sp,
                        color = if (showFeedback && (isCorrect || isSelected))
                            Color.White
                        else
                            MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                when (direction) {
                    QuizDirection.TRANSLATION_TO_CHINESE -> {
                        // Show Chinese + Pinyin
                        Text(
                            text = option.chinese,
                            fontSize = 20.sp,
                            style = MaterialTheme.typography.titleLarge,
                            color = contentColor
                        )
                        Text(
                            text = option.pinyin,
                            fontSize = 14.sp,
                            color = if (showFeedback && (isCorrect || isSelected))
                                Color.White.copy(alpha = 0.8f)
                            else
                                MaterialTheme.colorScheme.secondary
                        )
                    }
                    QuizDirection.CHINESE_TO_TRANSLATION,
                    QuizDirection.AUDIO_TO_TRANSLATION -> {
                        // Show English/Indonesian translation only
                        Text(
                            text = option.translation,
                            fontSize = 18.sp,
                            style = MaterialTheme.typography.titleLarge,
                            color = contentColor
                        )
                    }
                }
            }

            // Play audio button (only for TRANSLATION_TO_CHINESE)
            if (direction == QuizDirection.TRANSLATION_TO_CHINESE) {
                IconButton(
                    onClick = {
                        tts.speak(option.chinese)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (showFeedback) {
                if (isCorrect) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Correct",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                } else if (isSelected) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Wrong",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FeedbackCard(
    isCorrect: Boolean,
    explanation: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else
                Color(0xFFF44336).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isCorrect) "✅" else "❌",
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column {
                Text(
                    text = if (isCorrect) "Correct! 对了！" else "Not quite! 再试试！",
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = explanation,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuizResultsScreen(
    scenario: Scenario,
    rolePlayScore: Int,
    quizScore: Int,
    totalQuestions: Int,
    onComplete: () -> Unit,
    onTryAgain: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val quizPercentage = (quizScore.toFloat() / totalQuestions * 100).toInt()
    val totalScore = rolePlayScore + quizScore
    val isPerfect = quizScore == totalQuestions

    // Calculate stars and save progress once on composition
    val stars = remember { ProgressManager.calculateStars(quizScore, totalQuestions) }
    var xpGained by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        xpGained = ProgressRepository.getInstance(context).saveProgress(scenario.id, stars)
    }

    // Bouncing emoji animation for perfect score
    val infiniteTransition = rememberInfiniteTransition(label = "celebrate")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceY"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when {
                isPerfect -> "🎉🎊🏆"
                quizPercentage >= 70 -> "😊👍✨"
                else -> "💪📚🌟"
            },
            fontSize = 64.sp,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .then(
                    if (isPerfect) Modifier.graphicsLayer { translationY = bounceY }
                    else Modifier
                )
        )

        Text(
            text = when {
                isPerfect -> "Perfect Score!"
                quizPercentage >= 70 -> "Great Job!"
                else -> "Keep Practicing!"
            },
            fontSize = 32.sp,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Text(
            text = when {
                isPerfect -> "完美！Wánměi!"
                quizPercentage >= 70 -> "很好！Hěn hǎo!"
                else -> "加油！Jiāyóu!"
            },
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Star rating
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            repeat(3) { i ->
                Text(
                    text = if (i < stars) "★" else "☆",
                    fontSize = 48.sp,
                    color = if (i < stars) Color(0xFFFFC107) else Color(0xFFBDBDBD)
                )
            }
        }

        // XP gained
        if (xpGained > 0) {
            Text(
                text = "+$xpGained XP",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 24.dp)
            )
        } else {
            Text(
                text = "No new XP — try for a higher star rating!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Results",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("💬 Conversation:", fontSize = 16.sp)
                    Text(
                        "$rolePlayScore steps",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("📝 Quiz:", fontSize = 16.sp)
                    Text(
                        "$quizScore / $totalQuestions",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total Score:",
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "$totalScore points",
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💡 Learning Tip:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        isPerfect -> "You're a natural! Keep practicing to remember these phrases. Try using them with your friends!"
                        quizPercentage >= 70 -> "Good progress! Review the phrases you missed and try this scenario again tomorrow."
                        else -> "Learning takes time! Try this scenario again and listen carefully to how the characters speak."
                    },
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Back to Home 回家", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (onTryAgain != null) {
            OutlinedButton(
                onClick = onTryAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Try Again 再试一次", fontSize = 18.sp)
            }
        }
    }
}

private fun playWrongSound() {
    Thread {
        try {
            val sampleRate = 44100
            val duration = 0.8
            val numSamples = (sampleRate * duration).toInt()
            val samples = ShortArray(numSamples)

            // Descending pitch sweep from 320Hz down to 80Hz — classic TV buzzer
            val startFreq = 320.0
            val endFreq = 80.0

            for (i in samples.indices) {
                val t = i.toDouble() / sampleRate
                val progress = t / duration
                val freq = startFreq + (endFreq - startFreq) * progress
                // Square wave for harsh buzzer tone
                val phase = (t * freq) % 1.0
                val wave = if (phase < 0.5) 1.0 else -1.0
                // Slight fade out at the end
                val envelope = if (progress > 0.8) (1.0 - progress) / 0.2 else 1.0
                samples[i] = (wave * envelope * Short.MAX_VALUE * 0.7).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, numSamples)
            audioTrack.play()
            Thread.sleep((duration * 1000).toLong() + 200)
            audioTrack.release()
        } catch (_: Exception) {}
    }.start()
}

private fun playSuccessSound() {
    Thread {
        try {
            val sampleRate = 44100
            // Play two ascending notes: E5 (659Hz) then A5 (880Hz) — happy major arpeggio
            val notes = listOf(659.0, 880.0, 1047.0)
            val noteDuration = 0.18 // seconds each
            val totalSamples = (sampleRate * noteDuration * notes.size).toInt()
            val samples = ShortArray(totalSamples)

            var offset = 0
            for (freq in notes) {
                val count = (sampleRate * noteDuration).toInt()
                for (i in 0 until count) {
                    val t = i.toDouble() / sampleRate
                    val envelope = Math.exp(-6.0 * t)
                    val wave = Math.sin(2.0 * Math.PI * freq * t)
                    samples[offset + i] = (envelope * wave * Short.MAX_VALUE * 0.8).toInt().toShort()
                }
                offset += count
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(totalSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, totalSamples)
            audioTrack.play()
            Thread.sleep((noteDuration * notes.size * 1000).toLong() + 200)
            audioTrack.release()
        } catch (_: Exception) {}
    }.start()
}