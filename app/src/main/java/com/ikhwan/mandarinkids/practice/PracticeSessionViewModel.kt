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
    /** True when the deck was built from due-today words rather than the full library. */
    var isDueSession by mutableStateOf(false)
        private set

    var allWords by mutableStateOf<List<MasteredWordEntity>>(emptyList())
        private set
    /** Increments every time we advance to a new card — use as a `remember` key in the UI. */
    var cardToken by mutableStateOf(0)
        private set
    /** Null = all words; non-null = filter to this scenarioId. */
    var activeFilter by mutableStateOf<String?>(null)
        private set

    val currentWord: MasteredWordEntity?
        get() = if (deck.isNotEmpty() && currentIndex < deck.size) deck[currentIndex] else null

    init {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val allWords = repository.getAllMasteredWords().first()
                .distinctBy { it.chinese }
            this@PracticeSessionViewModel.allWords = allWords

            val dueWords = allWords.filter { it.nextReviewDate <= now }

            val words = if (dueWords.isNotEmpty()) {
                isDueSession = true
                dueWords.shuffled()
            } else {
                allWords.shuffled()
            }

            deck = words
            totalStartCount = words.size
            isLoading = false
        }
    }

    fun flip() {
        isFlipped = !isFlipped
    }

    /** Card was remembered — promote its box level and retire for this session. */
    fun markRemembered() {
        val word = deck[currentIndex]
        val newDeck = deck.toMutableList().also { it.removeAt(currentIndex) }
        rememberedCount++
        deck = newDeck
        isFlipped = false
        cardToken++
        if (newDeck.isEmpty()) showSummary = true
        else currentIndex = if (currentIndex >= newDeck.size) 0 else currentIndex

        viewModelScope.launch {
            val newBox = (word.boxLevel + 1).coerceAtMost(5)
            repository.markWordMastered(
                word.copy(
                    boxLevel = newBox,
                    nextReviewDate = System.currentTimeMillis() + boxIntervalMs(newBox)
                )
            )
        }
    }

    /** Card was forgotten — reset to box 1, due immediately, move to back of queue. */
    fun markForgotten() {
        val word = deck[currentIndex]
        val newDeck = deck.toMutableList().also {
            it.removeAt(currentIndex)
            it.add(word)
        }
        deck = newDeck
        currentIndex = if (currentIndex >= newDeck.size) 0 else currentIndex
        isFlipped = false
        cardToken++

        viewModelScope.launch {
            repository.markWordMastered(
                word.copy(boxLevel = 1, nextReviewDate = 0L)
            )
        }
    }

    /** User chose to end the session early. */
    fun finishEarly() {
        showSummary = true
    }

    /** Rebuild the deck filtered to a specific scenario (null = all words). */
    fun setScenarioFilter(scenarioId: String?) {
        if (scenarioId == activeFilter) return
        activeFilter = scenarioId
        val filtered = if (scenarioId == null) allWords
                       else allWords.filter { it.scenarioId == scenarioId }
        val now = System.currentTimeMillis()
        val dueWords = filtered.filter { it.nextReviewDate <= now }
        val words = if (dueWords.isNotEmpty()) {
            isDueSession = true
            dueWords.shuffled()
        } else {
            isDueSession = false
            filtered.shuffled()
        }
        deck = words
        totalStartCount = words.size
        rememberedCount = 0
        currentIndex = 0
        cardToken++
        showSummary = false
    }

    companion object {
        /** Returns the review interval in milliseconds for the given box level. */
        fun boxIntervalMs(box: Int): Long {
            val days = when (box) {
                1 -> 1L
                2 -> 2L
                3 -> 4L
                4 -> 7L
                else -> 14L
            }
            return days * 24 * 60 * 60 * 1000L
        }

        fun factory(repository: ProgressRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PracticeSessionViewModel(repository) as T
        }
    }
}
