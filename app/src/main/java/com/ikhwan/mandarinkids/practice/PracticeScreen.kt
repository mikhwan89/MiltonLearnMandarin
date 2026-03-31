package com.ikhwan.mandarinkids.practice

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ikhwan.mandarinkids.ToneUtils
import com.ikhwan.mandarinkids.data.models.ScenarioCategory
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import com.ikhwan.mandarinkids.db.PracticeType
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.preferences.UserPreferencesRepository
import com.ikhwan.mandarinkids.playSuccessSound
import com.ikhwan.mandarinkids.playWrongSound
import com.ikhwan.mandarinkids.tts.TtsManager
import com.ikhwan.mandarinkids.tts.rememberTtsManager
import com.ikhwan.mandarinkids.ui.ConfettiEffect
import kotlinx.coroutines.launch

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

private fun masteryGradient(level: Int): List<Color> = when {
    level <= 3 -> listOf(Color(0xFFEF5350), Color(0xFFE57373))
    level <= 6 -> listOf(Color(0xFFFF8F00), Color(0xFFFFA726))
    else       -> listOf(Color(0xFF43A047), Color(0xFF66BB6A))
}

private fun practiceTypeGradient(type: PracticeType, isDark: Boolean): List<Color> =
    if (isDark) when (type) {
        PracticeType.DEFAULT   -> listOf(Color(0xFF1A3D6E), Color(0xFF0F2A50))
        PracticeType.LISTENING -> listOf(Color(0xFF1A4558), Color(0xFF0F3242))
        PracticeType.READING   -> listOf(Color(0xFF342670), Color(0xFF261B55))
    } else when (type) {
        PracticeType.DEFAULT   -> listOf(Color(0xFFD0E8F8), Color(0xFFE4F2FB))
        PracticeType.LISTENING -> listOf(Color(0xFFB8E4F0), Color(0xFFD4EFF8))
        PracticeType.READING   -> listOf(Color(0xFFE8E4F5), Color(0xFFF2EFF9))
    }

