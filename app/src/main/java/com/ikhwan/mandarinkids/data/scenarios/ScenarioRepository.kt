package com.ikhwan.mandarinkids.data.scenarios

import android.content.Context
import com.ikhwan.mandarinkids.data.models.Scenario

/**
 * Central repository for all learning scenarios.
 * Call init(context) once from MainActivity.onCreate before any screen is shown.
 * After that, getAllScenarios() returns the loaded list with no further I/O.
 *
 * To add a new scenario: create a JSON file in assets/scenarios/ and add its
 * filename to assets/scenarios/index.json. No Kotlin changes required.
 */
object ScenarioRepository {

    private var scenarios: List<Scenario> = emptyList()

    fun init(context: Context) {
        scenarios = JsonScenarioLoader.loadAll(context)
    }

    fun getAllScenarios(): List<Scenario> = scenarios

    fun getScenarioById(id: String): Scenario? = scenarios.find { it.id == id }

    fun getScenarioCount(): Int = scenarios.size
}
