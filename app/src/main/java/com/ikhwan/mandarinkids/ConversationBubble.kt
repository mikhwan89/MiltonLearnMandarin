package com.ikhwan.mandarinkids

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ikhwan.mandarinkids.data.models.ConversationMessage
import com.ikhwan.mandarinkids.data.models.PinyinWord
import com.ikhwan.mandarinkids.data.models.Speaker
import com.ikhwan.mandarinkids.tts.TtsManager

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConversationBubble(
    message: ConversationMessage,
    characterName: String,
    characterEmoji: String,
    tts: TtsManager,
    speechSpeed: Float,
    isSpeaking: Boolean = false,
    showIndonesian: Boolean = true
) {
    val isCharacter = message.speaker == Speaker.CHARACTER
    var showWordDialog by remember { mutableStateOf(false) }
    var selectedWord by remember { mutableStateOf<PinyinWord?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "speaking")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isSpeaking && isCharacter) -10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(350),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceY"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCharacter) Arrangement.Start else Arrangement.End
    ) {
        if (isCharacter) {
            Text(
                text = characterEmoji,
                fontSize = 40.sp,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .graphicsLayer { translationY = bounceY }
            )
        }

        val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
        val bubbleGradient = if (isCharacter) {
            if (isDark) listOf(Color(0xFF1A3D6E), Color(0xFF0F2A50))
            else        listOf(Color(0xFFD0E8F8), Color(0xFFE4F2FB))
        } else {
            if (isDark) listOf(Color(0xFF1A4E30), Color(0xFF10382A))
            else        listOf(Color(0xFFD4EDD0), Color(0xFFE8F5E2))
        }
        val bubbleLabelColor = if (isDark) Color(0xFFE8E4D9) else Color(0xFF2A2D27)
        val bubbleShape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isCharacter) 4.dp else 16.dp,
            bottomEnd = if (isCharacter) 16.dp else 4.dp
        )

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isCharacter) Alignment.Start else Alignment.End
        ) {
            Text(
                text = if (isCharacter) characterName else "You",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Surface(
                color = Color.Transparent,
                shape = bubbleShape
            ) {
                Box(
                    modifier = Modifier.background(Brush.verticalGradient(bubbleGradient))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = message.textChinese,
                                fontSize = 18.sp,
                                color = bubbleLabelColor,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { tts.speak(message.textChinese, speechSpeed) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Play pronunciation",
                                    modifier = Modifier.size(20.dp),
                                    tint = bubbleLabelColor.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (message.pinyinWords.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                message.pinyinWords.forEach { word ->
                                    val pillBg = ToneUtils.pinyinColor(
                                        ToneUtils.splitSyllables(word.pinyin).first()
                                    ).copy(alpha = 0.18f)
                                    Text(
                                        text = ToneUtils.coloredAnnotatedPinyin(word.pinyin),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clickable {
                                                selectedWord = word
                                                showWordDialog = true
                                            }
                                            .background(pillBg, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = message.textPinyin,
                                fontSize = 14.sp,
                                color = bubbleLabelColor.copy(alpha = 0.75f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "🇬🇧 ${message.textEnglish}",
                            fontSize = 13.sp,
                            color = bubbleLabelColor.copy(alpha = 0.8f)
                        )

                        if (showIndonesian) {
                            Text(
                                text = "🇮🇩 ${message.textIndonesian}",
                                fontSize = 13.sp,
                                color = bubbleLabelColor.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }
        }

        if (!isCharacter) {
            Text(
                text = "👦",
                fontSize = 40.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }

    if (showWordDialog && selectedWord != null) {
        WordDetailDialog(
            word = selectedWord!!,
            tts = tts,
            speechSpeed = speechSpeed,
            showIndonesian = showIndonesian,
            onDismiss = { showWordDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordDetailDialog(
    word: PinyinWord,
    tts: TtsManager,
    speechSpeed: Float,
    showIndonesian: Boolean,
    onDismiss: () -> Unit
) {
    val syllables = ToneUtils.splitSyllables(word.pinyin)
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = word.chinese,
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = ToneUtils.coloredAnnotatedPinyin(word.pinyin),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    syllables.forEach { syllable ->
                        val tone = ToneUtils.detectTone(syllable)
                        val toneCol = ToneUtils.toneColor(tone)
                        Surface(
                            color = toneCol.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = ToneUtils.toneLabel(tone),
                                fontSize = 12.sp,
                                color = toneCol,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "🇬🇧 ${word.english}",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                if (showIndonesian) {
                    Text(
                        text = "🇮🇩 ${word.indonesian}",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = if (word.note != null) 12.dp else 16.dp)
                    )
                }
                if (word.note != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "💡 ${word.note}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    OutlinedButton(onClick = { tts.speak(word.chinese, speechSpeed) }) {
                        Text("🔊 Normal")
                    }
                    OutlinedButton(onClick = { tts.speak(word.chinese, rate = 0.5f) }) {
                        Text("🐢 Slow")
                    }
                }
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}
