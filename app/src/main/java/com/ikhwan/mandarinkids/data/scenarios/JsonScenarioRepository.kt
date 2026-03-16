package com.ikhwan.mandarinkids.data.scenarios

import android.content.Context
import com.ikhwan.mandarinkids.data.models.Scenario

/**
 * [ScenarioRepository] implementation that loads scenarios from JSON asset files.
 *
 * Call [init] once from MainActivity.onCreate before any screen is shown.
 * After that, all reads are in-memory with no further I/O.
 *
 * To add a new scenario: create a JSON file in assets/scenarios/ and add its
 * filename to assets/scenarios/index.json. No Kotlin changes required.
 */
object JsonScenarioRepository : ScenarioRepository {

    private var scenarios: List<Scenario> = emptyList()

    fun init(context: Context) {
        scenarios = JsonScenarioLoader.loadAll(context)
    }

    override fun getAll(): List<Scenario> = scenarios

    override fun getById(id: String): Scenario? = scenarios.find { it.id == id }

    override fun getCount(): Int = scenarios.size
}
