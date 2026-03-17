package com.ikhwan.mandarinkids

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ikhwan.mandarinkids.data.models.*
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.preferences.UserPreferencesRepository
import com.ikhwan.mandarinkids.tts.TtsManager
import com.ikhwan.mandarinkids.tts.rememberTtsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val globalSpeed by userPrefs.speechRate.collectAsState(initial = 1.0f)
    val repo = remember { ProgressRepository.getInstance(context) }
    LaunchedEffect(scenario.id) {
        val override = repo.getSpeechRateForScenario(scenario.id)
        vm.applyStoredSpeed(override ?: globalSpeed)
    }

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
                            coroutineScope.launch { repo.saveSpeechRateForScenario(scenario.id, vm.speechSpeed) }
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