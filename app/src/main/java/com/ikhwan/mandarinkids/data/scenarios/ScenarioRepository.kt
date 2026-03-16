package com.ikhwan.mandarinkids.data.scenarios

import com.ikhwan.mandarinkids.data.models.Scenario

/**
 * Central repository for all learning scenarios
 * Add new scenarios by creating a new file and adding the function here
 */
object ScenarioRepository {

    /**
     * Returns all available scenarios in order
     */
    fun getAllScenarios(): List<Scenario> {
        return listOf(
            getScenario1_GreetingTeacher(),
            getScenario2_MeetingMing(),
            getScenario3_SnackTime(),
            getScenario4_AskingBathroom(),
            getScenario5_Playground(),
            getScenario6_Goodbye(),
            getScenario7_BorrowingThings(),
            getScenario8_FeelingUnwell(),
            getScenario9_GettingLost(),
            getScenario10_SayingSorry(),
            getScenario11_AfterSchool(),
            getScenario12_RaisingHand()
        )
    }

    /**
     * Get a specific scenario by ID
     */
    fun getScenarioById(id: String): Scenario? {
        return getAllScenarios().find { it.id == id }
    }

    /**
     * Get total number of scenarios
     */
    fun getScenarioCount(): Int {
        return getAllScenarios().size
    }
}