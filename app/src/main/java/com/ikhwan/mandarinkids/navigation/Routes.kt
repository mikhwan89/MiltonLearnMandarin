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
    const val FLASHCARD = "flashcard/{scenarioId}"
    const val ROLEPLAY = "roleplay/{scenarioId}"
    const val QUIZ = "quiz/{scenarioId}/{rolePlayScore}"

    fun category(categoryName: String) = "category/$categoryName"
    fun flashcard(scenarioId: String) = "flashcard/$scenarioId"
    fun roleplay(scenarioId: String) = "roleplay/$scenarioId"
    fun quiz(scenarioId: String, rolePlayScore: Int) = "quiz/$scenarioId/$rolePlayScore"
}
