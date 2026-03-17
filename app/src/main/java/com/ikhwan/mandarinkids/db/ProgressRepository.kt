package com.ikhwan.mandarinkids.db

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

class ProgressRepository private constructor(
    private val dao: ProgressDao,
    private val masteredWordDao: MasteredWordDao,
    private val context: Context
) {

    // ── Room-backed reads (reactive) ─────────────────────────────────────

    fun getTotalXp(): Flow<Int> = dao.getTotalXp()

    fun getStars(scenarioId: String): Flow<Int> =
        dao.getById(scenarioId).map { it?.stars ?: 0 }

    // ── Room-backed write ────────────────────────────────────────────────

    /** Saves scenario progress. Returns XP gained (0 if no improvement). */
    suspend fun saveProgress(scenarioId: String, newStars: Int): Int {
        val current = dao.getById(scenarioId).first()
        val oldStars = current?.stars ?: 0
        val oldXp = current?.xp ?: 0
        val xpGained = if (newStars > oldStars) (newStars - oldStars) * 10 else 0
        dao.upsert(
            ScenarioProgressEntity(
                scenarioId = scenarioId,
                stars = maxOf(oldStars, newStars),
                xp = oldXp + xpGained,
                lastPlayedAt = System.currentTimeMillis()
            )
        )
        return xpGained
    }

    // ── Mastered word persistence ─────────────────────────────────────────

    suspend fun markWordMastered(entity: MasteredWordEntity) = masteredWordDao.upsert(entity)

    fun getAllMasteredWords(): Flow<List<MasteredWordEntity>> = masteredWordDao.getAll()

    fun getMasteredWordCount(): Flow<Int> = masteredWordDao.getTotalCount()

    fun getDueWords(now: Long = System.currentTimeMillis()): Flow<List<MasteredWordEntity>> =
        masteredWordDao.getDueWords(now)

    fun getDueWordCount(now: Long = System.currentTimeMillis()): Flow<Int> =
        masteredWordDao.getDueCount(now)

    // ── SharedPreferences-backed (streak is app-level, not per-scenario) ─

    fun getStreak(): Int = prefs().getInt(KEY_STREAK, 0)

    fun checkAndUpdateStreak(): Int {
        val p = prefs()
        val today = todayString()
        val lastOpen = p.getString(KEY_LAST_OPEN_DATE, "") ?: ""
        val current = p.getInt(KEY_STREAK, 0)
        val newStreak = when {
            lastOpen == today -> current
            lastOpen == yesterdayString() -> current + 1
            else -> 1
        }
        p.edit()
            .putString(KEY_LAST_OPEN_DATE, today)
            .putInt(KEY_STREAK, newStreak)
            .apply()
        return newStreak
    }

    private fun prefs() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun todayString(): String {
        val c = Calendar.getInstance()
        return "${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH) + 1}-${c.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun yesterdayString(): String {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_MONTH, -1)
        return "${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH) + 1}-${c.get(Calendar.DAY_OF_MONTH)}"
    }

    companion object {
        private const val PREFS_NAME = "milton_progress"
        private const val KEY_STREAK = "streak"
        private const val KEY_LAST_OPEN_DATE = "last_open_date"

        @Volatile private var _instance: ProgressRepository? = null

        fun getInstance(context: Context): ProgressRepository =
            _instance ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                _instance ?: ProgressRepository(
                    dao = db.progressDao(),
                    masteredWordDao = db.masteredWordDao(),
                    context = context.applicationContext
                ).also { _instance = it }
            }
    }
}
