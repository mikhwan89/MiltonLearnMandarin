package com.ikhwan.mandarinkids.data.scenarios

import android.content.Context
import com.ikhwan.mandarinkids.data.models.Scenario

/**
 * [ScenarioRepository] implementation that loads scenarios from JSON asset files
 * and merges in custom scenarios stored in Room.
 *
 * Call [init] once from MainActivity.onCreate before any screen is shown.
 * After that, all reads are in-memory with no further I/O.
 *
 * To add a new built-in scenario: create a JSON file in assets/scenarios/ and add its
 * filename to assets/scenarios/index.json. No Kotlin changes required.
 *
 * Custom (user-created) scenarios are injected via [setCustomScenarios] by
 * [CustomScenarioRepository] whenever the Room database changes.
 */
object JsonScenarioRepository : ScenarioRepository {

    private var builtInScenarios: List<Scenario> = emptyList()
    private var customScenarios: List<Scenario> = emptyList()

    /** IDs of app-bundled scenarios — used to reject conflicting custom IDs. */
    val builtInIds: Set<String> get() = builtInScenarios.map { it.id }.toSet()

    fun init(context: Context) {
        builtInScenarios = JsonScenarioLoader.loadAll(context)
    }

    fun setCustomScenarios(scenarios: List<Scenario>) {
        customScenarios = scenarios
    }

    override fun getAll(): List<Scenario> = builtInScenarios + customScenarios

    override fun getById(id: String): Scenario? =
        builtInScenarios.find { it.id == id } ?: customScenarios.find { it.id == id }

    override fun getCount(): Int = builtInScenarios.size + customScenarios.size
}
