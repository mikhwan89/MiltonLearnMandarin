package com.ikhwan.mandarinkids

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.ikhwan.mandarinkids.data.models.Scenario
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.ui.ConfettiEffect

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

    val stars = remember { ProgressManager.calculateStars(quizScore, totalQuestions) }
    var xpGained by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        xpGained = ProgressRepository.getInstance(context).saveProgress(scenario.id, stars)
    }

    var showConfetti by remember { mutableStateOf(isPerfect) }
    LaunchedEffect(isPerfect) {
        if (isPerfect) {
            kotlinx.coroutines.delay(2600)
            showConfetti = false
        }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
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

        if (showConfetti) {
            ConfettiEffect()
        }
    }
}

fun playWrongSound() {
    Thread {
        try {
            val sampleRate = 44100
            val duration = 0.8
            val numSamples = (sampleRate * duration).toInt()
            val samples = ShortArray(numSamples)

            val startFreq = 320.0
            val endFreq = 80.0

            for (i in samples.indices) {
                val t = i.toDouble() / sampleRate
                val progress = t / duration
                val freq = startFreq + (endFreq - startFreq) * progress
                val phase = (t * freq) % 1.0
                val wave = if (phase < 0.5) 1.0 else -1.0
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

fun playSuccessSound() {
    Thread {
        try {
            val sampleRate = 44100
            val notes = listOf(659.0, 880.0, 1047.0)
            val noteDuration = 0.18
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
