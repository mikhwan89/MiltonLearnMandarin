package com.ikhwan.mandarinkids

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import com.ikhwan.mandarinkids.preferences.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ikhwan.mandarinkids.tts.TtsManager
import com.ikhwan.mandarinkids.tts.rememberTtsManager
import androidx.compose.ui.text.font.FontWeight
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
    val tts = rememberTtsManager()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val vm: RolePlayViewModel = viewModel(
        key = scenario.id,
        factory = RolePlayViewModel.factory(scenario)
    )

    val userPrefs = remember { UserPreferencesRepository.getInstance(context) }
    val storedSpeed by userPrefs.speechRate.collectAsState(initial = 1.0f)
    LaunchedEffect(storedSpeed) { vm.applyStoredSpeed(storedSpeed) }

    // Process each step ONCE
    LaunchedEffect(vm.currentStepIndex) {
        // Prevent re-entry
        if (vm.isProcessingStep) return@LaunchedEffect

        val step = vm.currentStep

        // All steps done (e.g. last step was LISTEN_ONLY)
        if (step == null) {
            onComplete(vm.correctAnswersCount)
            return@LaunchedEffect
        }

        if (step.speaker == Speaker.CHARACTER) {
            vm.beginCharacterTurn()

            try {
                delay(500)

                vm.addMessage(ConversationMessage(
                    speaker = Speaker.CHARACTER,
                    textChinese = step.textChinese,
                    textPinyin = step.textPinyin,
                    textEnglish = step.textEnglish,
                    textIndonesian = step.textIndonesian,
                    pinyinWords = step.pinyinWords
                ))

                delay(200)
                coroutineScope.launch {
                    listState.animateScrollToItem(
                        index = maxOf(0, vm.conversationHistory.size - 1)
                    )
                }

                delay(300)
                tts.speakAndAwait(step.textChinese, "char_${vm.currentStepIndex}", vm.speechSpeed)

                delay(800)

                if (step.userNameInput) {
                    vm.revealNameInput()
                } else if (step.responseType == ResponseType.LISTEN_ONLY) {
                    delay(1500)
                    vm.advanceStep()
                } else {
                    vm.revealOptions()
                }
            } finally {
                vm.finishProcessing()
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
                            "Step ${vm.currentStepIndex + 1}/${scenario.dialogues.size}",
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
                            vm.toggleSpeechSpeed()
                            coroutineScope.launch { userPrefs.saveSpeechRate(vm.speechSpeed) }
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
                                text = if (vm.speechSpeed == 1.0f) "1x" else "0.7x",
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
                progress = { (vm.currentStepIndex + 1).toFloat() / scenario.dialogues.size },
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
                        .padding(bottom = if (vm.showOptions || vm.showNameInput) 200.dp else 0.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(vm.conversationHistory.size) { index ->
                        val message = vm.conversationHistory[index]
                        ConversationBubble(
                            message = message,
                            characterName = scenario.characterName,
                            characterEmoji = scenario.characterEmoji,
                            tts = tts,
                            speechSpeed = vm.speechSpeed,
                            isSpeaking = vm.isProcessingStep && index == vm.conversationHistory.size - 1
                        )
                    }
                }

                // Options overlay at bottom
                if (vm.currentStep != null && (vm.showOptions || vm.showNameInput)) {
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
                            if (vm.showNameInput && vm.currentStep!!.userNameInput) {
                                NameInputSection(
                                    option = vm.currentStep!!.options.first(),
                                    onNameEntered = { name ->
                                        val fullChinese = vm.submitName(name)
                                        tts.speak(fullChinese, vm.speechSpeed)
                                        coroutineScope.launch {
                                            delay(300)
                                            listState.animateScrollToItem(vm.conversationHistory.size - 1)
                                            delay(2000)
                                            vm.advanceStep()
                                        }
                                    },
                                    tts = tts,
                                    speechSpeed = vm.speechSpeed
                                )
                            }

                            if (vm.showOptions && !vm.showNameInput) {
                                Text(
                                    text = "Choose your response:",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                vm.currentStep!!.options.forEachIndexed { index, option ->
                                    ResponseOptionButton(
                                        option = option,
                                        index = index,
                                        onClick = {
                                            vm.selectOption(option)
                                            tts.speak(option.chinese, vm.speechSpeed)
                                            coroutineScope.launch {
                                                delay(300)
                                                listState.animateScrollToItem(vm.conversationHistory.size - 1)
                                                delay(2000)
                                                if (vm.currentStepIndex + 1 >= scenario.dialogues.size) {
                                                    onComplete(vm.correctAnswersCount)
                                                } else {
                                                    vm.advanceStep()
                                                }
                                            }
                                        },
                                        tts = tts,
                                        speechSpeed = vm.speechSpeed
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
    tts: TtsManager,
    speechSpeed: Float,
    isSpeaking: Boolean = false
) {
    val isCharacter = message.speaker == Speaker.CHARACTER
    var showWordDialog by remember { mutableStateOf(false) }
    var selectedWord by remember { mutableStateOf<PinyinWord?>(null) }

    // Bounce animation while character is speaking
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
                                    tts.speak(message.textChinese, speechSpeed)
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

                    // Clickable pinyin words (tap = word detail, tone-coloured)
                    if (message.pinyinWords.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(message.pinyinWords) { word ->
                                val toneCol = ToneUtils.pinyinColor(word.pinyin)
                                Text(
                                    text = word.pinyin,
                                    fontSize = 14.sp,
                                    color = toneCol,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clickable {
                                            selectedWord = word
                                            showWordDialog = true
                                        }
                                        .background(
                                            toneCol.copy(alpha = 0.12f),
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
        val word = selectedWord!!
        val tone = ToneUtils.detectTone(word.pinyin)
        val toneCol = ToneUtils.toneColor(tone)
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
                        text = word.chinese,
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = word.pinyin,
                        fontSize = 24.sp,
                        color = toneCol,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    // Tone label with coloured dot
                    Surface(
                        color = toneCol.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = ToneUtils.toneLabel(tone),
                            fontSize = 12.sp,
                            color = toneCol,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "🇬🇧 ${word.english}",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "🇮🇩 ${word.indonesian}",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    // Play buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        OutlinedButton(onClick = {
                            tts.speak(word.chinese, speechSpeed)
                        }) {
                            Text("🔊 Normal")
                        }
                        OutlinedButton(onClick = {
                            tts.speak(word.chinese, rate = 0.5f)
                        }) {
                            Text("🐢 Slow")
                        }
                    }
                    Button(onClick = { showWordDialog = false }) {
                        Text("Close")
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
    tts: TtsManager,
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
                        tts.speak(option.chinese, speechSpeed)
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
    tts: TtsManager,
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
                .heightIn(min = 72.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50),
                modifier = Modifier.size(36.dp)
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
                    tts.speak(option.chinese, speechSpeed)
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