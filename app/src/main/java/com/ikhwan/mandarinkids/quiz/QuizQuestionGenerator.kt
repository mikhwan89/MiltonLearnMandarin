package com.ikhwan.mandarinkids.quiz

import com.ikhwan.mandarinkids.data.models.PinyinWord
import com.ikhwan.mandarinkids.data.models.QuizDirection
import com.ikhwan.mandarinkids.data.models.QuizOption
import com.ikhwan.mandarinkids.data.models.QuizQuestion
import com.ikhwan.mandarinkids.data.models.Scenario
import com.ikhwan.mandarinkids.data.models.Speaker
import com.ikhwan.mandarinkids.getFlashcardWords

/**
 * Generates quiz questions dynamically from scenario word data.
 * Level 2: 12 questions from the scenario's quiz + generated vocab questions.
 * Level 3: quiz every word in the scenario.
 */
object QuizQuestionGenerator {

    /**
     * Returns questions for the given mastery [level].
     * Level 1: original scenario quiz questions (unchanged).
     * Level 2: 12 questions — original quiz + generated vocab fill.
     * Level 3: one question per unique word in the scenario.
     */
    fun generate(scenario: Scenario, level: Int): List<QuizQuestion> = when (level) {
        1 -> scenario.quizQuestions
        2 -> generateLevel2(scenario)
        3 -> generateLevel3(scenario)
        6 -> generateLevel6(scenario)
        else -> scenario.quizQuestions
    }

    private fun generateLevel2(scenario: Scenario): List<QuizQuestion> {
        val base = scenario.quizQuestions.toMutableList()
        val words = scenario.getFlashcardWords()
        val generated = generateVocabQuestions(words)
        // Fill up to 12 questions
        val needed = (12 - base.size).coerceAtLeast(0)
        base.addAll(generated.shuffled().take(needed))
        return base.shuffled()
    }

    private fun generateLevel3(scenario: Scenario): List<QuizQuestion> {
        val words = scenario.getFlashcardWords()
        return generateVocabQuestions(words).shuffled()
    }

    private fun generateLevel6(scenario: Scenario): List<QuizQuestion> {
        val characterSteps = scenario.dialogues
            .filter { it.speaker == Speaker.CHARACTER && it.textChinese.isNotBlank() && it.textEnglish.isNotBlank() }
        if (characterSteps.isEmpty()) return scenario.quizQuestions

        // All unique English translations from all dialogue steps as the distractor pool
        val allTranslations = scenario.dialogues
            .map { it.textEnglish }
            .filter { it.isNotBlank() }
            .distinct()

        val questions = mutableListOf<QuizQuestion>()
        for (step in characterSteps.shuffled()) {
            val distractors = allTranslations
                .filter { it != step.textEnglish }
                .shuffled()
                .take(3)
            if (distractors.size < 3) continue

            val correctOption = QuizOption(
                chinese = step.textChinese,
                pinyin = step.textPinyin,
                translation = step.textEnglish
            )
            val allOptions = (distractors.map { QuizOption("", "", it) } + correctOption).shuffled()
            val correctIndex = allOptions.indexOfFirst { it.translation == step.textEnglish }

            questions.add(
                QuizQuestion(
                    direction = QuizDirection.AUDIO_TO_TRANSLATION,
                    questionText = "Listen and choose the correct meaning!",
                    questionChinese = step.textChinese,
                    questionPinyin = step.textPinyin,
                    options = allOptions,
                    correctAnswerIndex = correctIndex,
                    explanation = "${step.textChinese} (${step.textPinyin}) means '${step.textEnglish}'"
                )
            )
            if (questions.size >= 5) break
        }
        return if (questions.isEmpty()) scenario.quizQuestions else questions
    }

    private fun generateVocabQuestions(words: List<PinyinWord>): List<QuizQuestion> {
        if (words.size < 2) return emptyList()
        val questions = mutableListOf<QuizQuestion>()

        for (word in words) {
            // Alternate between Chinese→Translation and Translation→Chinese
            val distractors = words.filter { it.chinese != word.chinese }.shuffled().take(3)
            if (distractors.size < 3) continue // need at least 3 distractors

            val direction = if (questions.size % 2 == 0)
                QuizDirection.CHINESE_TO_TRANSLATION
            else
                QuizDirection.TRANSLATION_TO_CHINESE

            val correctOption = QuizOption(
                chinese = word.chinese,
                pinyin = word.pinyin,
                translation = word.english
            )
            val distractorOptions = distractors.map {
                QuizOption(chinese = it.chinese, pinyin = it.pinyin, translation = it.english)
            }

            val allOptions = (distractorOptions + correctOption).shuffled()
            val correctIndex = allOptions.indexOfFirst { it.chinese == word.chinese }

            questions.add(
                QuizQuestion(
                    direction = direction,
                    questionText = if (direction == QuizDirection.CHINESE_TO_TRANSLATION)
                        "What does this mean?"
                    else
                        "How do you say '${word.english}' in Mandarin?",
                    questionChinese = word.chinese,
                    questionPinyin = word.pinyin,
                    options = allOptions,
                    correctAnswerIndex = correctIndex,
                    explanation = "${word.chinese} (${word.pinyin}) means '${word.english}'"
                )
            )
        }
        return questions
    }
}
