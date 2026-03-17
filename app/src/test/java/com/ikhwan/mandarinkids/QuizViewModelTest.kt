package com.ikhwan.mandarinkids

import org.junit.Assert.*
import org.junit.Test

class QuizViewModelTest {

    /** Build a VM with [numQuestions] questions, each having correctAnswerIndex = [correctIndex]. */
    private fun makeVm(numQuestions: Int = 3, correctIndex: Int = 1): QuizViewModel {
        val questions = (0 until numQuestions).map { testQuizQuestion(correctAnswerIndex = correctIndex) }
        return QuizViewModel(testScenario(quizQuestions = questions), rolePlayScore = 0)
    }

    // ── Initial state ─────────────────────────────────────────────────────

    @Test
    fun initialState_allDefaultsCorrect() {
        val vm = makeVm()
        assertEquals(0, vm.currentQuestionIndex)
        assertNull(vm.selectedAnswerIndex)
        assertFalse(vm.showFeedback)
        assertEquals(0, vm.correctAnswersCount)
        assertFalse(vm.showResults)
    }

    @Test
    fun currentQuestion_initiallyReturnsFirstQuestion() {
        val vm = makeVm()
        assertNotNull(vm.currentQuestion)
        assertEquals(vm.scenario.quizQuestions[0], vm.currentQuestion)
    }

    // ── selectAnswer ──────────────────────────────────────────────────────

    @Test
    fun selectAnswer_correctIndex_incrementsScore() {
        val vm = makeVm(correctIndex = 1)
        vm.selectAnswer(1)
        assertEquals(1, vm.correctAnswersCount)
    }

    @Test
    fun selectAnswer_wrongIndex_doesNotIncrementScore() {
        val vm = makeVm(correctIndex = 1)
        vm.selectAnswer(0)
        assertEquals(0, vm.correctAnswersCount)
    }

    @Test
    fun selectAnswer_setsShowFeedbackTrue() {
        val vm = makeVm()
        vm.selectAnswer(0)
        assertTrue(vm.showFeedback)
    }

    @Test
    fun selectAnswer_recordsSelectedIndex() {
        val vm = makeVm()
        vm.selectAnswer(2)
        assertEquals(2, vm.selectedAnswerIndex)
    }

    @Test
    fun selectAnswer_whenFeedbackAlreadyShown_isIgnored() {
        // First call: wrong answer
        val vm = makeVm(correctIndex = 0)
        vm.selectAnswer(1)
        assertEquals(0, vm.correctAnswersCount)
        assertEquals(1, vm.selectedAnswerIndex)

        // Second call: correct answer — must be ignored
        vm.selectAnswer(0)
        assertEquals(0, vm.correctAnswersCount)  // score still 0
        assertEquals(1, vm.selectedAnswerIndex)  // selection unchanged
    }

    // ── advanceQuestion ───────────────────────────────────────────────────

    @Test
    fun advanceQuestion_midGame_incrementsQuestionIndex() {
        val vm = makeVm(numQuestions = 3)
        vm.selectAnswer(0)
        vm.advanceQuestion()
        assertEquals(1, vm.currentQuestionIndex)
    }

    @Test
    fun advanceQuestion_midGame_resetsFeedbackAndSelection() {
        val vm = makeVm(numQuestions = 3)
        vm.selectAnswer(0)
        vm.advanceQuestion()
        assertFalse(vm.showFeedback)
        assertNull(vm.selectedAnswerIndex)
    }

    @Test
    fun advanceQuestion_onLastQuestion_setsShowResults() {
        val vm = makeVm(numQuestions = 1)
        vm.selectAnswer(0)
        vm.advanceQuestion()
        assertTrue(vm.showResults)
    }

    @Test
    fun advanceQuestion_onLastQuestion_doesNotIncrementIndex() {
        // index should stay at 0 when showResults fires — no off-by-one
        val vm = makeVm(numQuestions = 1)
        vm.selectAnswer(0)
        vm.advanceQuestion()
        assertEquals(0, vm.currentQuestionIndex)
    }

    @Test
    fun advanceQuestion_throughAllQuestions_showsResults() {
        val vm = makeVm(numQuestions = 3)
        repeat(3) {
            vm.selectAnswer(0)
            vm.advanceQuestion()
        }
        assertTrue(vm.showResults)
    }

    @Test
    fun advanceQuestion_throughAllCorrect_talliesFullScore() {
        val vm = makeVm(numQuestions = 3, correctIndex = 0)
        repeat(3) {
            vm.selectAnswer(0)
            vm.advanceQuestion()
        }
        assertEquals(3, vm.correctAnswersCount)
    }
}
