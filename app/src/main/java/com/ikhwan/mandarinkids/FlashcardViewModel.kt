package com.ikhwan.mandarinkids

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ikhwan.mandarinkids.data.models.PinyinWord
import com.ikhwan.mandarinkids.data.models.Scenario

class FlashcardViewModel(scenario: Scenario) : ViewModel() {

    val allWords: List<PinyinWord> = scenario.getFlashcardWords()
    var deck by mutableStateOf(allWords)
        private set
    var currentIndex by mutableStateOf(0)
        private set
    var isFlipped by mutableStateOf(false)
        private set
    var masteredCount by mutableStateOf(0)
        private set
    var showComplete by mutableStateOf(false)
        private set

    val currentWord get() = if (deck.isNotEmpty() && currentIndex < deck.size) deck[currentIndex] else null

    fun flip() {
        isFlipped = !isFlipped
    }

    fun resetFlipped() {
        isFlipped = false
    }

    fun markStillLearning() {
        val word = deck[currentIndex]
        val newDeck = deck.toMutableList().also {
            it.removeAt(currentIndex)
            it.add(word)
        }
        deck = newDeck
        currentIndex = if (currentIndex >= newDeck.size) 0 else currentIndex
        isFlipped = false
    }

    fun markMastered() {
        val newDeck = deck.toMutableList().also { it.removeAt(currentIndex) }
        masteredCount++
        deck = newDeck
        if (newDeck.isEmpty()) {
            showComplete = true
        } else {
            currentIndex = if (currentIndex >= newDeck.size) 0 else currentIndex
        }
        isFlipped = false
    }

    companion object {
        fun factory(scenario: Scenario) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                FlashcardViewModel(scenario) as T
        }
    }
}
