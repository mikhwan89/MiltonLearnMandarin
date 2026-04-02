package com.ikhwan.mandarinkids

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.ikhwan.mandarinkids.ui.theme.appColors
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ikhwan.mandarinkids.data.models.PinyinWord
import com.ikhwan.mandarinkids.data.models.Scenario
import com.ikhwan.mandarinkids.preferences.UserPreferencesRepository
import com.ikhwan.mandarinkids.ui.StrokeOrderSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

fun Scenario.getFlashcardWords(): List<PinyinWord> {
    return dialogues
        .flatMap { step -> step.pinyinWords + step.options.flatMap { it.pinyinWords } }
        .distinctBy { it.chinese }
        .filter { it.chinese.isNotBlank() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    scenario: Scenario,
    canSkip: Boolean = false,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { com.ikhwan.mandarinkids.db.ProgressRepository.getInstance(context) }
    val userPrefs = remember { UserPreferencesRepository.getInstance(context) }
    val showIndonesian by userPrefs.showIndonesian.collectAsState(initial = true)
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val vm: FlashcardViewModel = viewModel(
        key = scenario.id,
        factory = FlashcardViewModel.factory(scenario, repo)
    )

    val currentWord = vm.currentWord
    var showStrokeOrder by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.CHINESE
                ttsReady = true
            }
        }
    }

    // Auto-play when card changes, or when TTS becomes ready for the first card
    LaunchedEffect(vm.currentIndex, vm.deck.size, ttsReady) {
        val w = vm.currentWord
        if (ttsReady && w != null && !vm.isFlipped) {
            tts?.speak(w.chinese, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    DisposableEffect(Unit) {
        onDispose { tts?.shutdown() }
    }

    val rotation by animateFloatAsState(
        targetValue = if (vm.isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlip"
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Flashcards 单词卡", fontSize = 16.sp)
                        Text(
                            "${vm.masteredCount} / ${vm.allWords.size} words mastered",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (canSkip) {
                        TextButton(onClick = onComplete) {
                            Text("Skip →", fontSize = 14.sp)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (vm.showComplete) {
            FlashcardCompleteScreen(
                totalWords = vm.allWords.size,
                onStartConversation = onComplete
            )
        } else if (currentWord != null) {
            val word = currentWord
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                LinearProgressIndicator(
                    progress = { vm.masteredCount.toFloat() / vm.allWords.size },
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
                    text = if (!vm.isFlipped) "Tap card to reveal translation" else "Tap card to flip back",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Flip card
                val colors = MaterialTheme.appColors
                val frontGradient = colors.tileBlue.asList()
                val backGradient  = colors.tileGreen.asList()
                val cardLabelColor = colors.onLightTile
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .graphicsLayer { rotationY = rotation }
                        .clickable {
                            vm.flip()
                            if (!vm.isFlipped) {
                                tts?.speak(word.chinese, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        },
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(if (rotation <= 90f) frontGradient else backGradient)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (rotation <= 90f) {
                            // Front — Chinese character + pinyin
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = scenario.characterEmoji,
                                    fontSize = 36.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Text(
                                    text = word.chinese,
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = ToneUtils.coloredAnnotatedPinyin(word.pinyin, colors),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(onClick = {
                                        tts?.setSpeechRate(1.0f)
                                        tts?.speak(word.chinese, TextToSpeech.QUEUE_FLUSH, null, null)
                                    }) {
                                        Text("🔊 Normal", fontSize = 13.sp)
                                    }
                                    TextButton(onClick = {
                                        coroutineScope.launch {
                                            tts?.setSpeechRate(0.5f)
                                            tts?.speak(word.chinese, TextToSpeech.QUEUE_FLUSH, null, null)
                                            delay(3000)
                                            tts?.setSpeechRate(1.0f)
                                        }
                                    }) {
                                        Text("🐢 Slow", fontSize = 13.sp)
                                    }
                                }
                                TextButton(onClick = { showStrokeOrder = true }) {
                                    Text("✏️ How to write", fontSize = 13.sp)
                                }
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
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = ToneUtils.coloredAnnotatedPinyin(word.pinyin, colors),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(onClick = {
                                        tts?.setSpeechRate(1.0f)
                                        tts?.speak(word.chinese, TextToSpeech.QUEUE_FLUSH, null, null)
                                    }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play pronunciation", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("Normal", fontSize = 12.sp)
                                    }
                                    TextButton(onClick = {
                                        coroutineScope.launch {
                                            tts?.setSpeechRate(0.5f)
                                            tts?.speak(word.chinese, TextToSpeech.QUEUE_FLUSH, null, null)
                                            delay(3000)
                                            tts?.setSpeechRate(1.0f)
                                        }
                                    }) {
                                        Text("🐢 Slow", fontSize = 12.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "🇬🇧  ${word.english}",
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (showIndonesian) {
                                    Text(
                                        text = "🇮🇩  ${word.indonesian}",
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Note bubble — shown below the card after flip, only for words that have a note
                AnimatedVisibility(
                    visible = vm.isFlipped && word.note != null,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(150))
                ) {
                    word.note?.let { note ->
                        Surface(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Box(modifier = Modifier.background(Brush.verticalGradient(
                                colors.tileAmber.asList()
                            ))) {
                                Text(
                                    text = "💡 $note",
                                    fontSize = 14.sp,
                                    color = colors.onLightTile,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons — only visible after flip
                if (vm.isFlipped) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            onClick = { vm.markStillLearning() },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent,
                            modifier = Modifier.weight(1f).height(60.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.verticalGradient(colors.actionNegative.asList())),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✗  Still learning", fontSize = 15.sp, color = Color.White)
                            }
                        }

                        Surface(
                            onClick = { vm.markMastered() },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent,
                            modifier = Modifier.weight(1f).height(60.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.verticalGradient(colors.actionPositive.asList())),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓  Got it!", fontSize = 15.sp, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Skip to conversation button
                TextButton(onClick = onComplete) {
                    Text("Skip flashcards → go to conversation", fontSize = 13.sp)
                }
                }
            } // Box
        }
    }

    if (showStrokeOrder && currentWord != null) {
        StrokeOrderSheet(
            chinese = currentWord.chinese,
            pinyin = currentWord.pinyin,
            onDismiss = { showStrokeOrder = false }
        )
    }
}

@Composable
fun FlashcardCompleteScreen(
    totalWords: Int,
    onStartConversation: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "All Words Reviewed!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "你记住了 $totalWords 个词！",
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "You reviewed all $totalWords words!\nNow let's use them in a real conversation.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        val colors = MaterialTheme.appColors
        Surface(
            onClick = onStartConversation,
            shape = RoundedCornerShape(50),
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors.actionPositive.asList())),
                contentAlignment = Alignment.Center
            ) {
                Text("Start Conversation", fontSize = 18.sp,
                    color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
