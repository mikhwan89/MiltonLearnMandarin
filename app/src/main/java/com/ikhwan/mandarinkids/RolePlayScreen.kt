package com.ikhwan.mandarinkids

import android.speech.tts.TextToSpeech
import android.os.Bundle
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume
import com.ikhwan.mandarinkids.data.models.*

data class ConversationMessage(
    val speaker: Speaker,
    val textChinese: String,
    val textPinyin: String,
    val textEnglish: String,
    val textIndonesian: String,
    val pinyinWords: List<PinyinWord> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolePlayScreen(
    scenario: Scenario,
    onComplete: (score: Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var currentStepIndex by remember { mutableStateOf(0) }
    var userName by remember { mutableStateOf("") }
    var showNameInput by remember { mutableStateOf(false) }
    var conversationHistory by remember { mutableStateOf<List<ConversationMessage>>(emptyList()) }
    var showOptions by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var correctAnswersCount by remember { mutableStateOf(0) }
    var speechSpeed by remember { mutableStateOf(1.0f) }

    // Track if we're currently processing a step
    var isProcessingStep by remember { mutableStateOf(false) }

    // Initialize TTS
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.CHINESE
                tts?.setSpeechRate(speechSpeed)
            }
        }
    }

    LaunchedEffect(speechSpeed) {
        tts?.setSpeechRate(speechSpeed)
    }

    DisposableEffect(Unit) {
        onDispose { tts?.shutdown() }
    }

    val currentStep = if (currentStepIndex < scenario.dialogues.size) {
        scenario.dialogues[currentStepIndex]
    } else null

    // Process each step ONCE
    LaunchedEffect(currentStepIndex) {
        // Prevent re-entry
        if (isProcessingStep) return@LaunchedEffect

        // All steps done (e.g. last step was LISTEN_ONLY)
        if (currentStep == null) {
            onComplete(correctAnswersCount)
            return@LaunchedEffect
        }

        if (currentStep.speaker == Speaker.CHARACTER) {
            isProcessingStep = true
            showOptions = false
            showNameInput = false

            try {
                // Wait a bit before starting
                delay(500)

                // Add message to history
                conversationHistory = conversationHistory + ConversationMessage(
                    speaker = Speaker.CHARACTER,
                    textChinese = currentStep.textChinese,
                    textPinyin = currentStep.textPinyin,
                    textEnglish = currentStep.textEnglish,
                    textIndonesian = currentStep.textIndonesian,
                    pinyinWords = currentStep.pinyinWords
                )

                // Scroll to bottom
                delay(200)
                coroutineScope.launch {
                    listState.animateScrollToItem(
                        index = maxOf(0, conversationHistory.size - 1)
                    )
                }

                // Speak and wait for completion
                delay(300)
                suspendCancellableCoroutine<Unit> { continuation ->
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}

                        override fun onDone(utteranceId: String?) {
                            if (utteranceId == "char_${currentStepIndex}") {
                                continuation.resume(Unit)
                            }
                        }

                        override fun onError(utteranceId: String?) {
                            if (utteranceId == "char_${currentStepIndex}") {
                                continuation.resume(Unit)
                            }
                        }
                    })

                    val params = Bundle()
                    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "char_${currentStepIndex}")
                    tts?.speak(currentStep.textChinese, TextToSpeech.QUEUE_FLUSH, params, "char_${currentStepIndex}")

                    continuation.invokeOnCancellation {
                        tts?.stop()
                    }
                }

                // Speech complete, wait a bit more
                delay(800)

                // Show options or auto-advance
                if (currentStep.userNameInput) {
                    showNameInput = true
                } else if (currentStep.responseType == ResponseType.LISTEN_ONLY) {
                    delay(1500)
                    currentStepIndex++
                } else {
                    showOptions = true
                }
            } finally {
                isProcessingStep = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(scenario.title, fontSize = 16.sp)
                        Text(
                            "Step ${currentStepIndex + 1}/${scenario.dialogues.size}",
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
                    IconButton(
                        onClick = {
                            speechSpeed = if (speechSpeed == 1.0f) 0.7f else 1.0f
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = "Speech Speed",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (speechSpeed == 1.0f) "1x" else "0.7x",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LinearProgressIndicator(
                progress = { (currentStepIndex + 1).toFloat() / scenario.dialogues.size },
                modifier = Modifier.fillMaxWidth(),
            )

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Conversation area
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(bottom = if (showOptions || showNameInput) 200.dp else 0.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(conversationHistory.size) { index ->
                        val message = conversationHistory[index]
                        ConversationBubble(
                            message = message,
                            characterName = scenario.characterName,
                            characterEmoji = scenario.characterEmoji,
                            tts = tts,
                            speechSpeed = speechSpeed
                        )
                    }
                }

                // Options overlay at bottom
                if (currentStep != null && (showOptions || showNameInput)) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            if (showNameInput && currentStep.userNameInput) {
                                NameInputSection(
                                    option = currentStep.options.first(),
                                    onNameEntered = { name ->
                                        userName = name
                                        showNameInput = false

                                        val fullResponse = "${currentStep.options.first().chinese}${name}"
                                        conversationHistory = conversationHistory + ConversationMessage(
                                            speaker = Speaker.STUDENT,
                                            textChinese = fullResponse,
                                            textPinyin = "${currentStep.options.first().pinyin} $name",
                                            textEnglish = "${currentStep.options.first().english} $name",
                                            textIndonesian = "${currentStep.options.first().indonesian} $name",
                                            pinyinWords = currentStep.options.first().pinyinWords
                                        )

                                        val params = Bundle()
                                        tts?.speak(fullResponse, TextToSpeech.QUEUE_FLUSH, params, null)
                                        correctAnswersCount++

                                        coroutineScope.launch {
                                            delay(300)
                                            listState.animateScrollToItem(conversationHistory.size - 1)
                                            delay(2000)
                                            currentStepIndex++
                                        }
                                    },
                                    tts = tts,
                                    speechSpeed = speechSpeed
                                )
                            }

                            if (showOptions && !showNameInput) {
                                Text(
                                    text = "Choose your response:",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                currentStep.options.forEachIndexed { index, option ->
                                    ResponseOptionButton(
                                        option = option,
                                        index = index,
                                        onClick = {
                                            showOptions = false  // Hide immediately

                                            conversationHistory = conversationHistory + ConversationMessage(
                                                speaker = Speaker.STUDENT,
                                                textChinese = option.chinese,
                                                textPinyin = option.pinyin,
                                                textEnglish = option.english,
                                                textIndonesian = option.indonesian,
                                                pinyinWords = option.pinyinWords
                                            )

                                            val params = Bundle()
                                            tts?.speak(option.chinese, TextToSpeech.QUEUE_FLUSH, params, null)

                                            if (option.isCorrect) {
                                                correctAnswersCount++
                                            }

                                            coroutineScope.launch {
                                                delay(300)
                                                listState.animateScrollToItem(conversationHistory.size - 1)
                                                delay(2000)
                                                if (currentStepIndex + 1 >= scenario.dialogues.size) {
                                                    onComplete(correctAnswersCount)
                                                } else {
                                                    currentStepIndex++
                                                }
                                            }
                                        },
                                        tts = tts,
                                        speechSpeed = speechSpeed
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationBubble(
    message: ConversationMessage,
    characterName: String,
    characterEmoji: String,
    tts: TextToSpeech?,
    speechSpeed: Float
) {
    val isCharacter = message.speaker == Speaker.CHARACTER
    var showWordDialog by remember { mutableStateOf(false) }
    var selectedWord by remember { mutableStateOf<PinyinWord?>(null) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCharacter) Arrangement.Start else Arrangement.End
    ) {
        if (isCharacter) {
            Text(
                text = characterEmoji,
                fontSize = 40.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

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
                color = if (isCharacter)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isCharacter) 4.dp else 16.dp,
                    bottomEnd = if (isCharacter) 16.dp else 4.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message.textChinese,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )

                        if (isCharacter) {
                            IconButton(
                                onClick = {
                                    tts?.setSpeechRate(speechSpeed)
                                    val params = Bundle()
                                    tts?.speak(message.textChinese, TextToSpeech.QUEUE_FLUSH, params, null)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Clickable pinyin words
                    if (message.pinyinWords.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(message.pinyinWords) { word ->
                                Text(
                                    text = word.pinyin,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable {
                                            selectedWord = word
                                            showWordDialog = true
                                        }
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = message.textPinyin,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "🇬🇧 ${message.textEnglish}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "🇮🇩 ${message.textIndonesian}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

    // Word translation dialog
    if (showWordDialog && selectedWord != null) {
        Dialog(onDismissRequest = { showWordDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = selectedWord!!.chinese,
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = selectedWord!!.pinyin,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "🇬🇧 ${selectedWord!!.english}",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "🇮🇩 ${selectedWord!!.indonesian}",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(onClick = { showWordDialog = false }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
fun NameInputSection(
    option: ResponseOption,
    onNameEntered: (String) -> Unit,
    tts: TextToSpeech?,
    speechSpeed: Float
) {
    var name by remember { mutableStateOf("") }

    Column {
        Text(
            text = "Introduce yourself!",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = option.chinese, fontSize = 16.sp)
                    Text(
                        text = option.pinyin,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "🇬🇧 ${option.english}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        tts?.setSpeechRate(speechSpeed)
                        val params = Bundle()
                        tts?.speak(option.chinese, TextToSpeech.QUEUE_FLUSH, params, null)
                    }
                ) {
                    Icon(Icons.Default.PlayArrow, "Play")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your name") },
            placeholder = { Text("Enter your name here") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (name.isNotBlank()) {
                    onNameEntered(name.trim())
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank()
        ) {
            Text("Say it! 说吧！", fontSize = 16.sp)
        }
    }
}

@Composable
fun ResponseOptionButton(
    option: ResponseOption,
    index: Int,
    onClick: () -> Unit,
    tts: TextToSpeech?,
    speechSpeed: Float
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            isPressed = true
            onClick()
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50),
                modifier = Modifier.size(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = (index + 1).toString(),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.chinese,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = option.pinyin,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "🇬🇧 ${option.english}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "🇮🇩 ${option.indonesian}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = {
                    tts?.setSpeechRate(speechSpeed)
                    val params = Bundle()
                    tts?.speak(option.chinese, TextToSpeech.QUEUE_FLUSH, params, null)
                }
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}