package com.ikhwan.mandarinkids

import com.ikhwan.mandarinkids.data.models.ResponseType
import com.ikhwan.mandarinkids.data.models.Speaker
import com.ikhwan.mandarinkids.data.scenarios.JsonScenarioRepository
import org.junit.Assert.*
import org.junit.Test

class ScenarioValidationTest {

    private val scenarios = JsonScenarioRepository.getAll()

    @Test
    fun allScenariosHaveAtLeastThreeQuizQuestions() {
        scenarios.forEach { scenario ->
            assertTrue(
                "Scenario '${scenario.id}' has only ${scenario.quizQuestions.size} quiz questions (min 3 required)",
                scenario.quizQuestions.size >= 3
            )
        }
    }

    @Test
    fun allQuizQuestionsHaveValidCorrectAnswerIndex() {
        scenarios.forEach { scenario ->
            scenario.quizQuestions.forEachIndexed { qIndex, question ->
                assertTrue(
                    "Scenario '${scenario.id}' question $qIndex: correctAnswerIndex ${question.correctAnswerIndex} is out of bounds (options size: ${question.options.size})",
                    question.correctAnswerIndex >= 0 && question.correctAnswerIndex < question.options.size
                )
            }
        }
    }

    @Test
    fun noStudentListenOnlySteps() {
        scenarios.forEach { scenario ->
            scenario.dialogues.forEachIndexed { stepIndex, step ->
                assertFalse(
                    "Scenario '${scenario.id}' step $stepIndex: STUDENT + LISTEN_ONLY combination silently freezes RolePlayScreen",
                    step.speaker == Speaker.STUDENT && step.responseType == ResponseType.LISTEN_ONLY
                )
            }
        }
    }

    @Test
    fun allInteractiveStepsHaveOptions() {
        scenarios.forEach { scenario ->
            scenario.dialogues.forEachIndexed { stepIndex, step ->
                if (step.responseType != ResponseType.LISTEN_ONLY) {
                    assertTrue(
                        "Scenario '${scenario.id}' step $stepIndex: interactive step (responseType=${step.responseType}) has no options",
                        step.options.isNotEmpty()
                    )
                }
            }
        }
    }

    @Test
    fun allPinyinWordsHaveNonBlankFields() {
        scenarios.forEach { scenario ->
            // Check dialogue step pinyinWords
            scenario.dialogues.forEachIndexed { stepIndex, step ->
                step.pinyinWords.forEachIndexed { wordIndex, word ->
                    assertFalse(
                        "Scenario '${scenario.id}' step $stepIndex pinyinWord[$wordIndex]: chinese is blank",
                        word.chinese.isBlank()
                    )
                    assertFalse(
                        "Scenario '${scenario.id}' step $stepIndex pinyinWord[$wordIndex]: pinyin is blank",
                        word.pinyin.isBlank()
                    )
                }
                // Check options pinyinWords
                step.options.forEachIndexed { optIndex, option ->
                    option.pinyinWords.forEachIndexed { wordIndex, word ->
                        assertFalse(
                            "Scenario '${scenario.id}' step $stepIndex option[$optIndex] pinyinWord[$wordIndex]: chinese is blank",
                            word.chinese.isBlank()
                        )
                        assertFalse(
                            "Scenario '${scenario.id}' step $stepIndex option[$optIndex] pinyinWord[$wordIndex]: pinyin is blank",
                            word.pinyin.isBlank()
                        )
                    }
                }
            }
        }
    }

    @Test
    fun allScenariosHaveUniqueIds() {
        val ids = scenarios.map { it.id }
        val uniqueIds = ids.toSet()
        assertEquals(
            "Duplicate scenario IDs found: ${ids.groupBy { it }.filter { it.value.size > 1 }.keys}",
            uniqueIds.size,
            ids.size
        )
    }
}
