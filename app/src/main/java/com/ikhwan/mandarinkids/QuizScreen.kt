package com.ikhwan.mandarinkids

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.ikhwan.mandarinkids.data.models.*
import com.ikhwan.mandarinkids.tts.TtsManager
import com.ikhwan.mandarinkids.tts.rememberTtsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    scenario: Scenario,
    rolePlayScore: Int,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    onTryAgain: (() -> Unit)? = null  // NEW: Callback for try again
) {
    val context = LocalContext.current
    val tts = rememberTtsManager()
    val coroutineScope = rememberCoroutineScope()

    val vm: QuizViewModel = viewModel(
        key = "${scenario.id}_$rolePlayScore",
        factory = QuizViewModel.factory(scenario, rolePlayScore)
    )

    val currentQuestion = vm.currentQuestion

    // Auto-play TTS when an audio-mode question loads
    LaunchedEffect(vm.currentQuestionIndex) {
        val q = vm.currentQuestion
        if (q != null && q.direction == QuizDirection.AUDIO_TO_TRANSLATION) {
            delay(300)
            tts.speak(q.questionChinese)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Quiz Time! 测验", fontSize = 16.sp)
                        Text(
                            "Question ${vm.currentQuestionIndex + 1}/${vm.scenario.quizQuestions.size}",
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
        if (vm.showResults) {
            QuizResultsScreen(
                scenario = vm.scenario,
                rolePlayScore = vm.rolePlayScore,
                quizScore = vm.correctAnswersCount,
                totalQuestions = vm.scenario.quizQuestions.size,
                onComplete = onComplete,
                onTryAgain = onTryAgain  // Pass through the callback
            )
        } else if (currentQuestion != null) {
            val question = currentQuestion
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LinearProgressIndicator(
                    progress = { (vm.currentQuestionIndex + 1).toFloat() / vm.scenario.quizQuestions.size },
                    modifier = Modifier.fillMaxWidth(),
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Question ${vm.currentQuestionIndex + 1}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Display question based on direction
                                when (question.direction) {
                                    QuizDirection.CHINESE_TO_TRANSLATION -> {
                                        // Show Chinese + Pinyin
                                        Text(
                                            text = question.questionChinese,
                                            fontSize = 28.sp,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = question.questionPinyin,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = question.questionText,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    QuizDirection.TRANSLATION_TO_CHINESE -> {
                                        // Show English question only
                                        Text(
                                            text = question.questionText,
                                            fontSize = 20.sp,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                    QuizDirection.AUDIO_TO_TRANSLATION -> {
                                        // Show speaker UI — no text hint
                                        Text(
                                            text = "🎧 Listen and choose!",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                        Button(
                                            onClick = { tts.speak(question.questionChinese) },
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Play again", fontSize = 15.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Select your answer:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Answer options
                    items(question.options.size) { index ->
                        QuizOptionButton(
                            option = question.options[index],
                            index = index,
                            isSelected = vm.selectedAnswerIndex == index,
                            isCorrect = index == question.correctAnswerIndex,
                            showFeedback = vm.showFeedback,
                            onClick = {
                                if (!vm.showFeedback) {
                                    vm.selectAnswer(index)
                                    if (index == question.correctAnswerIndex) playSuccessSound()
                                    else playWrongSound()
                                    coroutineScope.launch {
                                        delay(3000)
                                        vm.advanceQuestion()
                                    }
                                }
                            },
                            tts = tts,
                            direction = question.direction  // NEW: Pass direction
                        )
                    }

                    if (vm.showFeedback) {
                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically()
                            ) {
                                FeedbackCard(
                                    isCorrect = vm.selectedAnswerIndex == question.correctAnswerIndex,
                                    explanation = question.explanation
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // No quiz questions available for this scenario
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("🎉", fontSize = 64.sp)
                    Text(
                        "No quiz for this scenario yet!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onComplete) {
                        Text("Back to Home")
                    }
                }
            }
        }
    }
}

@Composable
fun QuizOptionButton(
    option: QuizOption,
    index: Int,
    isSelected: Boolean,
    isCorrect: Boolean,
    showFeedback: Boolean,
    onClick: () -> Unit,
    tts: TtsManager,
    direction: QuizDirection  // NEW: Pass direction
) {
    val backgroundColor = when {
        !showFeedback -> MaterialTheme.colorScheme.surface
        isSelected && isCorrect -> Color(0xFF4CAF50)
        isSelected && !isCorrect -> Color(0xFFF44336)
        !isSelected && isCorrect -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        showFeedback && (isCorrect || isSelected) -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    val isCorrectAndSelected = showFeedback && isSelected && isCorrect
    val popScale by animateFloatAsState(
        targetValue = if (isCorrectAndSelected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "correctPop"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = popScale; scaleY = popScale },
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        enabled = !showFeedback
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (showFeedback && (isCorrect || isSelected))
                    Color.White.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = (index + 1).toString(),
                        fontSize = 18.sp,
                        color = if (showFeedback && (isCorrect || isSelected))
                            Color.White
                        else
                            MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                when (direction) {
                    QuizDirection.TRANSLATION_TO_CHINESE -> {
                        // Show Chinese + Pinyin
                        Text(
                            text = option.chinese,
                            fontSize = 20.sp,
                            style = MaterialTheme.typography.titleLarge,
                            color = contentColor
                        )
                        Text(
                            text = option.pinyin,
                            fontSize = 14.sp,
                            color = if (showFeedback && (isCorrect || isSelected))
                                Color.White.copy(alpha = 0.8f)
                            else
                                MaterialTheme.colorScheme.secondary
                        )
                    }
                    QuizDirection.CHINESE_TO_TRANSLATION,
                    QuizDirection.AUDIO_TO_TRANSLATION -> {
                        // Show English/Indonesian translation only
                        Text(
                            text = option.translation,
                            fontSize = 18.sp,
                            style = MaterialTheme.typography.titleLarge,
                            color = contentColor
                        )
                    }
                }
            }

            // Play audio button (only for TRANSLATION_TO_CHINESE)
            if (direction == QuizDirection.TRANSLATION_TO_CHINESE) {
                IconButton(
                    onClick = {
                        tts.speak(option.chinese)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (showFeedback) {
                if (isCorrect) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Correct",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                } else if (isSelected) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Wrong",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

