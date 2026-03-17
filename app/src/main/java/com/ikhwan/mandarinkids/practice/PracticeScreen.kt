package com.ikhwan.mandarinkids.practice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ikhwan.mandarinkids.ToneUtils
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.tts.TtsManager
import com.ikhwan.mandarinkids.tts.rememberTtsManager
import com.ikhwan.mandarinkids.ui.ConfettiEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { ProgressRepository.getInstance(context) }
    val vm: PracticeSessionViewModel = viewModel(factory = PracticeSessionViewModel.factory(repo))
    val tts: TtsManager = rememberTtsManager()

    // Auto-play TTS when a new card appears (cardToken always changes, even if index stays 0)
    val currentWord = vm.currentWord
    LaunchedEffect(vm.cardToken) {
        if (currentWord != null) {
            tts.speak(currentWord.chinese)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Practice Mode 复习", fontSize = 16.sp)
                        if (!vm.isLoading && vm.totalStartCount > 0) {
                            Text(
                                if (vm.isDueSession) "Due today · ${vm.rememberedCount} / ${vm.totalStartCount} remembered"
                                else "Full review · ${vm.rememberedCount} / ${vm.totalStartCount} remembered",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
            // ── Loading ──────────────────────────────────────────────────
            vm.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // ── Summary ──────────────────────────────────────────────────
            vm.showSummary -> {
                PracticeSummaryScreen(
                    remembered = vm.rememberedCount,
                    total = vm.totalStartCount,
                    onDone = onBack
                )
            }

            // ── Empty state ──────────────────────────────────────────────
            vm.totalStartCount == 0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("📭", fontSize = 80.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "No words to practise yet!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Complete some scenario flashcards first and tap \"Got it!\" on words you know. They'll show up here for extra practice.",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Go to Scenarios →", fontSize = 16.sp)
                    }
                }
            }

            // ── Practice card ─────────────────────────────────────────────
            else -> {
                val word = vm.currentWord ?: return@Scaffold

                // Build 4 options: correct + 3 distractors, reshuffled per card
                val options = remember(vm.cardToken) {
                    val distractors = vm.allWords
                        .filter { it.english != word.english }
                        .shuffled()
                        .take(3)
                        .map { it.english }
                    (distractors + word.english).shuffled()
                }

                var selectedAnswer by remember(vm.cardToken) { mutableStateOf<String?>(null) }
                val isAnswered = selectedAnswer != null
                val answeredCorrectly = selectedAnswer == word.english

                // Auto-advance 1.5 s after selection
                LaunchedEffect(selectedAnswer) {
                    if (selectedAnswer != null) {
                        kotlinx.coroutines.delay(1500)
                        if (answeredCorrectly) vm.markRemembered() else vm.markForgotten()
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { vm.rememberedCount.toFloat() / vm.totalStartCount },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    Text(
                        text = "${vm.deck.size} card${if (vm.deck.size != 1) "s" else ""} remaining",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // ── Word card ──────────────────────────────────────────
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = word.chinese,
                                    fontSize = if (isAnswered) 48.sp else 72.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                if (isAnswered) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = ToneUtils.coloredAnnotatedPinyin(word.pinyin),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🇮🇩  ${word.indonesian}",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Which translation is correct?",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Speaker icon — top-right corner of the card
                            IconButton(
                                onClick = { tts.speak(word.chinese) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    Icons.Default.VolumeUp,
                                    contentDescription = "Hear pronunciation",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // ── Note bubble (revealed after answering) ─────────────
                    if (isAnswered && word.note != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Text(
                                text = "💡 ${word.note}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── 4 multiple-choice options ──────────────────────────
                    options.forEach { option ->
                        val containerColor = when {
                            !isAnswered -> MaterialTheme.colorScheme.surface
                            option == word.english -> Color(0xFF4CAF50)
                            option == selectedAnswer -> Color(0xFFF44336)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val contentColor = when {
                            !isAnswered -> MaterialTheme.colorScheme.onSurface
                            option == word.english || option == selectedAnswer -> Color.White
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Button(
                            onClick = { if (!isAnswered) selectedAnswer = option },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = containerColor,
                                contentColor = contentColor,
                                disabledContainerColor = containerColor,
                                disabledContentColor = contentColor
                            ),
                            enabled = !isAnswered,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(option, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { vm.finishEarly() }) {
                        Text("Finish session early", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeSummaryScreen(
    remembered: Int,
    total: Int,
    onDone: () -> Unit
) {
    val stillPractising = total - remembered
    val isPerfect = remembered == total
    var showConfetti by remember { mutableStateOf(isPerfect) }
    LaunchedEffect(isPerfect) {
        if (isPerfect) {
            kotlinx.coroutines.delay(2600)
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
        Text(if (remembered == total) "🎉" else "👏", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (remembered == total) "Perfect Session!" else "Practice Complete!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "Reviewed", value = "$total", emoji = "📋")
            StatItem(label = "Remembered", value = "$remembered", emoji = "✅")
            if (stillPractising > 0) {
                StatItem(label = "Keep going", value = "$stillPractising", emoji = "🔁")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (stillPractising > 0) {
            Text(
                text = "You're getting there! Keep practising the $stillPractising remaining words.",
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        } else {
            Text(
                text = "You remembered every word in this session. Amazing!",
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("Done", fontSize = 18.sp)
        }
    } // end Column

    if (showConfetti) {
        ConfettiEffect()
    }
    } // end Box
}

@Composable
private fun StatItem(label: String, value: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 28.sp)
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
