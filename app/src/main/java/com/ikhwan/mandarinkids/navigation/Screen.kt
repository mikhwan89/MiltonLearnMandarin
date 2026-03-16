package com.ikhwan.mandarinkids.navigation

import com.ikhwan.mandarinkids.data.models.Scenario

sealed class Screen {
    object Home : Screen()
    data class Flashcard(val scenario: Scenario) : Screen()
    data class RolePlay(val scenario: Scenario) : Screen()
    data class Quiz(val scenario: Scenario, val rolePlayScore: Int) : Screen()
}
