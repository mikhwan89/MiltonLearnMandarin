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
    /** Words answered correctly in this session. */
    var correctCount by mutableStateOf(0)
        private set
    var totalStartCount by mutableStateOf(0)
        private set
    var showSummary by mutableStateOf(false)
        private set
    /** Increments every time we advance to a new card — use as a `remember` key in the UI. */
    var cardToken by mutableStateOf(0)
        private set

    /** Full deduplicated library of mastered words. */
    var allWords by mutableStateOf<List<MasteredWordEntity>>(emptyList())
        private set

    /** Distinct mastery levels (1-10) that exist in the library, sorted ascending. */
    var availableLevels by mutableStateOf<List<Int>>(emptyList())
        private set

    /** Currently selected mastery levels — words at these levels form the session deck. */
    var selectedLevels by mutableStateOf<Set<Int>>(emptySet())
        private set

    val currentWord: MasteredWordEntity?
        get() = if (deck.isNotEmpty() && currentIndex < deck.size) deck[currentIndex] else null

    init {
        viewModelScope.launch {
            val words = repository.getAllMasteredWords().first()
                .distinctBy { it.chinese }
            allWords = words

            val levels = words.map { it.boxLevel }.distinct().sorted()
            availableLevels = levels

            // Default: 3 lowest mastery levels
            val defaultLevels = levels.take(3).toSet()
            selectedLevels = defaultLevels
            buildDeck(defaultLevels, words)
            isLoading = false
        }
    }

    private fun buildDeck(levels: Set<Int>, source: List<MasteredWordEntity> = allWords) {
        val filtered = source.filter { it.boxLevel in levels }
        deck = filtered.shuffled()
        totalStartCount = filtered.size
        correctCount = 0
        currentIndex = 0
        cardToken++
        showSummary = false
    }

    /** Toggle a mastery level on/off. Always keeps at least one level selected. */
    fun toggleLevel(level: Int) {
        val newLevels = if (level in selectedLevels) selectedLevels - level else selectedLevels + level
        if (newLevels.isEmpty()) return
        selectedLevels = newLevels
        buildDeck(newLevels)
    }

    /** Card answered correctly — +1 mastery (max 10), retire from this session's deck. */
    fun markRemembered() {
        val word = deck[currentIndex]
        val newDeck = deck.toMutableList().also { it.removeAt(currentIndex) }
        correctCount++
        deck = newDeck
        cardToken++
        if (newDeck.isEmpty()) showSummary = true
        else currentIndex = if (currentIndex >= newDeck.size) 0 else currentIndex

        viewModelScope.launch {
            repository.markWordMastered(
                word.copy(boxLevel = (word.boxLevel + 1).coerceAtMost(10))
            )
        }
    }

    /** Card answered wrong — -1 mastery (min 1), move to back of queue. */
    fun markForgotten() {
        val word = deck[currentIndex]
        val newDeck = deck.toMutableList().also {
            it.removeAt(currentIndex)
            it.add(word)
        }
        deck = newDeck
        currentIndex = if (currentIndex >= newDeck.size) 0 else currentIndex
        cardToken++

        viewModelScope.launch {
            repository.markWordMastered(
                word.copy(boxLevel = (word.boxLevel - 1).coerceAtLeast(1))
            )
        }
    }

    fun finishEarly() { showSummary = true }

    companion object {
        fun factory(repository: ProgressRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PracticeSessionViewModel(repository) as T
        }
    }
}
