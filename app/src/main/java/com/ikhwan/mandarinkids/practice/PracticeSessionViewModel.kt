package com.ikhwan.mandarinkids.practice

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ikhwan.mandarinkids.db.MasteredWordEntity
import com.ikhwan.mandarinkids.db.ProgressRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PracticeSessionViewModel(private val repository: ProgressRepository) : ViewModel() {

    var isLoading by mutableStateOf(true)
        private set
    var deck by mutableStateOf<List<MasteredWordEntity>>(emptyList())
        private set
    var currentIndex by mutableStateOf(0)
        private set
    var isFlipped by mutableStateOf(false)
        private set
    var rememberedCount by mutableStateOf(0)
        private set
    var totalStartCount by mutableStateOf(0)
        private set
    var showSummary by mutableStateOf(false)
        private set

    val currentWord: MasteredWordEntity?
        get() = if (deck.isNotEmpty() && currentIndex < deck.size) deck[currentIndex] else null

    init {
        viewModelScope.launch {
            // Take a one-shot snapshot; deduplicate by Chinese character across scenarios
            val words = repository.getAllMasteredWords().first()
                .distinctBy { it.chinese }
                .shuffled()
            deck = words
            totalStartCount = words.size
            isLoading = false
        }
    }

    fun flip() {
        isFlipped = !isFlipped
    }

    /** Card was remembered — retire it for this session. */
    fun markRemembered() {
        val newDeck = deck.toMutableList().also { it.removeAt(currentIndex) }
        rememberedCount++
        deck = newDeck
        isFlipped = false
        if (newDeck.isEmpty()) {
            showSummary = true
        } else {
            currentIndex = if (currentIndex >= newDeck.size) 0 else currentIndex
        }
    }

    /** Card was forgotten — move it to the back of the queue. */
    fun markForgotten() {
        val word = deck[currentIndex]
        val newDeck = deck.toMutableList().also {
            it.removeAt(currentIndex)
            it.add(word)
        }
        deck = newDeck
        currentIndex = if (currentIndex >= newDeck.size) 0 else currentIndex
        isFlipped = false
    }

    /** User chose to end the session early. */
    fun finishEarly() {
        showSummary = true
    }

    companion object {
        fun factory(repository: ProgressRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PracticeSessionViewModel(repository) as T
        }
    }
}
