package com.ikhwan.mandarinkids.data.scenarios

import android.content.Context
import com.ikhwan.mandarinkids.data.models.Scenario
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

object JsonScenarioLoader {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun loadAll(context: Context): List<Scenario> {
        val indexText = context.assets.open("scenarios/index.json")
            .bufferedReader().readText()
        val filenames = json.decodeFromString(ListSerializer(String.serializer()), indexText)
        return filenames.map { filename ->
            val content = context.assets.open("scenarios/$filename")
                .bufferedReader().readText()
            json.decodeFromString<Scenario>(content)
        }
    }
}
