package com.ikhwan.mandarinkids

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ikhwan.mandarinkids.data.models.*

class RolePlayViewModel(val scenario: Scenario) : ViewModel() {

    var currentStepIndex by mutableStateOf(0)
        private set
    var userName by mutableStateOf("")
        private set
    var showNameInput by mutableStateOf(false)
        private set
    var conversationHistory by mutableStateOf<List<ConversationMessage>>(emptyList())
        private set
    var showOptions by mutableStateOf(false)
        private set
    var correctAnswersCount by mutableStateOf(0)
        private set
    var speechSpeed by mutableStateOf(1.0f)
        private set
    var isProcessingStep by mutableStateOf(false)
        private set

    val currentStep: DialogueStep?
        get() = if (currentStepIndex < scenario.dialogues.size) scenario.dialogues[currentStepIndex] else null

    fun applyStoredSpeed(speed: Float) {
        speechSpeed = speed
    }

    fun toggleSpeechSpeed() {
        speechSpeed = if (speechSpeed == 1.0f) 0.7f else 1.0f
    }

    fun beginCharacterTurn() {
        isProcessingStep = true
        showOptions = false
        showNameInput = false
    }

    fun finishProcessing() {
        isProcessingStep = false
    }

    fun addMessage(message: ConversationMessage) {
        conversationHistory = conversationHistory + message
    }

    fun revealOptions() {
        showOptions = true
    }

    fun revealNameInput() {
        showNameInput = true
    }

    fun advanceStep() {
        currentStepIndex++
    }

    fun selectOption(option: ResponseOption) {
        showOptions = false
        conversationHistory = conversationHistory + ConversationMessage(
            speaker = Speaker.STUDENT,
            textChinese = option.chinese,
            textPinyin = option.pinyin,
            textEnglish = option.english,
            textIndonesian = option.indonesian,
            pinyinWords = option.pinyinWords
        )
        if (option.isCorrect) {
            correctAnswersCount++
        }
    }

    /** Adds the student's name response to history and returns the full Chinese text for TTS. */
    fun submitName(name: String): String {
        val step = currentStep ?: return ""
        val option = step.options.first()
        userName = name
        showNameInput = false
        val fullChinese = "${option.chinese}$name"
        conversationHistory = conversationHistory + ConversationMessage(
            speaker = Speaker.STUDENT,
            textChinese = fullChinese,
            textPinyin = "${option.pinyin} $name",
            textEnglish = "${option.english} $name",
            textIndonesian = "${option.indonesian} $name",
            pinyinWords = option.pinyinWords
        )
        correctAnswersCount++
        return fullChinese
    }

    companion object {
        fun factory(scenario: Scenario) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RolePlayViewModel(scenario) as T
        }
    }
}
