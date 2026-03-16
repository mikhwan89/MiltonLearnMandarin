package com.ikhwan.mandarinkids.data.scenarios

import com.ikhwan.mandarinkids.data.models.Scenario

/**
 * Contract for accessing learning scenarios.
 *
 * The default implementation is [JsonScenarioRepository], which loads scenarios
 * from JSON assets. Tests can supply a [FakeScenarioRepository] to inject
 * controlled data without touching any assets or Android APIs.
 */
interface ScenarioRepository {
    fun getAll(): List<Scenario>
    fun getById(id: String): Scenario?
    fun getCount(): Int
}
