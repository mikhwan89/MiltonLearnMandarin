package com.ikhwan.mandarinkids.navigation

object Routes {
    const val HOME = "home"
    const val PRACTICE = "practice"
    const val FLASHCARD = "flashcard/{scenarioId}"
    const val ROLEPLAY = "roleplay/{scenarioId}"
    const val QUIZ = "quiz/{scenarioId}/{rolePlayScore}"

    fun flashcard(scenarioId: String) = "flashcard/$scenarioId"
    fun roleplay(scenarioId: String) = "roleplay/$scenarioId"
    fun quiz(scenarioId: String, rolePlayScore: Int) = "quiz/$scenarioId/$rolePlayScore"
}
