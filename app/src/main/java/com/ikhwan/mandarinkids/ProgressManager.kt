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
}
