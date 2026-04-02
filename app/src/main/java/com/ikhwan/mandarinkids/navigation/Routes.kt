package com.ikhwan.mandarinkids.navigation

object Routes {
    const val HOME             = "home"
    const val PRACTICE         = "practice"
    const val TONE_TRAINER     = "tone_trainer"
    const val SENTENCE_BUILDER = "sentence_builder"
    const val PROGRESS         = "progress"
    const val PARENT_DASHBOARD = "parent_dashboard"
    const val PIN              = "pin"
    const val CATEGORY = "category/{categoryName}"
    const val FLASHCARD = "flashcard/{scenarioId}/{level}"
    const val ROLEPLAY = "roleplay/{scenarioId}/{level}"
    const val QUIZ = "quiz/{scenarioId}/{rolePlayScore}/{level}"
    const val SENTENCE_QUIZ = "sentence_quiz/{scenarioId}/{level}"

    fun category(categoryName: String) = "category/$categoryName"
    fun flashcard(scenarioId: String, level: Int = 1) = "flashcard/$scenarioId/$level"
    fun roleplay(scenarioId: String, level: Int = 1) = "roleplay/$scenarioId/$level"
    fun quiz(scenarioId: String, rolePlayScore: Int, level: Int = 1) = "quiz/$scenarioId/$rolePlayScore/$level"
    fun sentenceQuiz(scenarioId: String, level: Int) = "sentence_quiz/$scenarioId/$level"
}
