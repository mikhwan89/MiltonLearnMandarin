package com.ikhwan.mandarinkids.data.models

// Core scenario data model
data class Scenario(
    val id: String,
    val title: String,
    val description: String,
    val characterName: String,
    val characterEmoji: String,
    val characterRole: String,
    val dialogues: List<DialogueStep>,
    val quizQuestions: List<QuizQuestion>
)

// Word breakdown for pinyin learning
data class PinyinWord(
    val pinyin: String,
    val chinese: String,
    val english: String,
    val indonesian: String
)

// Individual dialogue step in scenario
data class DialogueStep(
    val id: Int,
    val speaker: Speaker,
    val textChinese: String,
    val textPinyin: String,
    val textEnglish: String,
    val textIndonesian: String,
    val pinyinWords: List<PinyinWord> = emptyList(),
    val responseType: ResponseType,
    val options: List<ResponseOption> = emptyList(),
    val userNameInput: Boolean = false
)

// Who is speaking
enum class Speaker {
    CHARACTER,  // Teacher, friend, etc.
    STUDENT     // The learner
}

// Type of response expected
enum class ResponseType {
    SINGLE_CHOICE,      // One correct answer
    TEXT_INPUT,         // Type a response
    MULTIPLE_OPTIONS,   // Multiple valid choices
    LISTEN_ONLY        // Just listen, auto-advance
}

// Response option in dialogue
data class ResponseOption(
    val chinese: String,
    val pinyin: String,
    val english: String,
    val indonesian: String,
    val pinyinWords: List<PinyinWord> = emptyList(),
    val isCorrect: Boolean = true
)

// Quiz direction determines display format
enum class QuizDirection {
    CHINESE_TO_TRANSLATION,  // Question in Chinese → Answer in English/Indonesian
    TRANSLATION_TO_CHINESE   // Question in English/Indonesian → Answer in Chinese
}

// Quiz question model
data class QuizQuestion(
    val direction: QuizDirection,
    val questionText: String,
    val questionChinese: String = "",
    val questionPinyin: String = "",
    val options: List<QuizOption>,
    val correctAnswerIndex: Int,
    val explanation: String
)

// Quiz option model
data class QuizOption(
    val chinese: String,
    val pinyin: String,
    val translation: String
)