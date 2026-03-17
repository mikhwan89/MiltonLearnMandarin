package com.ikhwan.mandarinkids

import com.ikhwan.mandarinkids.data.models.*
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

/**
 * Verifies that [kotlinx.serialization] can parse well-formed Scenario JSON into
 * the expected data model — the same logic used by JsonScenarioLoader at runtime.
 * No Android Context required.
 */
class JsonParsingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // ── Full scenario round-trip ──────────────────────────────────────────

    @Test
    fun minimalScenario_parsesWithoutError() {
        val scenario = json.decodeFromString<Scenario>(MINIMAL_SCENARIO_JSON)
        assertEquals("test_001", scenario.id)
        assertEquals("Test", scenario.title)
        assertTrue(scenario.dialogues.isEmpty())
        assertTrue(scenario.quizQuestions.isEmpty())
    }

    @Test
    fun fullScenario_parsesDialogueStep() {
        val scenario = json.decodeFromString<Scenario>(FULL_SCENARIO_JSON)
        assertEquals(1, scenario.dialogues.size)
        val step = scenario.dialogues[0]
        assertEquals(1, step.id)
        assertEquals(Speaker.CHARACTER, step.speaker)
        assertEquals(ResponseType.LISTEN_ONLY, step.responseType)
        assertEquals("你好！", step.textChinese)
    }

    @Test
    fun fullScenario_parsesPinyinWords() {
        val scenario = json.decodeFromString<Scenario>(FULL_SCENARIO_JSON)
        val words = scenario.dialogues[0].pinyinWords
        assertEquals(1, words.size)
        assertEquals("nǐ", words[0].pinyin)
        assertEquals("你", words[0].chinese)
        assertEquals("you", words[0].english)
    }

    @Test
    fun fullScenario_parsesQuizQuestion() {
        val scenario = json.decodeFromString<Scenario>(FULL_SCENARIO_JSON)
        assertEquals(1, scenario.quizQuestions.size)
        val q = scenario.quizQuestions[0]
        assertEquals(QuizDirection.CHINESE_TO_TRANSLATION, q.direction)
        assertEquals(0, q.correctAnswerIndex)
        assertEquals(2, q.options.size)
        assertEquals("Hello", q.options[0].translation)
    }

    // ── Optional-field defaults ───────────────────────────────────────────

    @Test
    fun dialogueStep_missingPinyinWords_defaultsToEmpty() {
        val step = json.decodeFromString<DialogueStep>(STEP_WITHOUT_PINYIN_WORDS)
        assertTrue(step.pinyinWords.isEmpty())
    }

    @Test
    fun dialogueStep_missingOptions_defaultsToEmpty() {
        val step = json.decodeFromString<DialogueStep>(STEP_WITHOUT_PINYIN_WORDS)
        assertTrue(step.options.isEmpty())
    }

    @Test
    fun dialogueStep_missingUserNameInput_defaultsToFalse() {
        val step = json.decodeFromString<DialogueStep>(STEP_WITHOUT_PINYIN_WORDS)
        assertFalse(step.userNameInput)
    }

    @Test
    fun responseOption_missingIsCorrect_defaultsToTrue() {
        val option = json.decodeFromString<ResponseOption>(OPTION_WITHOUT_IS_CORRECT)
        assertTrue(option.isCorrect)
    }

    @Test
    fun quizQuestion_missingQuestionChinese_defaultsToEmpty() {
        val q = json.decodeFromString<QuizQuestion>(QUIZ_QUESTION_MINIMAL)
        assertEquals("", q.questionChinese)
        assertEquals("", q.questionPinyin)
    }

    // ── Robustness ────────────────────────────────────────────────────────

    @Test
    fun scenario_unknownFields_areIgnored() {
        val scenario = json.decodeFromString<Scenario>(SCENARIO_WITH_UNKNOWN_FIELDS)
        assertEquals("test_extra", scenario.id)
        // parsing must not throw despite the extra field
    }

    @Test
    fun scenario_multipleDialogues_allParsed() {
        val scenario = json.decodeFromString<Scenario>(SCENARIO_MULTI_STEPS)
        assertEquals(2, scenario.dialogues.size)
        assertEquals(Speaker.CHARACTER, scenario.dialogues[0].speaker)
        assertEquals(Speaker.STUDENT, scenario.dialogues[1].speaker)
    }

    // ── Test JSON fixtures ────────────────────────────────────────────────

    companion object {
        val MINIMAL_SCENARIO_JSON = """
            {
              "id": "test_001",
              "title": "Test",
              "description": "A test scenario",
              "characterName": "老师",
              "characterEmoji": "👩‍🏫",
              "characterRole": "Teacher",
              "dialogues": [],
              "quizQuestions": []
            }
        """.trimIndent()

        val FULL_SCENARIO_JSON = """
            {
              "id": "full_001",
              "title": "Greet the Teacher",
              "description": "Say hello",
              "characterName": "王老师",
              "characterEmoji": "👩‍🏫",
              "characterRole": "Teacher",
              "dialogues": [
                {
                  "id": 1,
                  "speaker": "CHARACTER",
                  "textChinese": "你好！",
                  "textPinyin": "nǐ hǎo!",
                  "textEnglish": "Hello!",
                  "textIndonesian": "Halo!",
                  "responseType": "LISTEN_ONLY",
                  "pinyinWords": [
                    { "pinyin": "nǐ", "chinese": "你", "english": "you", "indonesian": "kamu" }
                  ]
                }
              ],
              "quizQuestions": [
                {
                  "direction": "CHINESE_TO_TRANSLATION",
                  "questionText": "What does 你好 mean?",
                  "options": [
                    { "chinese": "你好", "pinyin": "nǐ hǎo", "translation": "Hello" },
                    { "chinese": "再见", "pinyin": "zài jiàn", "translation": "Goodbye" }
                  ],
                  "correctAnswerIndex": 0,
                  "explanation": "你好 means Hello"
                }
              ]
            }
        """.trimIndent()

        val STEP_WITHOUT_PINYIN_WORDS = """
            {
              "id": 1,
              "speaker": "CHARACTER",
              "textChinese": "你好",
              "textPinyin": "nǐ hǎo",
              "textEnglish": "Hello",
              "textIndonesian": "Halo",
              "responseType": "LISTEN_ONLY"
            }
        """.trimIndent()

        val OPTION_WITHOUT_IS_CORRECT = """
            {
              "chinese": "你好",
              "pinyin": "nǐ hǎo",
              "english": "Hello",
              "indonesian": "Halo"
            }
        """.trimIndent()

        val QUIZ_QUESTION_MINIMAL = """
            {
              "direction": "CHINESE_TO_TRANSLATION",
              "questionText": "What does 你好 mean?",
              "options": [
                { "chinese": "你好", "pinyin": "nǐ hǎo", "translation": "Hello" }
              ],
              "correctAnswerIndex": 0,
              "explanation": "你好 means Hello"
            }
        """.trimIndent()

        val SCENARIO_WITH_UNKNOWN_FIELDS = """
            {
              "id": "test_extra",
              "title": "Test",
              "description": "Test",
              "characterName": "老师",
              "characterEmoji": "👩",
              "characterRole": "Teacher",
              "dialogues": [],
              "quizQuestions": [],
              "futureField": "should be silently ignored"
            }
        """.trimIndent()

        val SCENARIO_MULTI_STEPS = """
            {
              "id": "multi",
              "title": "Multi",
              "description": "Test",
              "characterName": "老师",
              "characterEmoji": "👩",
              "characterRole": "Teacher",
              "dialogues": [
                {
                  "id": 1, "speaker": "CHARACTER",
                  "textChinese": "你好", "textPinyin": "nǐ hǎo",
                  "textEnglish": "Hello", "textIndonesian": "Halo",
                  "responseType": "LISTEN_ONLY"
                },
                {
                  "id": 2, "speaker": "STUDENT",
                  "textChinese": "你好", "textPinyin": "nǐ hǎo",
                  "textEnglish": "Hello", "textIndonesian": "Halo",
                  "responseType": "SINGLE_CHOICE",
                  "options": [
                    { "chinese": "你好", "pinyin": "nǐ hǎo", "english": "Hello", "indonesian": "Halo" }
                  ]
                }
              ],
              "quizQuestions": []
            }
        """.trimIndent()
    }
}