private fun practiceModeGradient(mode: PracticeMode, isDark: Boolean): List<Color> =
    if (isDark) when (mode) {
        PracticeMode.ALL     -> listOf(Color(0xFF1A3D6E), Color(0xFF0F2A50))
        PracticeMode.WEAK    -> listOf(Color(0xFF7A1830), Color(0xFF5C1024))
        PracticeMode.MASTERY -> listOf(Color(0xFF1A4E30), Color(0xFF10382A))
    } else when (mode) {
        PracticeMode.ALL     -> listOf(Color(0xFFD0E8F8), Color(0xFFE4F2FB))
        PracticeMode.WEAK    -> listOf(Color(0xFFF5E0E0), Color(0xFFFAEEEE))
        PracticeMode.MASTERY -> listOf(Color(0xFFD4EDD0), Color(0xFFE8F5E2))
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(onBack: () -> Unit) {
    var selectedType by remember { mutableStateOf(PracticeType.DEFAULT) }
    var selectedCategory by remember { mutableStateOf<ScenarioCategory?>(null) }
    val context = LocalContext.current
    val repo = remember { ProgressRepository.getInstance(context) }
    val tts: TtsManager = rememberTtsManager()
    val userPrefs = remember { UserPreferencesRepository.getInstance(context) }
    val showIndonesian by userPrefs.showIndonesian.collectAsState(initial = true)

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

    val scenarioCategoryMap = remember {
        JsonScenarioRepository.getAll().associate { it.id to it.category }
    }

    val scope = rememberCoroutineScope()

    // ── Dark-mode-aware gradient theme ────────────────────────────────────────
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val labelColor = if (isDark) Color(0xFFE8E4D9) else Color(0xFF2A2D27)
    val wordCardGradient = if (isDark)
        listOf(Color(0xFF1A3D6E), Color(0xFF0F2A50))
    else
        listOf(Color(0xFFD0E8F8), Color(0xFFE4F2FB))
    val optionIdleGradient = if (isDark)
        listOf(Color(0xFF2A2550), Color(0xFF1E1A40))
    else
        listOf(Color(0xFFE8E4F5), Color(0xFFF2EFF9))
    val neutralGradient = listOf(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.surfaceVariant
    )

    val density = LocalDensity.current
    val drawerWidthPx = with(density) { 220.dp.toPx() }
    val closedPx     = -drawerWidthPx
    val offsetX      = remember { Animatable(closedPx) }

    // Hoisted so the drawer can read it even when the card area hasn't rendered yet
    val availableCategories = remember(vm.allWords) {
        vm.allWords.mapNotNull { scenarioCategoryMap[it.scenarioId] }.distinct()
    }

    // Auto-play TTS: DEFAULT and LISTENING auto-play; READING does not (test visual recognition)
    val currentWord = vm.currentWord
    LaunchedEffect(vm.cardToken, selectedType) {
        if (currentWord != null && selectedType != PracticeType.READING) {
            tts.speak(currentWord.chinese)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Practice type tab row ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter button sits to the left of the tabs — always reserves its space
                // to keep the tabs stable, but only shows the icon when useful
                IconButton(
                    onClick = { scope.launch { offsetX.animateTo(0f, spring()) } },
                    enabled = availableCategories.size > 1,
                    modifier = Modifier.size(48.dp)
                ) {
                    if (availableCategories.size > 1) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter by category",
                            modifier = Modifier.size(20.dp),
                            tint = if (selectedCategory != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    }
                }
                TabRow(
                    modifier = Modifier.weight(1f),
                    selectedTabIndex = PracticeType.values().indexOf(selectedType),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    PracticeType.values().forEach { type ->
                        val isSelected = selectedType == type
                        Tab(
                            selected = isSelected,
                            onClick = { selectedType = type },
                            modifier = if (isSelected) Modifier.background(
                                Brush.verticalGradient(practiceTypeGradient(type, isDark))
                            ) else Modifier,
                            text = {
                                Text(
                                    "${type.emoji} ${type.displayName}",
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    color = if (isSelected) labelColor
                                            else MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        )
                    }
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
                            PracticeMode.values().forEachIndexed { index, mode ->
                                val modeLabel = when (mode) {
                                    PracticeMode.ALL     -> "All Words"
                                    PracticeMode.WEAK    -> "Weak Words"
                                    PracticeMode.MASTERY -> "Maintain"
                                }
                                SegmentedButton(
                                    selected = vm.practiceMode == mode,
                                    onClick = { vm.setMode(mode) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = practiceModeGradient(mode, isDark).first(),
                                        activeContentColor = labelColor,
                                        inactiveContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        inactiveContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Text(modeLabel, fontSize = 11.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }

                        // ── Star rating distribution (respects both category filter and mode) ──
                        val levelCounts = remember(vm.allWords, vm.practiceMode, vm.categoryFilter) {
                            val filtered = if (vm.categoryFilter != null)
                                vm.allWords.filter { scenarioCategoryMap[it.scenarioId] == vm.categoryFilter }
                            else vm.allWords
                            val levels = filtered.map { it.boxLevel }.distinct().sorted()
                            val weakLevels   = levels.take(3).toSet()
                            val masteryLevels = levels.takeLast(3).filter { it >= 4 }.toSet()
                            val all = filtered.groupBy { it.boxLevel }.entries.sortedBy { it.key }
                            when (vm.practiceMode) {
                                PracticeMode.WEAK    -> all.filter { (level, _) -> level in weakLevels }
                                PracticeMode.MASTERY -> all.filter { (level, _) -> level in masteryLevels }
                                PracticeMode.ALL     -> all
                            }
                        }
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(levelCounts) { (level, words) ->
                                val pillShape = RoundedCornerShape(50)
                                Box(
                                    modifier = Modifier
                                        .clip(pillShape)
                                        .background(Brush.verticalGradient(masteryGradient(level)))
                                        .padding(horizontal = 9.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "${masteryEmoji(level)} $level ✦ ${words.size}",
                                        fontSize = 12.sp, color = Color.White,
                                        fontWeight = FontWeight.SemiBold
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
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.verticalGradient(wordCardGradient)),
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
                                                    textAlign = TextAlign.Center,
                                                    color = labelColor
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    ToneUtils.coloredAnnotatedPinyin(word.pinyin),
                                                    fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                if (showIndonesian) Text(
                                                    "🇮🇩  ${word.indonesian}", fontSize = 12.sp,
                                                    color = labelColor.copy(alpha = 0.7f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }

                                            // ── LISTENING: hide character, show audio prompt ───────
                                            selectedType == PracticeType.LISTENING -> {
                                                Text("🔊", fontSize = 52.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(50))
                                                        .background(Brush.verticalGradient(masteryGradient(word.boxLevel)))
                                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        "${masteryEmoji(word.boxLevel)} Mastery ${word.boxLevel}/10",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.White
                                                    )
                                                }
                                            }

                                            // ── DEFAULT / READING: show character ─────────────────
                                            else -> {
                                                Text(
                                                    text = word.chinese,
                                                    fontSize = 56.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                    color = labelColor
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(50))
                                                        .background(Brush.verticalGradient(masteryGradient(word.boxLevel)))
                                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        "${masteryEmoji(word.boxLevel)} Mastery ${word.boxLevel}/10",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.White
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
                                                tint = labelColor.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }

                            // ── +1 XP indicator ────────────────────────────
                            if (isAnswered && answeredCorrectly) {
                                Text(
                                    "+1 XP ✨",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // ── Note bubble ────────────────────────────────
                            if (isAnswered && word.note != null) {
                                val noteDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                                Surface(
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Box(modifier = Modifier.background(Brush.verticalGradient(
                                        if (noteDark) listOf(Color(0xFF6B5208), Color(0xFF4E3C06))
                                        else          listOf(Color(0xFFFFF0B3), Color(0xFFFFF8D9))
                                    ))) {
                                        Text(
                                            "💡 ${word.note}", fontSize = 13.sp,
                                            color = if (noteDark) Color(0xFFE8E4D9) else Color(0xFF2A2D27),
                                            textAlign = TextAlign.Center, lineHeight = 19.sp,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
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
                                        val isCorrectOption  = isAnswered && option == word.english
                                        val isWrongSelection = isAnswered && option == selectedAnswer && !answeredCorrectly
                                        val isIdleAfter      = isAnswered && !isCorrectOption && option != selectedAnswer
                                        val tileGradient = when {
                                            isCorrectOption  -> listOf(Color(0xFF43A047), Color(0xFF66BB6A))
                                            isWrongSelection -> listOf(Color(0xFFE53935), Color(0xFFEF5350))
                                            isIdleAfter      -> neutralGradient
                                            else             -> optionIdleGradient
                                        }
                                        val contentColor = when {
                                            isIdleAfter                     -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            isCorrectOption || isWrongSelection -> Color.White
                                            else                            -> labelColor
                                        }
                                        val tileShape = RoundedCornerShape(12.dp)
                                        Surface(
                                            onClick = {
                                                if (!isAnswered) {
                                                    selectedAnswer = option
                                                    if (option == word.english) playSuccessSound()
                                                    else playWrongSound()
                                                }
                                            },
                                            shape = tileShape,
                                            modifier = Modifier.weight(1f).height(74.dp),
                                            color = Color.Transparent
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Brush.verticalGradient(tileGradient)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    option, fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    textAlign = TextAlign.Center, lineHeight = 18.sp,
                                                    color = contentColor,
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                )
                                            }
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

        // ── Scrim ──────────────────────────────────────────────────────────
        val scrimAlpha = ((offsetX.value - closedPx) / (-closedPx) * 0.4f).coerceIn(0f, 0.4f)
        if (scrimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            scope.launch { offsetX.animateTo(closedPx, spring()) }
                        }
                    }
            )
        }

        // ── Peeking drawer panel ────────────────────────────────────────────
        // Right 18 dp strip is always visible, acting as a colour tab hint.
        // Drag right anywhere on the panel to open; drag left or tap scrim to close.
        Surface(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                val target = if (offsetX.value > closedPx / 2) 0f else closedPx
                                offsetX.animateTo(target, spring())
                            }
                        }
                    ) { _, dragAmount ->
                        scope.launch {
                            offsetX.snapTo((offsetX.value + dragAmount).coerceIn(closedPx, 0f))
                        }
                    }
                },
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    "Filter by Category",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                NavigationDrawerItem(
                    label = { Text("🌐 All Categories") },
                    selected = selectedCategory == null,
                    onClick = {
                        selectedCategory = null
                        vm.setCategory(null)
                        scope.launch { offsetX.animateTo(closedPx, spring()) }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                availableCategories.forEach { cat ->
                    NavigationDrawerItem(
                        label = { Text("${cat.emoji} ${cat.displayName}") },
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = cat
                            vm.setCategory(cat)
                            scope.launch { offsetX.animateTo(closedPx, spring()) }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

    } // end Box
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
            Surface(
                onClick = onDone,
                shape = RoundedCornerShape(50),
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFF388E3C), Color(0xFF66BB6A)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Done", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
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
