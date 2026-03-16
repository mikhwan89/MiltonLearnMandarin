package com.ikhwan.mandarinkids

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

object ProgressManager {
    private const val PREFS_NAME = "milton_progress"
    private const val KEY_TOTAL_XP = "total_xp"
    private const val KEY_STREAK = "streak"
    private const val KEY_LAST_OPEN_DATE = "last_open_date"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Stars (0–3) for a scenario
    fun getStars(context: Context, scenarioId: String): Int =
        prefs(context).getInt("stars_$scenarioId", 0)

    /** Save progress. Returns XP gained (0 if no improvement over previous best). */
    fun saveProgress(context: Context, scenarioId: String, newStars: Int): Int {
        val p = prefs(context)
        val oldStars = p.getInt("stars_$scenarioId", 0)
        val xpGained = if (newStars > oldStars) (newStars - oldStars) * 10 else 0
        p.edit()
            .putInt("stars_$scenarioId", maxOf(oldStars, newStars))
            .putInt(KEY_TOTAL_XP, p.getInt(KEY_TOTAL_XP, 0) + xpGained)
            .apply()
        return xpGained
    }

    fun getTotalXp(context: Context): Int =
        prefs(context).getInt(KEY_TOTAL_XP, 0)

    fun calculateStars(quizScore: Int, totalQuestions: Int): Int {
        val pct = quizScore.toFloat() / totalQuestions * 100
        return when {
            pct >= 100f -> 3
            pct >= 70f -> 2
            else -> 1
        }
    }

    fun getLevel(xp: Int): String = when {
        xp >= 180 -> "中文小明星"
        xp >= 60 -> "小达人"
        else -> "初学者"
    }

    fun getLevelLabel(xp: Int): String = when {
        xp >= 180 -> "Mandarin Star 🌟"
        xp >= 60 -> "Junior Expert ⭐"
        else -> "Beginner 📚"
    }

    /** Call once on app open to maintain the daily streak. */
    fun checkAndUpdateStreak(context: Context): Int {
        val p = prefs(context)
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

    fun getStreak(context: Context): Int =
        prefs(context).getInt(KEY_STREAK, 0)

    private fun todayString(): String {
        val c = Calendar.getInstance()
        return "${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH) + 1}-${c.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun yesterdayString(): String {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_MONTH, -1)
        return "${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH) + 1}-${c.get(Calendar.DAY_OF_MONTH)}"
    }
}
