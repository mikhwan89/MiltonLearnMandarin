package com.ikhwan.mandarinkids

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ikhwan.mandarinkids.data.models.PinyinWord
import com.ikhwan.mandarinkids.data.models.Scenario
import com.ikhwan.mandarinkids.db.MasteredWordEntity
import com.ikhwan.mandarinkids.db.ProgressRepository
import kotlinx.coroutines.launch

class FlashcardViewModel(
    scenario: Scenario,
    private val repository: ProgressRepository
) : ViewModel() {

    private val scenarioId = scenario.id

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

    init {
        // Seed all scenario words on open so they appear in practice even if every card is skipped.
        viewModelScope.launch {
            val seedWords = allWords.map { pw ->
                MasteredWordEntity(
                    scenarioId = scenarioId,
                    chinese = pw.chinese,
                    pinyin = pw.pinyin,
                    english = pw.english,
                    indonesian = pw.indonesian,
                    note = pw.note,
                    boxLevel = 1,
                    nextReviewDate = 0L
                )
            }
            repository.seedWordsForScenario(scenarioId, seedWords)
        }
    }

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
        val word = deck[currentIndex]
        val newDeck = deck.toMutableList().also { it.removeAt(currentIndex) }
        masteredCount++
        deck = newDeck
        if (newDeck.isEmpty()) {
            showComplete = true
        } else {
            currentIndex = if (currentIndex >= newDeck.size) 0 else currentIndex
        }
        isFlipped = false

        // Persist so the practice mode can access this word across sessions
        viewModelScope.launch {
            repository.markWordMastered(
                MasteredWordEntity(
                    scenarioId = scenarioId,
                    chinese = word.chinese,
                    pinyin = word.pinyin,
                    english = word.english,
                    indonesian = word.indonesian,
                    note = word.note
                )
            )
        }
    }

    companion object {
        fun factory(scenario: Scenario, repository: ProgressRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    FlashcardViewModel(scenario, repository) as T
            }
    }
}
