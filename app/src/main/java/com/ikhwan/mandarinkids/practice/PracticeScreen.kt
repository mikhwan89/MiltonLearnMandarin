package com.ikhwan.mandarinkids.practice

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

    // Auto-play TTS when card is flipped to back
    val currentWord = vm.currentWord
    LaunchedEffect(vm.isFlipped, vm.currentIndex) {
        if (vm.isFlipped && currentWord != null) {
            tts.speak(currentWord.chinese)
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (vm.isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlip"
    )

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
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    Text(
                        text = if (!vm.isFlipped) "Tap card to hear & see translation" else "Tap card to flip back",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // ── Flip card ─────────────────────────────────────────
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .graphicsLayer { rotationY = rotation }
                            .clickable { vm.flip() },
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (rotation <= 90f)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (rotation <= 90f) {
                                // Front — Chinese character only
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Text(
                                        text = word.chinese,
                                        fontSize = 72.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tap to reveal",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                // Back — pinyin + translations (counter-rotate to un-mirror)
                                Column(
                                    modifier = Modifier
                                        .graphicsLayer { rotationY = 180f }
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = word.chinese,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    Text(
                                        text = ToneUtils.coloredAnnotatedPinyin(word.pinyin),
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )
                                    Text(
                                        text = "🇬🇧  ${word.english}",
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🇮🇩  ${word.indonesian}",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // ── Note bubble (below card, fades in on flip) ────────
                    if (vm.isFlipped && word.note != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Text(
                                text = "💡 ${word.note}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Action buttons (only after flip) ──────────────────
                    if (vm.isFlipped) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { vm.markForgotten() },
                                modifier = Modifier.weight(1f).height(60.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFF44336)
                                )
                            ) {
                                Text("✗  Forgot it", fontSize = 15.sp)
                            }

                            Button(
                                onClick = { vm.markRemembered() },
                                modifier = Modifier.weight(1f).height(60.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Text("✓  Remember it!", fontSize = 15.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
