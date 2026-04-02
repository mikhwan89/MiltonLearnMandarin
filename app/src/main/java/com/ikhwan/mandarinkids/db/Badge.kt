package com.ikhwan.mandarinkids.db

enum class Badge(
    val id: String,
    val emoji: String,
    val label: String,
    val description: String
) {
    // ── Scenario — Level 1 ───────────────────────────────────────────────────
    FIRST_STEPS(
        "first_steps", "🐣", "First Steps",
        "Complete any scenario for the first time"
    ),
    PERFECT_SCORE(
        "perfect_score", "⭐", "Perfect Score",
        "Get 3★ on any scenario at Level 1"
    ),
    SCENARIO_ACE(
        "scenario_ace", "🎯", "Scenario Ace",
        "Get 3★ on 5 scenarios at Level 1"
    ),
    ALL_STARS(
        "all_stars", "🌟", "All Stars",
        "Get 3★ on every scenario at Level 1"
    ),

    // ── Scenario — Level 2 ───────────────────────────────────────────────────
    LEVEL_2_DEBUT(
        "level_2_debut", "🔓", "Level 2 Debut",
        "Pass Level 2 on your first scenario"
    ),
    LEVEL_2_ACE(
        "level_2_ace", "🎓", "Level 2 Graduate",
        "Get 3★ at Level 2 on 5 scenarios"
    ),
    LEVEL_2_MASTER(
        "level_2_master", "🥈", "Level 2 Master",
        "Get 3★ at Level 2 on every scenario"
    ),

    // ── Scenario — Level 3 ───────────────────────────────────────────────────
    LEVEL_3_DEBUT(
        "level_3_debut", "💡", "Level 3 Rising",
        "Pass Level 3 on your first scenario"
    ),
    LEVEL_3_ACE(
        "level_3_ace", "🌋", "Level 3 Blazing",
        "Get 3★ at Level 3 on 5 scenarios"
    ),
    LEVEL_3_MASTER(
        "level_3_master", "🥇", "Level 3 Master",
        "Get 3★ at Level 3 on every scenario"
    ),

    // ── Scenario — Level 4 ───────────────────────────────────────────────────
    LEVEL_4_DEBUT(
        "level_4_debut", "⚡", "Level 4 Charged",
        "Pass Level 4 on your first scenario"
    ),
    LEVEL_4_ACE(
        "level_4_ace", "✨", "Level 4 Shining",
        "Get 3★ at Level 4 on 5 scenarios"
    ),
    LEVEL_4_MASTER(
        "level_4_master", "🎖️", "Level 4 Champion",
        "Get 3★ at Level 4 on every scenario"
    ),

    // ── Scenario — Level 5 ───────────────────────────────────────────────────
    LEVEL_5_DEBUT(
        "level_5_debut", "💎", "Diamond Learner",
        "Pass Level 5 on your first scenario"
    ),
    LEVEL_5_ACE(
        "level_5_ace", "🚀", "Rockstar",
        "Get 3★ at Level 5 on 5 scenarios"
    ),
    LEVEL_5_MASTER(
        "level_5_master", "🏵️", "Grandmaster",
        "Get 3★ at Level 5 on every single scenario"
    ),

    // ── Login streak ─────────────────────────────────────────────────────────
    STREAK_STARTER(
        "streak_starter", "🔥", "Streak Starter",
        "Open the app 5 days in a row"
    ),
    STREAK_CHAMPION(
        "streak_champion", "🌈", "Streak Champion",
        "Open the app 15 days in a row"
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
        "Unlock 100 distinct flashcard words"
    ),
    WORD_MASTER(
        "word_master", "🧠", "Word Master",
        "Unlock 500 distinct flashcard words"
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

    // ── Tone Trainer ─────────────────────────────────────────────────────────
    TONE_CURIOUS(
        "tone_curious", "🎵", "Tone Curious",
        "Answer your first tone question correctly"
    ),
    TONE_LEARNER(
        "tone_learner", "🎶", "Tone Learner",
        "Answer 20 tone questions correctly"
    ),
    TONE_ADEPT(
        "tone_adept", "🎼", "Tone Adept",
        "Answer 100 tone questions correctly"
    ),
    TONE_MASTER(
        "tone_master", "🎹", "Tone Master",
        "Answer 200 tone questions correctly"
    ),
    PERFECT_PITCH(
        "perfect_pitch", "🎙️", "Perfect Pitch",
        "Complete a Tone Trainer session with every answer correct on the first try"
    ),
    PITCH_PERFECT(
        "pitch_perfect", "🎸", "Pitch Perfect",
        "Get all 100 tone questions correct in a single session"
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
        "consistent_learner", "📅", "Consistent",
        "Practice flashcards 30 days in a row"
    ),

    // ── Grand Master ─────────────────────────────────────────────────────────
    GRAND_MASTER(
        "grand_master", "🏆", "Grand Master",
        "Earn every other badge in the game"
    ),

    // ── XP milestones ────────────────────────────────────────────────────────
    XP_SEEKER(
        "xp_seeker", "🪙", "XP Seeker",
        "Earn 100 XP"
    ),
    XP_HUNTER(
        "xp_hunter", "🏹", "XP Hunter",
        "Earn 1,000 XP"
    ),
    XP_LEGEND(
        "xp_legend", "🌌", "XP Legend",
        "Earn 10,000 XP"
    ),
    XP_MYTHICAL(
        "xp_mythical", "🌠", "XP Mythical",
        "Earn 50,000 XP"
    )
}
