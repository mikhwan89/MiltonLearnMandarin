package com.ikhwan.mandarinkids.db

enum class Badge(
    val id: String,
    val emoji: String,
    val label: String,
    val description: String
) {
    FIRST_STEPS("first_steps", "🐣", "First Steps", "Complete your first scenario"),
    PERFECT_SCORE("perfect_score", "⭐", "Perfect Score", "Get 3 stars on any scenario"),
    WORD_COLLECTOR("word_collector", "📚", "Word Collector", "Master 10 flashcard words"),
    STREAK_STARTER("streak_starter", "🔥", "Streak Starter", "Log in 3 days in a row"),
    STREAK_CHAMPION("streak_champion", "🏆", "Streak Champion", "Log in 7 days in a row"),
    ALL_STARS("all_stars", "🌟", "All Stars", "Get 3 stars on every scenario")
}
