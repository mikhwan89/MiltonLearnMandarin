package com.ikhwan.mandarinkids

import com.ikhwan.mandarinkids.data.models.*

// Minimal builders for constructing test data without touching production code.

fun testScenario(
    id: String = "test_scenario",
    dialogues: List<DialogueStep> = emptyList(),
    quizQuestions: List<QuizQuestion> = emptyList()
) = Scenario(
    id = id,
    title = "Test Scenario",
    description = "A test scenario",
    characterName = "老师",
    characterEmoji = "👩‍🏫",
    characterRole = "Teacher",
    dialogues = dialogues,
    quizQuestions = quizQuestions
)

fun testDialogueStep(
    id: Int = 1,
    speaker: Speaker = Speaker.CHARACTER,
    responseType: ResponseType = ResponseType.LISTEN_ONLY,
    options: List<ResponseOption> = emptyList(),
    userNameInput: Boolean = false
) = DialogueStep(
    id = id,
    speaker = speaker,
    textChinese = "你好",
    textPinyin = "nǐ hǎo",
    textEnglish = "Hello",
    textIndonesian = "Halo",
    responseType = responseType,
    options = options,
    userNameInput = userNameInput
)

fun testResponseOption(
    chinese: String = "你好",
    pinyin: String = "nǐ hǎo",
    english: String = "Hello",
    isCorrect: Boolean = true
) = ResponseOption(
    chinese = chinese,
    pinyin = pinyin,
    english = english,
    indonesian = "Halo",
    isCorrect = isCorrect
)

fun testQuizQuestion(correctAnswerIndex: Int = 0) = QuizQuestion(
    direction = QuizDirection.CHINESE_TO_TRANSLATION,
    questionText = "What does 你好 mean?",
    options = listOf(
        QuizOption(chinese = "你好", pinyin = "nǐ hǎo", translation = "Hello"),
        QuizOption(chinese = "再见", pinyin = "zài jiàn", translation = "Goodbye"),
        QuizOption(chinese = "谢谢", pinyin = "xiè xie", translation = "Thank you")
    ),
    correctAnswerIndex = correctAnswerIndex,
    explanation = "你好 means Hello"
)
