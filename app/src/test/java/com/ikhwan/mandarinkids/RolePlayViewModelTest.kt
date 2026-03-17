package com.ikhwan.mandarinkids

import com.ikhwan.mandarinkids.data.models.*
import org.junit.Assert.*
import org.junit.Test

class RolePlayViewModelTest {

    private fun makeVm(dialogues: List<DialogueStep> = emptyList()) =
        RolePlayViewModel(testScenario(dialogues = dialogues))

    // ── Initial state ─────────────────────────────────────────────────────

    @Test
    fun initialState_allDefaultsCorrect() {
        val vm = makeVm()
        assertEquals(0, vm.currentStepIndex)
        assertEquals("", vm.userName)
        assertFalse(vm.showNameInput)
        assertTrue(vm.conversationHistory.isEmpty())
        assertFalse(vm.showOptions)
        assertEquals(0, vm.correctAnswersCount)
        assertEquals(1.0f, vm.speechSpeed, 0.001f)
        assertFalse(vm.isProcessingStep)
    }

    @Test
    fun currentStep_noDialogues_isNull() {
        assertNull(makeVm().currentStep)
    }

    @Test
    fun currentStep_withDialogues_returnsFirstStep() {
        val step = testDialogueStep()
        val vm = makeVm(dialogues = listOf(step))
        assertEquals(step, vm.currentStep)
    }

    // ── advanceStep ───────────────────────────────────────────────────────

    @Test
    fun advanceStep_incrementsIndex() {
        val vm = makeVm(dialogues = listOf(testDialogueStep(id = 1), testDialogueStep(id = 2)))
        vm.advanceStep()
        assertEquals(1, vm.currentStepIndex)
    }

    @Test
    fun advanceStep_pastEnd_currentStepBecomesNull() {
        val vm = makeVm(dialogues = listOf(testDialogueStep()))
        vm.advanceStep()
        assertNull(vm.currentStep)
    }

    // ── processing flags ──────────────────────────────────────────────────

    @Test
    fun beginCharacterTurn_setsIsProcessingAndHidesUI() {
        val vm = makeVm()
        vm.revealOptions()
        vm.revealNameInput()
        vm.beginCharacterTurn()
        assertTrue(vm.isProcessingStep)
        assertFalse(vm.showOptions)
        assertFalse(vm.showNameInput)
    }

    @Test
    fun finishProcessing_clearsIsProcessing() {
        val vm = makeVm()
        vm.beginCharacterTurn()
        vm.finishProcessing()
        assertFalse(vm.isProcessingStep)
    }

    @Test
    fun revealOptions_setsShowOptions() {
        val vm = makeVm()
        vm.revealOptions()
        assertTrue(vm.showOptions)
    }

    @Test
    fun revealNameInput_setsShowNameInput() {
        val vm = makeVm()
        vm.revealNameInput()
        assertTrue(vm.showNameInput)
    }

    // ── conversation history ──────────────────────────────────────────────

    @Test
    fun addMessage_appendsToHistory() {
        val vm = makeVm()
        val msg = ConversationMessage(Speaker.CHARACTER, "你好", "nǐ hǎo", "Hello", "Halo")
        vm.addMessage(msg)
        assertEquals(1, vm.conversationHistory.size)
        assertEquals(msg, vm.conversationHistory.first())
    }

    @Test
    fun addMessage_multipleTimes_growsHistory() {
        val vm = makeVm()
        val msg = ConversationMessage(Speaker.CHARACTER, "你好", "nǐ hǎo", "Hello", "Halo")
        repeat(3) { vm.addMessage(msg) }
        assertEquals(3, vm.conversationHistory.size)
    }

    // ── selectOption ──────────────────────────────────────────────────────

    @Test
    fun selectOption_correctOption_incrementsScore() {
        val vm = makeVm()
        vm.selectOption(testResponseOption(isCorrect = true))
        assertEquals(1, vm.correctAnswersCount)
    }

    @Test
    fun selectOption_wrongOption_doesNotIncrementScore() {
        val vm = makeVm()
        vm.selectOption(testResponseOption(isCorrect = false))
        assertEquals(0, vm.correctAnswersCount)
    }

    @Test
    fun selectOption_hidesOptions() {
        val vm = makeVm()
        vm.revealOptions()
        vm.selectOption(testResponseOption())
        assertFalse(vm.showOptions)
    }

    @Test
    fun selectOption_addsStudentMessageToHistory() {
        val vm = makeVm()
        val option = testResponseOption(chinese = "你好", english = "Hello")
        vm.selectOption(option)
        assertEquals(1, vm.conversationHistory.size)
        val msg = vm.conversationHistory[0]
        assertEquals(Speaker.STUDENT, msg.speaker)
        assertEquals("你好", msg.textChinese)
        assertEquals("Hello", msg.textEnglish)
    }

    // ── speech speed ──────────────────────────────────────────────────────

    @Test
    fun toggleSpeechSpeed_from1x_goesTo07x() {
        val vm = makeVm()
        vm.toggleSpeechSpeed()
        assertEquals(0.7f, vm.speechSpeed, 0.001f)
    }

    @Test
    fun toggleSpeechSpeed_from07x_goesBackTo1x() {
        val vm = makeVm()
        vm.toggleSpeechSpeed()
        vm.toggleSpeechSpeed()
        assertEquals(1.0f, vm.speechSpeed, 0.001f)
    }

    @Test
    fun applyStoredSpeed_updatesSpeed() {
        val vm = makeVm()
        vm.applyStoredSpeed(0.7f)
        assertEquals(0.7f, vm.speechSpeed, 0.001f)
    }

    // ── submitName ────────────────────────────────────────────────────────

    @Test
    fun submitName_returnsFullChineseText() {
        val option = testResponseOption(chinese = "我叫", pinyin = "wǒ jiào", english = "My name is")
        val step = testDialogueStep(options = listOf(option), userNameInput = true)
        val vm = makeVm(dialogues = listOf(step))
        assertEquals("我叫Milton", vm.submitName("Milton"))
    }

    @Test
    fun submitName_setsUserNameAndHidesInput() {
        val step = testDialogueStep(options = listOf(testResponseOption()), userNameInput = true)
        val vm = makeVm(dialogues = listOf(step))
        vm.revealNameInput()
        vm.submitName("Milton")
        assertEquals("Milton", vm.userName)
        assertFalse(vm.showNameInput)
    }

    @Test
    fun submitName_addsStudentMessageWithFullChineseToHistory() {
        val option = testResponseOption(chinese = "我叫")
        val step = testDialogueStep(options = listOf(option))
        val vm = makeVm(dialogues = listOf(step))
        vm.submitName("Milton")
        assertEquals(1, vm.conversationHistory.size)
        assertEquals("我叫Milton", vm.conversationHistory[0].textChinese)
        assertEquals(Speaker.STUDENT, vm.conversationHistory[0].speaker)
    }

    @Test
    fun submitName_incrementsCorrectAnswersCount() {
        val step = testDialogueStep(options = listOf(testResponseOption()))
        val vm = makeVm(dialogues = listOf(step))
        vm.submitName("Milton")
        assertEquals(1, vm.correctAnswersCount)
    }

    @Test
    fun submitName_noCurrentStep_returnsEmptyString() {
        val vm = makeVm(dialogues = emptyList())
        assertEquals("", vm.submitName("Milton"))
    }

    @Test
    fun submitName_noCurrentStep_doesNotChangeHistory() {
        val vm = makeVm(dialogues = emptyList())
        vm.submitName("Milton")
        assertTrue(vm.conversationHistory.isEmpty())
    }
}
