package com.ikhwan.mandarinkids

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ikhwan.mandarinkids.ui.theme.appColors
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikhwan.mandarinkids.data.models.Scenario
import com.ikhwan.mandarinkids.db.MasteredWordEntity
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.ui.ConfettiEffect

@Composable
fun QuizResultsScreen(
    scenario: Scenario,
    rolePlayScore: Int,
    quizScore: Int,
    totalQuestions: Int,
    level: Int = 1,
    onComplete: () -> Unit,
    onTryAgain: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val quizPercentage = (quizScore.toFloat() / totalQuestions * 100).toInt()
    val isPerfect = quizScore == totalQuestions
    val leveledUp = isPerfect && level < 6

    val stars = remember { ProgressManager.calculateStars(quizScore, totalQuestions) }
    var xpGained by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        val repo = ProgressRepository.getInstance(context)
        xpGained = repo.saveProgress(scenario.id, stars, level)
        // Seed every word from this scenario into the flashcard DB regardless of score.
        // Uses INSERT OR IGNORE so existing mastery progress is never overwritten.
        val seedWords = scenario.getFlashcardWords().map { pw ->
            MasteredWordEntity(
                scenarioId = scenario.id,
                chinese = pw.chinese,
                pinyin = pw.pinyin,
                english = pw.english,
                indonesian = pw.indonesian,
                note = pw.note,
                boxLevel = 1,
                nextReviewDate = 0L
            )
        }
        repo.seedWordsForScenario(scenario.id, seedWords)
    }

    var showConfetti by remember { mutableStateOf(isPerfect) }
    LaunchedEffect(isPerfect) {
        if (isPerfect) {
            kotlinx.coroutines.delay(2600)
            showConfetti = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "celebrate")
    // Perfect: big excited bounce; non-perfect: gentle idle bob so screen never feels static
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPerfect) -20f else -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPerfect) 400 else 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceY"
    )

    val colors = MaterialTheme.appColors
    val labelColor = colors.onLightTile
    val resultsGradient = colors.tileBlue.asList()

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
                    .graphicsLayer { translationY = bounceY }
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
                        color = if (i < stars) colors.starFilled else colors.starEmpty
                    )
                }
            }

            if (leveledUp) {
                Text(
                    text = "🔓 Level ${level + 1} Unlocked!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (xpGained > 0) {
                Text(
                    text = "+$xpGained XP",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.xpGainText,
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
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(resultsGradient))) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Your Results",
                            fontSize = 18.sp,
                            color = labelColor.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "You got $quizScore / $totalQuestions correct",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                onClick = onComplete,
                shape = RoundedCornerShape(50),
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(Brush.verticalGradient(colors.actionPositive.asList())),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Back to Home 回家", fontSize = 18.sp,
                        color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (onTryAgain != null) {
                Surface(
                    onClick = onTryAgain,
                    shape = RoundedCornerShape(50),
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(Brush.verticalGradient(colors.tileAmber.asList())),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Try Again 再试一次", fontSize = 18.sp,
                            color = colors.onLightTile,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (showConfetti) {
            ConfettiEffect()
        }
    }
}

/**
 * Wrong-answer sound: two dissonant tritone frequencies (Bb4 + E4) with a
 * descending sawtooth-rich timbre — clearly "wrong" without being too harsh.
 */
fun playWrongSound() {
    Thread {
        try {
            val sampleRate = 44100
            val duration = 0.65
            val numSamples = (sampleRate * duration).toInt()
            val samples = ShortArray(numSamples)

            val freq1 = 466.16  // Bb4
            val freq2 = 311.13  // Eb4  — tritone apart (maximally dissonant)

            for (i in samples.indices) {
                val t = i.toDouble() / sampleRate
                val progress = t / duration
                // Pitch slides down 35% over the duration
                val pitchMod = 1.0 - progress * 0.35
                // Envelope: instant attack, sustain, then fade last 30%
                val env = if (progress > 0.70) (1.0 - progress) / 0.30 else 1.0
                // Sawtooth approximation (1st + harmonics) on both freqs for buzz
                fun saw(f: Double) =
                    Math.sin(2.0 * Math.PI * f * pitchMod * t) * 0.55 +
                    Math.sin(2.0 * Math.PI * f * pitchMod * t * 2) * 0.25 +
                    Math.sin(2.0 * Math.PI * f * pitchMod * t * 3) * 0.12 +
                    Math.sin(2.0 * Math.PI * f * pitchMod * t * 4) * 0.08
                samples[i] = (env * (saw(freq1) * 0.5 + saw(freq2) * 0.5) *
                               Short.MAX_VALUE * 0.72).toInt().toShort()
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
            Thread.sleep((duration * 1000).toLong() + 150)
            audioTrack.release()
        } catch (_: Exception) {}
    }.start()
}

/**
 * Correct-answer sound: 5-note ascending major arpeggio (C5→E5→G5→C6→E6)
 * with warm harmonics and a punchy attack — designed to feel rewarding.
 */
fun playSuccessSound() {
    Thread {
        try {
            val sampleRate = 44100
            // C5, E5, G5, C6, E6 — bright ascending major arpeggio
            val notes   = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51)
            val durations = listOf(0.10,   0.10,   0.10,   0.10,   0.22)   // last note held
            val totalSamples = durations.sumOf { (sampleRate * it).toInt() }
            val samples = ShortArray(totalSamples)

            var offset = 0
            for ((idx, freq) in notes.withIndex()) {
                val dur = durations[idx]
                val count = (sampleRate * dur).toInt()
                for (i in 0 until count) {
                    val t = i.toDouble() / sampleRate
                    val progress = t / dur
                    // Punchy: tiny linear attack, then exponential decay
                    val env = (if (progress < 0.015) progress / 0.015 else 1.0) *
                               Math.exp(-5.0 * progress)
                    // Warm tone: fundamental + 2nd + 3rd harmonic
                    val wave = Math.sin(2.0 * Math.PI * freq * t)        * 0.65 +
                               Math.sin(2.0 * Math.PI * freq * 2.0 * t) * 0.22 +
                               Math.sin(2.0 * Math.PI * freq * 3.0 * t) * 0.10 +
                               Math.sin(2.0 * Math.PI * freq * 4.0 * t) * 0.03
                    samples[offset + i] = (env * wave * Short.MAX_VALUE * 0.85).toInt().toShort()
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
            Thread.sleep((durations.sum() * 1000).toLong() + 150)
            audioTrack.release()
        } catch (_: Exception) {}
    }.start()
}
