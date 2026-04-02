package com.ikhwan.mandarinkids.data.scenarios

import android.content.Context
import com.ikhwan.mandarinkids.data.models.Scenario
import com.ikhwan.mandarinkids.db.AppDatabase
import com.ikhwan.mandarinkids.db.CustomScenarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class CustomScenarioRepository private constructor(private val context: Context) {

    private val dao = AppDatabase.getInstance(context).customScenarioDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    val scenariosFlow: Flow<List<Scenario>> = dao.getAll().map { entities ->
        entities.mapNotNull { entity ->
            try { json.decodeFromString<Scenario>(entity.scenarioJson) } catch (e: Exception) { null }
        }
    }

    init {
        // Keep JsonScenarioRepository in sync whenever Room changes
        scope.launch {
            scenariosFlow.collect { list ->
                JsonScenarioRepository.setCustomScenarios(list)
            }
        }
    }

    /** Validates the JSON, checks ID uniqueness, then persists. Returns error message on failure. */
    suspend fun addFromJson(rawJson: String): String? {
        val scenario = try {
            json.decodeFromString<Scenario>(rawJson.trim())
        } catch (e: Exception) {
            return "Invalid JSON format: ${e.message?.take(120)}"
        }

        // Field validation
        if (scenario.id.isBlank()) return "The \"id\" field must not be blank."
        if (scenario.title.isBlank()) return "The \"title\" field must not be blank."
        if (scenario.dialogues.isEmpty()) return "The scenario must have at least one dialogue step."
        if (scenario.quizQuestions.isEmpty()) return "The scenario must have at least one quiz question."
        val emptyPinyinStep = scenario.dialogues.firstOrNull { it.pinyinWords.isEmpty() }
        if (emptyPinyinStep != null) return "Dialogue step id=${emptyPinyinStep.id} is missing pinyinWords. Every step needs per-word pinyin data."

        // ID collision check
        if (scenario.id in JsonScenarioRepository.builtInIds) {
            return "ID \"${scenario.id}\" conflicts with a built-in scenario. Use a unique ID starting with \"custom_\"."
        }

        dao.insert(CustomScenarioEntity(id = scenario.id, scenarioJson = rawJson.trim()))
        return null // success
    }

    suspend fun delete(id: String) = dao.deleteById(id)

    companion object {
        @Volatile private var _instance: CustomScenarioRepository? = null
        fun getInstance(context: Context): CustomScenarioRepository =
            _instance ?: synchronized(this) {
                _instance ?: CustomScenarioRepository(context.applicationContext).also { _instance = it }
            }
    }
}
