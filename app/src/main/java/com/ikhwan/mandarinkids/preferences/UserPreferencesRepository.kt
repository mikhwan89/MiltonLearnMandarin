package com.ikhwan.mandarinkids.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

/**
 * Persistent user preferences backed by DataStore.
 *
 * Flows are hot and emit the current value immediately on first collection,
 * making them suitable for direct use with [collectAsState] in composables.
 *
 * Use [getInstance] to obtain the singleton.
 */
class UserPreferencesRepository private constructor(private val context: Context) {

    /** TTS playback rate. 1.0f = normal speed, 0.7f = slow mode. */
    val speechRate: Flow<Float> =
        context.userPrefsDataStore.data.map { it[SPEECH_RATE] ?: 1.0f }

    /** Whether Indonesian translations are shown alongside English. */
    val showIndonesian: Flow<Boolean> =
        context.userPrefsDataStore.data.map { it[SHOW_INDONESIAN] ?: true }

    /** Whether dark mode is enabled. */
    val darkMode: Flow<Boolean> =
        context.userPrefsDataStore.data.map { it[DARK_MODE] ?: false }

    /** Whether the first-run onboarding tutorial has been completed. */
    val onboardingCompleted: Flow<Boolean> =
        context.userPrefsDataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }

    /** Index into [com.ikhwan.mandarinkids.ui.theme.AppThemes] for the active colour theme. */
    val colorThemeIndex: Flow<Int> =
        context.userPrefsDataStore.data.map { it[COLOR_THEME_INDEX] ?: 0 }

    suspend fun saveSpeechRate(rate: Float) {
        context.userPrefsDataStore.edit { it[SPEECH_RATE] = rate }
    }

    suspend fun saveShowIndonesian(show: Boolean) {
        context.userPrefsDataStore.edit { it[SHOW_INDONESIAN] = show }
    }

    suspend fun saveDarkMode(dark: Boolean) {
        context.userPrefsDataStore.edit { it[DARK_MODE] = dark }
    }

    suspend fun saveOnboardingCompleted(completed: Boolean) {
        context.userPrefsDataStore.edit { it[ONBOARDING_COMPLETED] = completed }
    }

    suspend fun saveColorThemeIndex(index: Int) {
        context.userPrefsDataStore.edit { it[COLOR_THEME_INDEX] = index }
    }

    /** Tab IDs that are disabled. Empty = all enabled. Values: "roleplay","flashcard","tone","build" */
    val disabledTabs: Flow<Set<String>> =
        context.userPrefsDataStore.data.map { it[DISABLED_TABS] ?: emptySet() }

    /** ScenarioCategory names that are disabled. Empty = all enabled. */
    val disabledCategories: Flow<Set<String>> =
        context.userPrefsDataStore.data.map { it[DISABLED_CATEGORIES] ?: emptySet() }

    /** Scenario IDs that are disabled. Empty = all enabled. */
    val disabledScenarios: Flow<Set<String>> =
        context.userPrefsDataStore.data.map { it[DISABLED_SCENARIOS] ?: emptySet() }

    suspend fun saveDisabledTabs(tabs: Set<String>) {
        context.userPrefsDataStore.edit { it[DISABLED_TABS] = tabs }
    }
    suspend fun saveDisabledCategories(cats: Set<String>) {
        context.userPrefsDataStore.edit { it[DISABLED_CATEGORIES] = cats }
    }
    suspend fun saveDisabledScenarios(ids: Set<String>) {
        context.userPrefsDataStore.edit { it[DISABLED_SCENARIOS] = ids }
    }

    companion object {
        // Key name kept as "speech_speed" to preserve any existing saved value.
        private val SPEECH_RATE         = floatPreferencesKey("speech_speed")
        private val SHOW_INDONESIAN     = booleanPreferencesKey("show_indonesian")
        private val DARK_MODE           = booleanPreferencesKey("dark_mode")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val COLOR_THEME_INDEX    = intPreferencesKey("color_theme_index")
        private val DISABLED_TABS       = stringSetPreferencesKey("disabled_tabs")
        private val DISABLED_CATEGORIES = stringSetPreferencesKey("disabled_categories")
        private val DISABLED_SCENARIOS  = stringSetPreferencesKey("disabled_scenarios")

        @Volatile private var _instance: UserPreferencesRepository? = null

        fun getInstance(context: Context): UserPreferencesRepository =
            _instance ?: synchronized(this) {
                _instance ?: UserPreferencesRepository(context.applicationContext)
                    .also { _instance = it }
            }
    }
}
