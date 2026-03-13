package com.ikhwan.mandarinkids

import android.speech.tts.TextToSpeech
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
import com.ikhwan.mandarinkids.data.models.PinyinWord
import com.ikhwan.mandarinkids.data.models.Scenario
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
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    val allWords = remember { scenario.getFlashcardWords() }
    var deck by remember { mutableStateOf(allWords) }
    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var masteredCount by remember { mutableStateOf(0) }
    var showComplete by remember { mutableStateOf(false) }

    val currentWord = if (deck.isNotEmpty() && currentIndex < deck.size) deck[currentIndex] else null

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.CHINESE
            }
        }
    }

    // Auto-play when card changes
    LaunchedEffect(currentIndex, deck.size) {
        if (currentWord != null && !isFlipped) {
            tts?.speak(currentWord.chinese, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    DisposableEffect(Unit) {
        onDispose { tts?.shutdown() }
    }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlip"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Flashcards 单词卡", fontSize = 16.sp)
                        Text(
                            "$masteredCount / ${allWords.size} words mastered",
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
        if (showComplete) {
            FlashcardCompleteScreen(
                totalWords = allWords.size,
                onStartConversation = onComplete
            )
        } else if (currentWord != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { masteredCount.toFloat() / allWords.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                Text(
                    text = "${deck.size} card${if (deck.size != 1) "s" else ""} remaining",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Text(
                    text = if (!isFlipped) "Tap card to reveal" else "Tap card to flip back",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Flip card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .graphicsLayer { rotationY = rotation }
                        .clickable {
                            isFlipped = !isFlipped
                            if (!isFlipped) {
                                tts?.speak(currentWord.chinese, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        },
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
                            // Front — Chinese character
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
                                    text = currentWord.chinese,
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                TextButton(onClick = {
                                    tts?.speak(currentWord.chinese, TextToSpeech.QUEUE_FLUSH, null, null)
                                }) {
                                    Text("🔊 Hear it again", fontSize = 14.sp)
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
                                    text = currentWord.chinese,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = currentWord.pinyin,
                                    fontSize = 26.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "🇬🇧  ${currentWord.english}",
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "🇮🇩  ${currentWord.indonesian}",
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons — only visible after flip
                if (isFlipped) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // Move word to end of deck for another round
                                val word = deck[currentIndex]
                                val newDeck = deck.toMutableList().also {
                                    it.removeAt(currentIndex)
                                    it.add(word)
                                }
                                deck = newDeck
                                currentIndex = if (currentIndex >= newDeck.size) 0 else currentIndex
                                isFlipped = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFF44336)
                            )
                        ) {
                            Text("✗  Still learning", fontSize = 15.sp)
                        }

                        Button(
                            onClick = {
                                val newDeck = deck.toMutableList().also { it.removeAt(currentIndex) }
                                masteredCount++
                                deck = newDeck
                                if (newDeck.isEmpty()) {
                                    showComplete = true
                                } else {
                                    currentIndex = if (currentIndex >= newDeck.size) 0 else currentIndex
                                }
                                isFlipped = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text("✓  Got it!", fontSize = 15.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Skip to conversation button
                TextButton(onClick = onComplete) {
                    Text("Skip flashcards → go to conversation", fontSize = 13.sp)
                }
            }
        }
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
            "All Words Mastered!",
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
            "You remembered all $totalWords words!\nNow let's use them in a real conversation.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onStartConversation,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text("Start Conversation! 开始对话 ▶", fontSize = 18.sp)
        }
    }
}
