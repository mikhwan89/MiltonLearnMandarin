package com.ikhwan.mandarinkids.practice

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ikhwan.mandarinkids.data.scenarios.ScenarioRepository
import com.ikhwan.mandarinkids.db.MasteredWordEntity
import com.ikhwan.mandarinkids.db.PracticeType
import com.ikhwan.mandarinkids.db.ProgressRepository
import com.ikhwan.mandarinkids.getFlashcardWords
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PracticeSessionViewModel(
    private val repository: ProgressRepository,
    private val scenarioRepository: ScenarioRepository,
    val practiceType: PracticeType
) : ViewModel() {

    var isLoading by mutableStateOf(true)
        private set
    /** Increments every time a new card is shown — use as `remember` key in the UI. */
    var cardToken by mutableStateOf(0)
        private set

    /** Full deduplicated word library for this practice type with real-time boxLevel values. */
    var allWords by mutableStateOf<List<MasteredWordEntity>>(emptyList())
        private set

    /** The card currently on screen. Null when the active mode has no eligible words. */
    var currentWord by mutableStateOf<MasteredWordEntity?>(null)
        private set

    var practiceMode by mutableStateOf(PracticeMode.ALL)
        private set

    var correctCount by mutableStateOf(0)
        private set
    var totalAnswered by mutableStateOf(0)
        private set
    var promotedCount by mutableStateOf(0)
        private set
    var demotedCount by mutableStateOf(0)
        private set

    var showSummary by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            // Seed words for every scenario the student has played (stars >= 1).
            // Seeds all 3 practice types via insertIgnore, so existing progress is preserved.
            val completedIds = repository.getAllProgress().first()
                .filter { it.stars >= 1 }
                .map { it.scenarioId }
            for (scenarioId in completedIds) {
                val scenario = scenarioRepository.getById(scenarioId) ?: continue
                val seedWords = scenario.getFlashcardWords().map { pw ->
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

            // Ensure LISTENING and READING always have every word that DEFAULT has
            // (catches words added via migration or the per-scenario flashcard screen)
            repository.syncPracticeTypesFromDefault()

            // Load only words for this practice type
            val words = repository.getAllMasteredWords(practiceType).first()
                .distinctBy { it.chinese }
            allWords = words
            currentWord = pickNextWord()
            isLoading = false
        }
    }

    /** Words eligible for the current practice mode. */
    private fun poolForMode(words: List<MasteredWordEntity> = allWords): List<MasteredWordEntity> {
        val levels = words.map { it.boxLevel }.distinct().sorted()
        return when (practiceMode) {
            PracticeMode.ALL -> words
            PracticeMode.WEAK -> {
                val weakLevels = levels.take(3).toSet()
                words.filter { it.boxLevel in weakLevels }
            }
            PracticeMode.MASTERY -> {
                val masteryLevels = levels.takeLast(3).filter { it >= 4 }.toSet()
                words.filter { it.boxLevel in masteryLevels }
            }
        }
    }

    /** The 3 lowest mastery levels currently present in the library. */
    val weakLevels: Set<Int>
        get() {
            val levels = allWords.map { it.boxLevel }.distinct().sorted()
            return levels.take(3).toSet()
        }

    /** The 3 highest mastery levels that are >= 4, or empty if none qualify. */
    val masteryLevels: Set<Int>
        get() {
            val levels = allWords.map { it.boxLevel }.distinct().sorted()
            return levels.takeLast(3).filter { it >= 4 }.toSet()
        }

    /**
     * Weight for weighted-random selection.
     * Level 1 → 16, level 2 → 8, level 3 → 4, level 4 → 2, level 5-10 → 1.
     */
    private fun weightForLevel(level: Int): Int = maxOf(1, 16 shr (level - 1))

    private fun pickNextWord(exclude: MasteredWordEntity? = null): MasteredWordEntity? {
        val pool = poolForMode().let { p ->
            if (exclude != null && p.size > 1) p.filter { it.chinese != exclude.chinese } else p
        }
        if (pool.isEmpty()) return null

        val weights = pool.map { weightForLevel(it.boxLevel) }
        val total = weights.sum()
        var pick = (0 until total).random()
        for (i in pool.indices) {
            pick -= weights[i]
            if (pick < 0) return pool[i]
        }
        return pool.last()
    }

    fun setMode(mode: PracticeMode) {
        practiceMode = mode
        currentWord = pickNextWord()
        cardToken++
    }

    fun markRemembered() {
        val word = currentWord ?: return
        val newLevel = (word.boxLevel + 1).coerceAtMost(10)
        val updatedWord = word.copy(boxLevel = newLevel)

        allWords = allWords.map { if (it.chinese == word.chinese) updatedWord else it }
        correctCount++
        totalAnswered++
        if (newLevel > word.boxLevel) promotedCount++

        currentWord = pickNextWord(exclude = updatedWord)
        cardToken++

        viewModelScope.launch { repository.markWordMastered(updatedWord) }
    }

    fun markForgotten() {
        val word = currentWord ?: return
        val newLevel = (word.boxLevel - 1).coerceAtLeast(1)
        val updatedWord = word.copy(boxLevel = newLevel)

        allWords = allWords.map { if (it.chinese == word.chinese) updatedWord else it }
        totalAnswered++
        if (newLevel < word.boxLevel) demotedCount++

        currentWord = pickNextWord(exclude = updatedWord)
        cardToken++

        viewModelScope.launch { repository.markWordMastered(updatedWord) }
    }

    fun finishEarly() { showSummary = true }

    companion object {
        fun factory(
            repository: ProgressRepository,
            scenarioRepository: ScenarioRepository,
            practiceType: PracticeType
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PracticeSessionViewModel(repository, scenarioRepository, practiceType) as T
        }
    }
}
