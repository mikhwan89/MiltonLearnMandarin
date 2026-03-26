package com.ikhwan.mandarinkids.db

enum class Badge(
    val id: String,
    val emoji: String,
    val label: String,
    val description: String
) {
    // ── Scenario ─────────────────────────────────────────────────────────────
    FIRST_STEPS(
        "first_steps", "🐣", "First Steps",
        "Complete your first scenario"
    ),
    PERFECT_SCORE(
        "perfect_score", "⭐", "Perfect Score",
        "Get 3★ on any scenario"
    ),
    SCENARIO_ACE(
        "scenario_ace", "🎯", "Scenario Ace",
        "Get 3★ on 5 different scenarios"
    ),
    ALL_STARS(
        "all_stars", "🌟", "All Stars",
        "Get 3★ on every single scenario"
    ),

    // ── Login streak ─────────────────────────────────────────────────────────
    STREAK_STARTER(
        "streak_starter", "🔥", "Streak Starter",
        "Open the app 3 days in a row"
    ),
    STREAK_CHAMPION(
        "streak_champion", "🏆", "Streak Champion",
        "Open the app 7 days in a row"
    ),
    STREAK_LEGEND(
        "streak_legend", "💫", "Streak Legend",
        "Open the app 30 days in a row"
    ),

    // ── Word mastery (all modes combined) ────────────────────────────────────
    WORD_COLLECTOR(
        "word_collector", "📚", "Word Collector",
        "Unlock 10 distinct flashcard words"
    ),
    WORD_SCHOLAR(
        "word_scholar", "📖", "Word Scholar",
        "Unlock 50 distinct flashcard words"
    ),
    WORD_MASTER(
        "word_master", "🧠", "Word Master",
        "Unlock 100 distinct flashcard words"
    ),

    // ── Default mode ★10 ────────────────────────────────────────────────────
    FLASHCARD_NOVICE(
        "flashcard_novice", "🃏", "Flashcard Novice",
        "Reach ★10 on 25 words in Default mode"
    ),
    FLASHCARD_PRO(
        "flashcard_pro", "🎴", "Flashcard Pro",
        "Reach ★10 on 50 words in Default mode"
    ),
    FLASHCARD_LEGEND(
        "flashcard_legend", "🏅", "Flashcard Legend",
        "Reach ★10 on 100 words in Default mode"
    ),

    // ── Listening mode ★10 ──────────────────────────────────────────────────
    SHARP_EAR(
        "sharp_ear", "👂", "Sharp Ear",
        "Reach ★10 on 25 words in Listening mode"
    ),
    KEEN_EAR(
        "keen_ear", "🎧", "Keen Ear",
        "Reach ★10 on 50 words in Listening mode"
    ),
    LISTENING_MASTER(
        "listening_master", "🔊", "Listening Master",
        "Reach ★10 on 100 words in Listening mode"
    ),

    // ── Reading mode ★10 ────────────────────────────────────────────────────
    SYMBOL_SPOTTER(
        "symbol_spotter", "👁️", "Symbol Spotter",
        "Reach ★10 on 25 words in Reading mode"
    ),
    CHARACTER_READER(
        "character_reader", "📝", "Character Reader",
        "Reach ★10 on 50 words in Reading mode"
    ),
    READING_MASTER(
        "reading_master", "🔤", "Reading Master",
        "Reach ★10 on 100 words in Reading mode"
    ),

    // ── Sentence Builder ─────────────────────────────────────────────────────
    SENTENCE_STARTER(
        "sentence_starter", "🧩", "Sentence Starter",
        "Build your first correct sentence"
    ),
    SENTENCE_BUILDER(
        "sentence_builder", "🔨", "Sentence Builder",
        "Build 10 sentences correctly"
    ),
    SENTENCE_SMITH(
        "sentence_smith", "⚒️", "Sentence Smith",
        "Build 50 sentences correctly"
    ),
    SENTENCE_MASTER(
        "sentence_master", "🏗️", "Sentence Master",
        "Build 100 sentences correctly"
    ),
    PERFECT_BUILDER(
        "perfect_builder", "💯", "Perfect Builder",
        "Complete a session with every sentence correct on the first try"
    ),

    // ── Special ──────────────────────────────────────────────────────────────
    TRIPLE_CROWN(
        "triple_crown", "👑", "Triple Crown",
        "Master 1 word to ★10 in all 3 practice modes"
    ),
    CONSISTENT_LEARNER(
        "consistent_learner", "📅", "Consistent Learner",
        "Practice flashcards 30 days in a row"
    ),

    // ── Grand Master ─────────────────────────────────────────────────────────
    GRAND_MASTER(
        "grand_master", "🌈", "Grand Master",
        "Get 3★ on every scenario AND reach ★10 on every word in all 3 flashcard modes"
    ),

    // ── XP milestones ────────────────────────────────────────────────────────
    XP_SEEKER(
        "xp_seeker", "✨", "XP Seeker",
        "Earn 100 XP"
    ),
    XP_HUNTER(
        "xp_hunter", "💎", "XP Hunter",
        "Earn 500 XP"
    ),
    XP_LEGEND(
        "xp_legend", "🚀", "XP Legend",
        "Earn 1,000 XP"
    )
}
