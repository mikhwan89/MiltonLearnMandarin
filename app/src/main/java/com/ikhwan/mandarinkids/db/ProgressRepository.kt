package com.ikhwan.mandarinkids.db

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.Calendar

class ProgressRepository private constructor(
    private val dao: ProgressDao,
    private val masteredWordDao: MasteredWordDao,
    private val rewardDao: MilestoneRewardDao,
    private val context: Context
) {

    // ── Room-backed reads (reactive) ─────────────────────────────────────

    fun getTotalXp(): Flow<Int> = dao.getTotalXp()

    fun getStars(scenarioId: String): Flow<Int> =
        dao.getById(scenarioId).map { it?.stars ?: 0 }

    fun getAllProgress(): Flow<List<ScenarioProgressEntity>> = dao.getAll()

    // ── Per-scenario speech rate ──────────────────────────────────────────

    suspend fun getSpeechRateForScenario(scenarioId: String): Float? =
        dao.getById(scenarioId).first()?.speechRateOverride

    suspend fun saveSpeechRateForScenario(scenarioId: String, rate: Float) {
        val current = dao.getById(scenarioId).first()
        dao.upsert(
            (current ?: ScenarioProgressEntity(scenarioId = scenarioId, stars = 0, xp = 0))
                .copy(speechRateOverride = rate)
        )
    }

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

        // Check scenario-based badges
        if (newStars >= 1) awardBadge(Badge.FIRST_STEPS.id)
        if (newStars >= 3) awardBadge(Badge.PERFECT_SCORE.id)
        val allProgress = dao.getAll().first()
        if (allProgress.isNotEmpty() && allProgress.all { it.stars >= 3 }) {
            awardBadge(Badge.ALL_STARS.id)
        }

        return xpGained
    }

    // ── Mastered word persistence ─────────────────────────────────────────

    suspend fun markWordMastered(entity: MasteredWordEntity) {
        masteredWordDao.upsert(entity)
        val count = masteredWordDao.getTotalCount().first()
        if (count >= 10) awardBadge(Badge.WORD_COLLECTOR.id)
    }

    fun getAllMasteredWords(): Flow<List<MasteredWordEntity>> = masteredWordDao.getAll()

    fun getMasteredWordCount(): Flow<Int> = masteredWordDao.getTotalCount()

    fun getDueWords(now: Long = System.currentTimeMillis()): Flow<List<MasteredWordEntity>> =
        masteredWordDao.getDueWords(now)

    fun getDueWordCount(now: Long = System.currentTimeMillis()): Flow<Int> =
        masteredWordDao.getDueCount(now)

    fun getHighMasteryWordCount(): Flow<Int> = masteredWordDao.getHighMasteryWordCount()

    // ── Milestone Rewards ─────────────────────────────────────────────────

    suspend fun addReward(type: MilestoneType, targetValue: Int, rewardText: String) =
        rewardDao.insert(
            MilestoneReward(
                milestoneType = type.name,
                targetValue = targetValue,
                rewardText = rewardText
            )
        )

    fun getAllRewards(): Flow<List<MilestoneReward>> = rewardDao.getAll()

    suspend fun claimReward(id: Int) = rewardDao.claim(id)

    suspend fun deleteReward(id: Int) = rewardDao.delete(id)

    // ── Word of the Day ───────────────────────────────────────────────────

    fun getOrPickWordOfDay(words: List<MasteredWordEntity>): MasteredWordEntity? {
        if (words.isEmpty()) return null
        val p = prefs()
        val today = todayString()
        val storedDate = p.getString(KEY_WORD_OF_DAY_DATE, "") ?: ""
        val storedChinese = p.getString(KEY_WORD_OF_DAY_CHINESE, "") ?: ""

        if (storedDate == today && storedChinese.isNotEmpty()) {
            return words.firstOrNull { it.chinese == storedChinese }
        }

        val candidates = words.filter { it.boxLevel <= 2 }.ifEmpty { words }
        val picked = candidates.random()
        p.edit()
            .putString(KEY_WORD_OF_DAY_DATE, today)
            .putString(KEY_WORD_OF_DAY_CHINESE, picked.chinese)
            .apply()
        return picked
    }

    // ── Badges ────────────────────────────────────────────────────────────

    fun getEarnedBadges(): Set<String> =
        prefs().getStringSet(KEY_EARNED_BADGES, emptySet()) ?: emptySet()

    fun awardBadge(id: String): Boolean {
        val p = prefs()
        val current = p.getStringSet(KEY_EARNED_BADGES, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (id in current) return false
        current.add(id)
        p.edit().putStringSet(KEY_EARNED_BADGES, current).apply()
        return true
    }

    // ── PIN ───────────────────────────────────────────────────────────────

    fun isPinSet(): Boolean = prefs().getString(KEY_PIN_HASH, "").orEmpty().isNotEmpty()

    fun verifyPin(pin: String): Boolean =
        sha256(pin) == prefs().getString(KEY_PIN_HASH, "")

    fun setPin(pin: String) {
        prefs().edit().putString(KEY_PIN_HASH, sha256(pin)).apply()
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ── Indonesian toggle ─────────────────────────────────────────────────

    fun getShowIndonesian(): Boolean = prefs().getBoolean(KEY_SHOW_INDONESIAN, true)

    fun setShowIndonesian(value: Boolean) {
        prefs().edit().putBoolean(KEY_SHOW_INDONESIAN, value).apply()
    }

    // ── SharedPreferences-backed (streak) ────────────────────────────────

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

        if (newStreak >= 3) awardBadge(Badge.STREAK_STARTER.id)
        if (newStreak >= 7) awardBadge(Badge.STREAK_CHAMPION.id)

        return newStreak
    }

    // ── Reset all progress ────────────────────────────────────────────────

    suspend fun resetAllProgress() {
        dao.deleteAll()
        masteredWordDao.deleteAll()
        rewardDao.deleteAll()
        prefs().edit()
            .remove(KEY_STREAK)
            .remove(KEY_LAST_OPEN_DATE)
            .remove(KEY_EARNED_BADGES)
            .remove(KEY_WORD_OF_DAY_DATE)
            .remove(KEY_WORD_OF_DAY_CHINESE)
            // PIN and Indonesian preference intentionally kept
            .apply()
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
        private const val KEY_EARNED_BADGES = "earned_badges"
        private const val KEY_WORD_OF_DAY_DATE = "word_of_day_date"
        private const val KEY_WORD_OF_DAY_CHINESE = "word_of_day_chinese"
        private const val KEY_PIN_HASH = "parent_pin_hash"
        private const val KEY_SHOW_INDONESIAN = "show_indonesian"

        @Volatile private var _instance: ProgressRepository? = null

        fun getInstance(context: Context): ProgressRepository =
            _instance ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                _instance ?: ProgressRepository(
                    dao = db.progressDao(),
                    masteredWordDao = db.masteredWordDao(),
                    rewardDao = db.milestoneRewardDao(),
                    context = context.applicationContext
                ).also { _instance = it }
            }
    }
}
