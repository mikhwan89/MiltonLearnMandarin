package com.ikhwan.mandarinkids

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

object AppPreferences {
    private val SPEECH_SPEED = floatPreferencesKey("speech_speed")

    fun speechSpeedFlow(context: Context): Flow<Float> =
        context.dataStore.data.map { it[SPEECH_SPEED] ?: 1.0f }

    suspend fun saveSpeechSpeed(context: Context, speed: Float) {
        context.dataStore.edit { it[SPEECH_SPEED] = speed }
    }
}
