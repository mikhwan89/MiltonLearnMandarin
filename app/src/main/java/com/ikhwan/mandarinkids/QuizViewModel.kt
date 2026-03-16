package com.ikhwan.mandarinkids

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ikhwan.mandarinkids.data.models.Scenario

class QuizViewModel(val scenario: Scenario, val rolePlayScore: Int) : ViewModel() {

    var currentQuestionIndex by mutableStateOf(0)
        private set
    var selectedAnswerIndex by mutableStateOf<Int?>(null)
        private set
    var showFeedback by mutableStateOf(false)
        private set
    var correctAnswersCount by mutableStateOf(0)
        private set
    var showResults by mutableStateOf(false)
        private set

    val currentQuestion get() =
        if (currentQuestionIndex < scenario.quizQuestions.size)
            scenario.quizQuestions[currentQuestionIndex]
        else null

    fun selectAnswer(index: Int) {
        if (showFeedback) return
        selectedAnswerIndex = index
        showFeedback = true
        if (index == currentQuestion?.correctAnswerIndex) {
            correctAnswersCount++
        }
    }

    fun advanceQuestion() {
        if (currentQuestionIndex + 1 >= scenario.quizQuestions.size) {
            showResults = true
        } else {
            currentQuestionIndex++
            selectedAnswerIndex = null
            showFeedback = false
        }
    }

    companion object {
        fun factory(scenario: Scenario, rolePlayScore: Int) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                QuizViewModel(scenario, rolePlayScore) as T
        }
    }
}
