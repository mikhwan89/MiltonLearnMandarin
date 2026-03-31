package com.ikhwan.mandarinkids

object ProgressManager {

    fun calculateStars(quizScore: Int, totalQuestions: Int): Int {
        val pct = quizScore.toFloat() / totalQuestions * 100
        return when {
            pct >= 100f -> 3
            pct >= 70f -> 2
            else -> 1
        }
    }

    fun getLevel(xp: Int): String = getLevelLabel(xp)

    fun getLevelLabel(xp: Int): String = when {
        xp >= 50000 -> "XP Mythical"
        xp >= 10000 -> "XP Legend"
        xp >= 1000  -> "XP Hunter"
        xp >= 100   -> "XP Seeker"
        else        -> "Beginner"
    }
}
