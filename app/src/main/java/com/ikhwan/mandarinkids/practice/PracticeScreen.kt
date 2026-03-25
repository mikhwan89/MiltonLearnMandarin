package com.ikhwan.mandarinkids.practice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.db.PracticeType
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.playSuccessSound
import com.ikhwan.mandarinkids.playWrongSound
import com.ikhwan.mandarinkids.tts.TtsManager
import com.ikhwan.mandarinkids.tts.rememberTtsManager
import com.ikhwan.mandarinkids.ui.ConfettiEffect

private fun masteryColor(level: Int): Color = when {
    level <= 3 -> Color(0xFFEF5350)
    level <= 6 -> Color(0xFFFFA726)
    else       -> Color(0xFF66BB6A)
}

private fun masteryEmoji(level: Int): String = when (level) {
    1  -> "\u2B52"
    2  -> "\u2B51"
    3  -> "\u2729"
    4  -> "\u272C"
    5  -> "\u272E"
    6  -> "\u272D"
    7  -> "\u2605"
    8  -> "\u272F"
    9  -> "\u2B50"
    else -> "🌟"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(onBack: () -> Unit) {
    var selectedType by remember { mutableStateOf(PracticeType.DEFAULT) }
    val context = LocalContext.current
    val repo = remember { ProgressRepository.getInstance(context) }
    val tts: TtsManager = rememberTtsManager()

    // One ViewModel per practice type — each tracks its own progress independently
    val vmDefault: PracticeSessionViewModel = viewModel(
        key = "practice_DEFAULT",
        factory = PracticeSessionViewModel.factory(repo, JsonScenarioRepository, PracticeType.DEFAULT)
    )
    val vmListening: PracticeSessionViewModel = viewModel(
        key = "practice_LISTENING",
        factory = PracticeSessionViewModel.factory(repo, JsonScenarioRepository, PracticeType.LISTENING)
    )
    val vmReading: PracticeSessionViewModel = viewModel(
        key = "practice_READING",
        factory = PracticeSessionViewModel.factory(repo, JsonScenarioRepository, PracticeType.READING)
    )
    val vm = when (selectedType) {
        PracticeType.DEFAULT   -> vmDefault
        PracticeType.LISTENING -> vmListening
        PracticeType.READING   -> vmReading
    }

    // Auto-play TTS: DEFAULT and LISTENING auto-play; READING does not (test visual recognition)
    val currentWord = vm.currentWord
    LaunchedEffect(vm.cardToken, selectedType) {
        if (currentWord != null && selectedType != PracticeType.READING) {
            tts.speak(currentWord.chinese)
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Practice type tab row ──────────────────────────────────────
            TabRow(
                selectedTabIndex = PracticeType.values().indexOf(selectedType),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                PracticeType.values().forEach { type ->
                    Tab(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        text = {
                            Text(
                                "${type.emoji} ${type.displayName}",
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            // ── Practice content for the selected type ─────────────────────
            when {
                vm.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                vm.showSummary -> {
                    PracticeSummaryScreen(
                        correct = vm.correctCount,
                        total = vm.totalAnswered,
                        promoted = vm.promotedCount,
                        demoted = vm.demotedCount,
                        onDone = { vm.finishEarly().let { } /* reset via onBack */ ; onBack() }
                    )
                }

                vm.allWords.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("📭", fontSize = 80.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "No words to practise yet!", fontSize = 24.sp,
                            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Complete some scenario flashcards first.",
                            fontSize = 16.sp, textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(40.dp))
                        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                            Text("Go to Scenarios →", fontSize = 16.sp)
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // ── Session stats ──────────────────────────────────
                        if (vm.totalAnswered > 0) {
                            Text(
                                "${vm.correctCount} / ${vm.totalAnswered} correct this session",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                        }

                        // ── Practice mode selector (All / Weak / Maintain) ─
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 4.dp)
                        ) {
                            SegmentedButton(
                                selected = vm.practiceMode == PracticeMode.ALL,
                                onClick = { vm.setMode(PracticeMode.ALL) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                            ) { Text("All Words", fontSize = 11.sp, textAlign = TextAlign.Center) }

                            SegmentedButton(
                                selected = vm.practiceMode == PracticeMode.WEAK,
                                onClick = { vm.setMode(PracticeMode.WEAK) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                            ) { Text("Weak Words", fontSize = 11.sp, textAlign = TextAlign.Center) }

                            SegmentedButton(
                                selected = vm.practiceMode == PracticeMode.MASTERY,
                                onClick = { vm.setMode(PracticeMode.MASTERY) },
                                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                            ) { Text("Maintain", fontSize = 11.sp, textAlign = TextAlign.Center) }
                        }

                        // ── Star rating distribution (read-only) ───────────
                        val weakLevels = vm.weakLevels
                        val masteryLevels = vm.masteryLevels
                        val levelCounts = remember(vm.allWords) {
                            vm.allWords.groupBy { it.boxLevel }.entries.sortedBy { it.key }
                        }
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(levelCounts) { (level, words) ->
                                val color = masteryColor(level)
                                Surface(
                                    color = color.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        "${masteryEmoji(level)} $level ✦ ${words.size}",
                                        fontSize = 12.sp, color = color,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // ── Empty pool for current mode ────────────────────
                        val word = vm.currentWord
                        if (word == null) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                when (vm.practiceMode) {
                                    PracticeMode.WEAK ->
                                        "🎉 No weak words!\nAll your words are at ★4 or above. Great work!"
                                    PracticeMode.MASTERY ->
                                        "⭐ No words at ★4+ yet!\nKeep practising to build mastery."
                                    PracticeMode.ALL -> ""
                                },
                                fontSize = 18.sp, textAlign = TextAlign.Center, lineHeight = 26.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        } else {

                            // 4 options: correct + 3 distractors (drawn from all words of any type)
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

                            // ── Word card ──────────────────────────────────
                            Card(
                                modifier = Modifier.fillMaxWidth().height(160.dp),
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
                                        when {
                                            // ── After answering: always reveal character + pinyin ──
                                            isAnswered -> {
                                                Text(
                                                    text = word.chinese,
                                                    fontSize = 38.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    ToneUtils.coloredAnnotatedPinyin(word.pinyin),
                                                    fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    "🇮🇩  ${word.indonesian}", fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = TextAlign.Center
                                                )
                                            }

                                            // ── LISTENING: hide character, show audio prompt ───────
                                            selectedType == PracticeType.LISTENING -> {
                                                Text("🔊", fontSize = 52.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Surface(
                                                    color = masteryColor(word.boxLevel).copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(50)
                                                ) {
                                                    Text(
                                                        "${masteryEmoji(word.boxLevel)} Mastery ${word.boxLevel}/10",
                                                        fontSize = 12.sp,
                                                        color = masteryColor(word.boxLevel),
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            // ── DEFAULT / READING: show character ─────────────────
                                            else -> {
                                                Text(
                                                    text = word.chinese,
                                                    fontSize = 56.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Surface(
                                                    color = masteryColor(word.boxLevel).copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(50)
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
                                    }

                                    // Speaker icon — hidden for READING before answering
                                    if (selectedType != PracticeType.READING || isAnswered) {
                                        IconButton(
                                            onClick = { tts.speak(word.chinese) },
                                            modifier = Modifier.align(Alignment.TopEnd)
                                        ) {
                                            Icon(
                                                Icons.Default.VolumeUp, "Hear pronunciation",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }

                            // ── Note bubble ────────────────────────────────
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

                            Spacer(modifier = Modifier.height(6.dp))

                            // ── 2×2 answer grid ────────────────────────────
                            options.chunked(2).forEach { rowOptions ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
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
                                                containerColor = containerColor,
                                                contentColor = contentColor,
                                                disabledContainerColor = containerColor,
                                                disabledContentColor = contentColor
                                            ),
                                            enabled = !isAnswered,
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                option, fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                textAlign = TextAlign.Center, lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            TextButton(onClick = { vm.finishEarly() }) {
                                Text("I'm done for now", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeSummaryScreen(
    correct: Int,
    total: Int,
    promoted: Int,
    demoted: Int,
    onDone: () -> Unit
) {
    val isPerfect = total > 0 && correct == total && demoted == 0
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
                if (isPerfect) "Perfect Session!" else "Great Practice!",
                fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("Answered", "$total", "📋")
                StatItem("Correct", "$correct", "✅")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("Promoted", "$promoted", "⬆️")
                StatItem("Demoted", "$demoted", "⬇️")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                when {
                    isPerfect -> "Everything correct and no demotions — amazing! 🌟"
                    promoted > demoted -> "More promotions than demotions — you're improving!"
                    demoted > 0 -> "Some words slipped back — keep practising!"
                    else -> "Good practice! Keep going to raise your mastery ratings."
                },
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
