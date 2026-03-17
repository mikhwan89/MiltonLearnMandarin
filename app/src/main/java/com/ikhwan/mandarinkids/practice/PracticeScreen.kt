package com.ikhwan.mandarinkids.practice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.ikhwan.mandarinkids.playSuccessSound
import com.ikhwan.mandarinkids.playWrongSound
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

/** Emoji + container color for a mastery level 1-10. */
private fun masteryColor(level: Int): Color = when {
    level <= 3 -> Color(0xFFEF5350)   // red — still learning
    level <= 6 -> Color(0xFFFFA726)   // orange — getting there
    else       -> Color(0xFF66BB6A)   // green — near mastered
}

private fun masteryEmoji(level: Int): String = when (level) {
    1  -> "\u2B52"  // ⭒ small star outline
    2  -> "\u2B51"  // ⭑ black small star
    3  -> "\u2729"  // ✩ stress outlined white star
    4  -> "\u272C"  // ✬ open centre black star
    5  -> "\u272E"  // ✮ circled white star
    6  -> "\u272D"  // ✭ outlined black star
    7  -> "\u2605"  // ★ black star
    8  -> "\u272F"  // ✯ pinwheel star
    9  -> "\u2B50"  // ⭐ medium white star
    else -> "🌟"   // 10 — glowing star
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { ProgressRepository.getInstance(context) }
    val vm: PracticeSessionViewModel = viewModel(factory = PracticeSessionViewModel.factory(repo))
    val tts: TtsManager = rememberTtsManager()

    // Auto-play TTS on every new card
    val currentWord = vm.currentWord
    LaunchedEffect(vm.cardToken) {
        if (currentWord != null) tts.speak(currentWord.chinese)
    }

    // Word count per mastery level (for chip labels)
    val levelCounts = remember(vm.allWords) {
        vm.allWords.groupBy { it.boxLevel }.mapValues { it.value.size }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Flashcard Practice 复习", fontSize = 16.sp)
                        if (!vm.isLoading && vm.totalStartCount > 0) {
                            Text(
                                "Session: ${vm.correctCount} / ${vm.totalStartCount} correct",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    ) { padding ->

        when {
            // ── Loading ───────────────────────────────────────────────────
            vm.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            // ── Summary ───────────────────────────────────────────────────
            vm.showSummary -> {
                PracticeSummaryScreen(
                    correct = vm.correctCount,
                    total = vm.totalStartCount,
                    onDone = onBack
                )
            }

            // ── No mastered words at all ───────────────────────────────────
            vm.allWords.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("📭", fontSize = 80.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("No words to practise yet!", fontSize = 24.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Complete some scenario flashcards first. Words you master will appear here.",
                        fontSize = 16.sp, textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                        Text("Go to Scenarios →", fontSize = 16.sp)
                    }
                }
            }

            // ── Practice card ─────────────────────────────────────────────
            else -> {
                val word = vm.currentWord ?: return@Scaffold

                // 4 options: correct + 3 distractors
                val options = remember(vm.cardToken) {
                    val distractors = vm.allWords
                        .filter { it.english != word.english }
                        .shuffled().take(3).map { it.english }
                    (distractors + word.english).shuffled()
                }

                var selectedAnswer by remember(vm.cardToken) { mutableStateOf<String?>(null) }
                val isAnswered = selectedAnswer != null
                val answeredCorrectly = selectedAnswer == word.english

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
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // ── Mastery level filter chips ─────────────────────────
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(vm.availableLevels) { level ->
                            val count = levelCounts[level] ?: 0
                            val selected = level in vm.selectedLevels
                            val chipColor = masteryColor(level)
                            FilterChip(
                                selected = selected,
                                onClick = { vm.toggleLevel(level) },
                                label = {
                                    Text(
                                        "${masteryEmoji(level)} $level · $count",
                                        fontSize = 13.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = chipColor.copy(alpha = 0.25f),
                                    selectedLabelColor = chipColor,
                                    selectedLeadingIconColor = chipColor
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    selectedBorderColor = chipColor,
                                    selectedBorderWidth = 1.5.dp
                                )
                            )
                        }
                    }

                    // ── Session progress bar ───────────────────────────────
                    LinearProgressIndicator(
                        progress = { vm.correctCount.toFloat() / vm.totalStartCount },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    Text(
                        "${vm.deck.size} card${if (vm.deck.size != 1) "s" else ""} remaining",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 10.dp, top = 2.dp)
                    )

                    // ── Word card ──────────────────────────────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth().height(190.dp),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = word.chinese,
                                    fontSize = if (isAnswered) 44.sp else 68.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                if (isAnswered) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        ToneUtils.coloredAnnotatedPinyin(word.pinyin),
                                        fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "🇮🇩  ${word.indonesian}", fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    // Mastery rating badge
                                    Surface(
                                        color = masteryColor(word.boxLevel).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "${masteryEmoji(word.boxLevel)} Mastery ${word.boxLevel}/10",
                                            fontSize = 12.sp,
                                            color = masteryColor(word.boxLevel),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            // Speaker icon — top-right corner
                            IconButton(
                                onClick = { tts.speak(word.chinese) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.VolumeUp, "Hear pronunciation",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }

                    // ── Note bubble ────────────────────────────────────────
                    if (isAnswered && word.note != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text(
                                "💡 ${word.note}", fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                textAlign = TextAlign.Center, lineHeight = 19.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ── 2×2 answer grid ────────────────────────────────────
                    options.chunked(2).forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowOptions.forEach { option ->
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
                                    onClick = {
                                        if (!isAnswered) {
                                            selectedAnswer = option
                                            if (option == word.english) playSuccessSound()
                                            else playWrongSound()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(64.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = containerColor, contentColor = contentColor,
                                        disabledContainerColor = containerColor, disabledContentColor = contentColor
                                    ),
                                    enabled = !isAnswered,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(option, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center, lineHeight = 18.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(onClick = { vm.finishEarly() }) {
                        Text("Finish session early", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeSummaryScreen(correct: Int, total: Int, onDone: () -> Unit) {
    val isPerfect = correct == total
    var showConfetti by remember { mutableStateOf(isPerfect) }
    LaunchedEffect(isPerfect) {
        if (isPerfect) {
            kotlinx.coroutines.delay(2600)
            showConfetti = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(if (isPerfect) "🎉" else "👏", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                if (isPerfect) "Perfect Session!" else "Session Complete!",
                fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("Reviewed", "$total", "📋")
                StatItem("Correct", "$correct", "✅")
                if (correct < total) StatItem("Missed", "${total - correct}", "🔁")
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                if (isPerfect) "You got every word right! Amazing work! 🌟"
                else "Good practice! Keep going to raise your mastery ratings.",
                fontSize = 15.sp, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(60.dp)) {
                Text("Done", fontSize = 18.sp)
            }
        }
        if (showConfetti) ConfettiEffect()
    }
}

@Composable
private fun StatItem(label: String, value: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 28.sp)
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
