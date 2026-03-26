package com.ikhwan.mandarinkids.db

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // ── Weekly XP (reactive, SharedPreferences-backed) ───────────────────

    private val _weeklyXp: MutableStateFlow<Int> by lazy { MutableStateFlow(getWeeklyXp()) }
    val weeklyXp: StateFlow<Int> by lazy { _weeklyXp.asStateFlow() }

    private fun getWeeklyXp(): Int {
        val p = prefs()
        return if (p.getString(KEY_WEEKLY_XP_DATE, "") == thisWeekString())
            p.getInt(KEY_WEEKLY_XP, 0) else 0
    }

    private fun incrementWeeklyXp(amount: Int) {
        val p = prefs()
        val thisWeek = thisWeekString()
        val oldXp = if (p.getString(KEY_WEEKLY_XP_DATE, "") == thisWeek)
            p.getInt(KEY_WEEKLY_XP, 0) else 0
        val newXp = oldXp + amount
        p.edit()
            .putString(KEY_WEEKLY_XP_DATE, thisWeek)
            .putInt(KEY_WEEKLY_XP, newXp)
            .apply()
        _weeklyXp.value = newXp
    }

    private fun thisWeekString(): String {
        val c = Calendar.getInstance()
        return "${c.get(Calendar.YEAR)}-W${c.get(Calendar.WEEK_OF_YEAR)}"
    }

    // ── Daily practice count (reactive, SharedPreferences-backed) ────────

    private val _dailyPracticeCount: MutableStateFlow<Int> by lazy {
        MutableStateFlow(getDailyPracticeCount())
    }
    val dailyPracticeCount: StateFlow<Int> by lazy { _dailyPracticeCount.asStateFlow() }

    fun getDailyPracticeCount(): Int {
        val p = prefs()
        return if (p.getString(KEY_DAILY_COUNT_DATE, "") == todayString())
            p.getInt(KEY_DAILY_COUNT, 0)
        else 0
    }

    fun incrementDailyPracticeCount() {
        val p = prefs()
        val today = todayString()
        val oldCount = if (p.getString(KEY_DAILY_COUNT_DATE, "") == today)
            p.getInt(KEY_DAILY_COUNT, 0) else 0
        val newCount = oldCount + 1
        p.edit()
            .putString(KEY_DAILY_COUNT_DATE, today)
            .putInt(KEY_DAILY_COUNT, newCount)
            .apply()
        _dailyPracticeCount.value = newCount
    }

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
        val perfectCount = allProgress.count { it.stars >= 3 }
        if (perfectCount >= 5) awardBadge(Badge.SCENARIO_ACE.id)
        if (allProgress.isNotEmpty() && allProgress.all { it.stars >= 3 }) {
            awardBadge(Badge.ALL_STARS.id)
        }
        checkGrandMasterBadge(allProgress)

        // XP milestone badges
        val totalXp = dao.getTotalXp().first()
        if (totalXp >= 100) awardBadge(Badge.XP_SEEKER.id)
        if (totalXp >= 500) awardBadge(Badge.XP_HUNTER.id)
        if (totalXp >= 1000) awardBadge(Badge.XP_LEGEND.id)

        if (xpGained > 0) incrementWeeklyXp(xpGained)
        return xpGained
    }

    // ── Flashcard XP ─────────────────────────────────────────────────────

    /** Awards +[amount] XP for a correct flashcard answer (stored under a sentinel scenario). */
    suspend fun addFlashcardXp(amount: Int = 1) {
        val current = dao.getById(FLASHCARD_XP_ID).first()
        val oldXp = current?.xp ?: 0
        dao.upsert(
            ScenarioProgressEntity(
                scenarioId = FLASHCARD_XP_ID,
                stars = current?.stars ?: 0,
                xp = oldXp + amount,
                lastPlayedAt = System.currentTimeMillis()
            )
        )
        // XP milestone badges
        val totalXp = dao.getTotalXp().first()
        if (totalXp >= 100) awardBadge(Badge.XP_SEEKER.id)
        if (totalXp >= 500) awardBadge(Badge.XP_HUNTER.id)
        if (totalXp >= 1000) awardBadge(Badge.XP_LEGEND.id)
        // Flashcard daily-practice streak
        checkAndUpdateFlashcardStreak()
        incrementDailyPracticeCount()
        incrementWeeklyXp(amount)
    }

    // ── Tone Trainer XP ──────────────────────────────────────────────────

    /** Awards +[amount] XP for each correctly-identified tone. Checks Tone Trainer badges. */
    suspend fun addToneTrainerXp(amount: Int = 2) {
        val current = dao.getById(TONE_TRAINER_XP_ID).first()
        val oldXp = current?.xp ?: 0
        dao.upsert(
            ScenarioProgressEntity(
                scenarioId = TONE_TRAINER_XP_ID,
                stars = current?.stars ?: 0,
                xp = oldXp + amount,
                lastPlayedAt = System.currentTimeMillis()
            )
        )
        // XP milestone badges
        val totalXp = dao.getTotalXp().first()
        if (totalXp >= 100)  awardBadge(Badge.XP_SEEKER.id)
        if (totalXp >= 500)  awardBadge(Badge.XP_HUNTER.id)
        if (totalXp >= 1000) awardBadge(Badge.XP_LEGEND.id)

        // Tone Trainer cumulative-correct badges
        val p = prefs()
        val newTotal = p.getInt(KEY_TONE_TRAINER_CORRECT_TOTAL, 0) + 1
        p.edit().putInt(KEY_TONE_TRAINER_CORRECT_TOTAL, newTotal).apply()
        if (newTotal >= 1)   awardBadge(Badge.TONE_CURIOUS.id)
        if (newTotal >= 20)  awardBadge(Badge.TONE_LEARNER.id)
        if (newTotal >= 100) awardBadge(Badge.TONE_ADEPT.id)
        if (newTotal >= 200) awardBadge(Badge.TONE_MASTER.id)
        incrementDailyPracticeCount()
        incrementWeeklyXp(amount)
    }

    // ── Sentence Builder XP ──────────────────────────────────────────────

    /** Awards +[amount] XP for each correctly-built sentence. Checks SB badges. */
    suspend fun addSentenceBuilderXp(amount: Int = 10) {
        val current = dao.getById(SENTENCE_BUILDER_XP_ID).first()
        val oldXp = current?.xp ?: 0
        dao.upsert(
            ScenarioProgressEntity(
                scenarioId = SENTENCE_BUILDER_XP_ID,
                stars = current?.stars ?: 0,
                xp = oldXp + amount,
                lastPlayedAt = System.currentTimeMillis()
            )
        )
        // XP milestone badges
        val totalXp = dao.getTotalXp().first()
        if (totalXp >= 100)  awardBadge(Badge.XP_SEEKER.id)
        if (totalXp >= 500)  awardBadge(Badge.XP_HUNTER.id)
        if (totalXp >= 1000) awardBadge(Badge.XP_LEGEND.id)

        // Sentence builder cumulative-correct badges
        val p = prefs()
        val newTotal = p.getInt(KEY_SB_CORRECT_TOTAL, 0) + 1
        p.edit().putInt(KEY_SB_CORRECT_TOTAL, newTotal).apply()
        if (newTotal >= 1)   awardBadge(Badge.SENTENCE_STARTER.id)
        if (newTotal >= 10)  awardBadge(Badge.SENTENCE_BUILDER.id)
        if (newTotal >= 50)  awardBadge(Badge.SENTENCE_SMITH.id)
        if (newTotal >= 100) awardBadge(Badge.SENTENCE_MASTER.id)
        incrementDailyPracticeCount()
        incrementWeeklyXp(amount)
    }

    // ── Mastered word persistence ─────────────────────────────────────────

    /**
     * Seeds all [words] for [scenarioId] into the DB for every [PracticeType].
     * Uses INSERT OR IGNORE so any word already tracked keeps its existing mastery level.
     */
    suspend fun seedWordsForScenario(scenarioId: String, words: List<MasteredWordEntity>) {
        PracticeType.values().forEach { type ->
            words.forEach { word ->
                masteredWordDao.insertIgnore(word.copy(practiceType = type.name))
            }
        }
    }

    suspend fun markWordMastered(entity: MasteredWordEntity) {
        masteredWordDao.upsert(entity)

        // Total distinct word count (all modes)
        val count = masteredWordDao.getTotalCount().first()
        if (count >= 10) awardBadge(Badge.WORD_COLLECTOR.id)
        if (count >= 50) awardBadge(Badge.WORD_SCHOLAR.id)
        if (count >= 100) awardBadge(Badge.WORD_MASTER.id)

        // Default mode ★10
        val defaultHigh = masteredWordDao.getHighMasteryCountByType(PracticeType.DEFAULT.name).first()
        if (defaultHigh >= 25) awardBadge(Badge.FLASHCARD_NOVICE.id)
        if (defaultHigh >= 50) awardBadge(Badge.FLASHCARD_PRO.id)
        if (defaultHigh >= 100) awardBadge(Badge.FLASHCARD_LEGEND.id)

        // Listening mode ★10
        val listeningHigh = masteredWordDao.getHighMasteryCountByType(PracticeType.LISTENING.name).first()
        if (listeningHigh >= 25) awardBadge(Badge.SHARP_EAR.id)
        if (listeningHigh >= 50) awardBadge(Badge.KEEN_EAR.id)
        if (listeningHigh >= 100) awardBadge(Badge.LISTENING_MASTER.id)

        // Reading mode ★10
        val readingHigh = masteredWordDao.getHighMasteryCountByType(PracticeType.READING.name).first()
        if (readingHigh >= 25) awardBadge(Badge.SYMBOL_SPOTTER.id)
        if (readingHigh >= 50) awardBadge(Badge.CHARACTER_READER.id)
        if (readingHigh >= 100) awardBadge(Badge.READING_MASTER.id)

        // Triple Crown: ★10 in all 3 modes for at least 1 word
        val tripleCrown = masteredWordDao.getTripleCrownCount().first()
        if (tripleCrown >= 1) awardBadge(Badge.TRIPLE_CROWN.id)

        checkGrandMasterBadge(dao.getAll().first())
    }

    /** All words for a specific practice modality. */
    fun getAllMasteredWords(type: PracticeType): Flow<List<MasteredWordEntity>> =
        masteredWordDao.getAllByType(type.name)

    /** All words across all practice types (used for stats/badges). */
    fun getAllMasteredWords(): Flow<List<MasteredWordEntity>> = masteredWordDao.getAll()

    /**
     * Ensures every word in DEFAULT mode also exists in LISTENING and READING.
     * Uses INSERT OR IGNORE so existing mastery levels are never overwritten.
     * Call this after scenario seeding to catch any words added via other paths
     * (e.g. DB migration, per-scenario flashcard screen).
     */
    suspend fun syncPracticeTypesFromDefault() {
        val defaultWords = masteredWordDao.getAllByType(PracticeType.DEFAULT.name).first()
        PracticeType.values().filter { it != PracticeType.DEFAULT }.forEach { type ->
            defaultWords.forEach { word ->
                masteredWordDao.insertIgnore(word.copy(practiceType = type.name, boxLevel = 1))
            }
        }
    }

    fun getMasteredWordCount(): Flow<Int> = masteredWordDao.getTotalCount()

    fun getDueWords(now: Long = System.currentTimeMillis()): Flow<List<MasteredWordEntity>> =
        masteredWordDao.getDueWords(now)

    fun getDueWordCount(now: Long = System.currentTimeMillis()): Flow<Int> =
        masteredWordDao.getDueCount(now)

    fun getHighMasteryWordCount(): Flow<Int> = masteredWordDao.getHighMasteryWordCount()

    fun getHighMasteryCountByType(type: PracticeType): Flow<Int> =
        masteredWordDao.getHighMasteryCountByType(type.name)

    // ── Milestone Rewards ─────────────────────────────────────────────────

    suspend fun addReward(
        conditions: List<MilestoneCondition>,
        logic: String,
        rewardText: String
    ) = rewardDao.insert(
        MilestoneReward(
            conditionsJson = encodeConditions(conditions),
            logic = logic,
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

    // ── Grand Master check ───────────────────────────────────────────────────

    /**
     * Awards GRAND_MASTER if:
     *  • every visited scenario has 3 stars, AND
     *  • every word in DEFAULT, LISTENING, and READING modes is at ★10.
     */
    private suspend fun checkGrandMasterBadge(allProgress: List<ScenarioProgressEntity>) {
        val sentinelIds = setOf(FLASHCARD_XP_ID, SENTENCE_BUILDER_XP_ID, TONE_TRAINER_XP_ID)
        val scenarioEntries = allProgress.filter { it.scenarioId !in sentinelIds }
        if (scenarioEntries.isEmpty() || !scenarioEntries.all { it.stars >= 3 }) return

        for (type in PracticeType.values()) {
            val total = masteredWordDao.getTotalCountByType(type.name).first()
            val high  = masteredWordDao.getHighMasteryCountByType(type.name).first()
            if (total == 0 || high < total) return
        }

        awardBadge(Badge.GRAND_MASTER.id)
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
        if (newStreak >= 30) awardBadge(Badge.STREAK_LEGEND.id)

        return newStreak
    }

    // ── Flashcard daily-practice streak ──────────────────────────────────────

    fun getFlashcardStreak(): Int = prefs().getInt(KEY_FLASHCARD_STREAK, 0)

    private fun checkAndUpdateFlashcardStreak() {
        val p = prefs()
        val today = todayString()
        val lastDate = p.getString(KEY_FLASHCARD_LAST_DATE, "") ?: ""
        val current = p.getInt(KEY_FLASHCARD_STREAK, 0)
        val newStreak = when {
            lastDate == today -> return  // already counted today
            lastDate == yesterdayString() -> current + 1
            else -> 1
        }
        p.edit()
            .putString(KEY_FLASHCARD_LAST_DATE, today)
            .putInt(KEY_FLASHCARD_STREAK, newStreak)
            .apply()
        if (newStreak >= 30) awardBadge(Badge.CONSISTENT_LEARNER.id)
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
            .remove(KEY_FLASHCARD_STREAK)
            .remove(KEY_FLASHCARD_LAST_DATE)
            .remove(KEY_SB_CORRECT_TOTAL)
            .remove(KEY_TONE_TRAINER_CORRECT_TOTAL)
            .remove(KEY_DAILY_COUNT)
            .remove(KEY_DAILY_COUNT_DATE)
            .remove(KEY_WEEKLY_XP)
            .remove(KEY_WEEKLY_XP_DATE)
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
        const val FLASHCARD_XP_ID          = "__flashcard__"
        const val SENTENCE_BUILDER_XP_ID   = "__sentence_builder__"
        const val TONE_TRAINER_XP_ID       = "__tone_trainer__"
        private const val PREFS_NAME = "milton_progress"
        private const val KEY_STREAK = "streak"
        private const val KEY_LAST_OPEN_DATE = "last_open_date"
        private const val KEY_EARNED_BADGES = "earned_badges"
        private const val KEY_WORD_OF_DAY_DATE = "word_of_day_date"
        private const val KEY_WORD_OF_DAY_CHINESE = "word_of_day_chinese"
        private const val KEY_PIN_HASH = "parent_pin_hash"
        private const val KEY_SHOW_INDONESIAN = "show_indonesian"
        private const val KEY_FLASHCARD_STREAK    = "flashcard_streak"
        private const val KEY_FLASHCARD_LAST_DATE  = "flashcard_last_date"
        private const val KEY_SB_CORRECT_TOTAL           = "sentence_builder_correct_total"
        private const val KEY_TONE_TRAINER_CORRECT_TOTAL = "tone_trainer_correct_total"
        private const val KEY_DAILY_COUNT                = "daily_practice_count"
        private const val KEY_DAILY_COUNT_DATE           = "daily_practice_count_date"
        const val DAILY_GOAL                             = 5
        private const val KEY_WEEKLY_XP                  = "weekly_xp"
        private const val KEY_WEEKLY_XP_DATE             = "weekly_xp_date"

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
