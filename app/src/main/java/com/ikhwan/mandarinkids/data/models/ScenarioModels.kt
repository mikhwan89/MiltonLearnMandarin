package com.ikhwan.mandarinkids.data.models

import kotlinx.serialization.Serializable

// Category grouping for the Home screen
@Serializable
enum class ScenarioCategory(val displayName: String, val emoji: String) {
    ESSENTIALS("Essentials", "👋"),
    AT_SCHOOL("At School", "🏫"),
    SCHOOL_SUBJECTS("School Subjects", "📖"),
    FOOD_AND_EATING("Food & Eating", "🍎"),
    FEELINGS_AND_HEALTH("Feelings & Health", "💗"),
    PLAY_AND_HOBBIES("Play & Hobbies", "⚽"),
    HOME("At Home", "🏠"),
    OUT_AND_ABOUT("Out & About", "🚌")
}

// Core scenario data model
@Serializable
data class Scenario(
    val id: String,
    val title: String,
    val description: String,
    val characterName: String,
    val characterEmoji: String,
    val characterRole: String,
    val category: ScenarioCategory = ScenarioCategory.ESSENTIALS,
    val dialogues: List<DialogueStep>,
    val quizQuestions: List<QuizQuestion>
)

// Word breakdown for pinyin learning
@Serializable
data class PinyinWord(
    val pinyin: String,
    val chinese: String,
    val english: String,
    val indonesian: String,
    val note: String? = null   // Child-friendly explanation shown in flashcard/dialog
)

// Individual dialogue step in scenario
@Serializable
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
@Serializable
enum class Speaker {
    CHARACTER,  // Teacher, friend, etc.
    STUDENT     // The learner
}

// Type of response expected
@Serializable
enum class ResponseType {
    SINGLE_CHOICE,      // One correct answer
    TEXT_INPUT,         // Type a response
    MULTIPLE_OPTIONS,   // Multiple valid choices
    LISTEN_ONLY        // Just listen, auto-advance
}

// Response option in dialogue
@Serializable
data class ResponseOption(
    val chinese: String,
    val pinyin: String,
    val english: String,
    val indonesian: String,
    val pinyinWords: List<PinyinWord> = emptyList(),
    val isCorrect: Boolean = true
)

// Quiz direction determines display format
@Serializable
enum class QuizDirection {
    CHINESE_TO_TRANSLATION,  // Question in Chinese → Answer in English/Indonesian
    TRANSLATION_TO_CHINESE,  // Question in English/Indonesian → Answer in Chinese
    AUDIO_TO_TRANSLATION     // TTS plays the word → Answer in English/Indonesian (ear training)
}

// Quiz question model
@Serializable
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
@Serializable
data class QuizOption(
    val chinese: String,
    val pinyin: String,
    val translation: String
)

// A single message in the role-play conversation history
data class ConversationMessage(
    val speaker: Speaker,
    val textChinese: String,
    val textPinyin: String,
    val textEnglish: String,
    val textIndonesian: String,
    val pinyinWords: List<PinyinWord> = emptyList()
)