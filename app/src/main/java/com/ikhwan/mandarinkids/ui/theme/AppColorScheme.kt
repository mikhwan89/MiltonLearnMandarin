package com.ikhwan.mandarinkids.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * A pair of colors defining a vertical gradient (start at top, end at bottom).
 */
data class ColorPair(val start: Color, val end: Color) {
    fun asList(): List<Color> = listOf(start, end)
}

/**
 * All color tokens for the app. Use [DefaultPalette] for the current defaults.
 * Swap the [LocalAppColors] provider in MandarinKidsTheme to switch palettes.
 *
 * Semantic rules that must hold for any valid palette:
 *  - [masteryLow] / [masteryLowGradient] must feel "bad/warning" (red family)
 *  - [masteryMid] / [masteryMidGradient] must feel "mediocre/neutral" (amber/yellow family)
 *  - [masteryHigh] / [masteryHighGradient] must feel "excellent/good" (green family)
 *  - [modeWeak] must feel "bad/warning" (red family)
 *  - [modeMastery] must feel "good/excellent" (green family)
 *  - [modeAll] must feel "neutral" (blue family)
 *  - [answerCorrectText] / [answerCorrect] must clearly signal correct (green family)
 *  - [answerWrongText] / [actionNegative] must clearly signal wrong (red family)
 *  - [tone1]..[tone4] / [toneNeutral] may be any distinct colors, but must stay consistent
 *    across every pinyin display in the app (flashcards, conversation bubbles, tone trainer, etc.)
 */
data class AppColorScheme(

    // ── Core tile gradients ──────────────────────────────────────────────────
    /** Blue — character speech bubbles, question cards, word-of-day dialog, stats tiles */
    val tileBlue: ColorPair,
    /** Green — student speech bubbles */
    val tileGreen: ColorPair,
    /** Amber — notes/tips, idle response options, scenario cards, learning tip card */
    val tileAmber: ColorPair,
    /** Purple — name-input tile, sentence-answer area, content-settings tile */
    val tilePurple: ColorPair,
    /** Grey — neutral / disabled surface */
    val tileGrey: ColorPair,

    // ── Action button gradients ──────────────────────────────────────────────
    /** Strong green CTA: "Got it!", "Done", "Start Conversation", "Next →", "Play Again" */
    val actionPositive: ColorPair,
    /** Strong red: "Still Learning", wrong-answer selection */
    val actionNegative: ColorPair,
    /** Grey disabled: buttons that cannot be tapped yet */
    val actionNeutral: ColorPair,

    // ── Answer / feedback ────────────────────────────────────────────────────
    /** Gradient used on correct-answer option buttons */
    val answerCorrect: ColorPair,
    /** Soft gradient for the FeedbackCard background when answer was wrong */
    val answerWrongSoft: ColorPair,
    /** Headline text colour "Correct! 对了！" */
    val answerCorrectText: Color,
    /** Headline text colour "Not quite! 再试试！" */
    val answerWrongText: Color,

    // ── Category tile gradients ──────────────────────────────────────────────
    val categoryEssentials: ColorPair,
    val categoryAtSchool: ColorPair,
    val categorySchoolSubjects: ColorPair,
    val categoryFoodAndEating: ColorPair,
    val categoryFeelingsHealth: ColorPair,
    val categoryPlayHobbies: ColorPair,
    val categoryHome: ColorPair,
    val categoryOutAndAbout: ColorPair,
    val categoryDefault: ColorPair,

    // ── Practice-type tab gradients (Flashcard tab: DEFAULT / LISTENING / READING) ─
    val practiceTypeDefault: ColorPair,
    val practiceTypeListening: ColorPair,
    val practiceTypeReading: ColorPair,

    // ── Practice mode selector ───────────────────────────────────────────────
    /** "All Words" — neutral, e.g. soft blue */
    val modeAll: ColorPair,
    /** "Weak Words" — red family (signals weakness) */
    val modeWeak: ColorPair,
    /** "Maintain" — green family (signals excellence) */
    val modeMastery: ColorPair,

    // ── Text colors ──────────────────────────────────────────────────────────
    /** Primary text on light/pastel gradient tiles */
    val onLightTile: Color,
    /** Primary text on dark/strong gradient tiles (typically white-ish) */
    val onDarkTile: Color,
    /** Subdued/hint secondary text */
    val textSecondary: Color,
    /** "+N XP" earned text */
    val xpGainText: Color,

    // ── Star rating ──────────────────────────────────────────────────────────
    val starFilled: Color,
    val starEmpty: Color,

    // ── Tone colors — consistent across ALL pinyin displays ──────────────────
    val tone1: Color,         // Tone 1 flat ā  — typically red family
    val tone2: Color,         // Tone 2 rising á — typically orange family
    val tone3: Color,         // Tone 3 dip ǎ  — typically green family
    val tone4: Color,         // Tone 4 falling à — typically blue family
    val toneNeutral: Color,   // Neutral / 5th tone — typically grey

    // ── Tone button tile gradients (ToneTrainerScreen choice buttons) ────────
    val toneTile1: ColorPair,
    val toneTile2: ColorPair,
    val toneTile3: ColorPair,
    val toneTile4: ColorPair,
    val toneTileNeutral: ColorPair,
    /** After answer — selected tone was correct */
    val toneTileCorrect: ColorPair,
    /** After answer — selected tone was wrong */
    val toneTileWrong: ColorPair,
    /** After answer — idle (neither selected nor correct) */
    val toneTileIdle: ColorPair,

    // ── Mastery level (fixed semantic: red → amber → green progression) ──────
    val masteryLow: Color,              // Levels 1-3 — red (weak)
    val masteryMid: Color,              // Levels 4-6 — amber (mediocre)
    val masteryHigh: Color,             // Levels 7+  — green (excellent)
    val masteryLowGradient: ColorPair,
    val masteryMidGradient: ColorPair,
    val masteryHighGradient: ColorPair,

    // ── Achievement / milestone ──────────────────────────────────────────────
    /** Gradient for a claimable / unlocked milestone reward tile */
    val achievementUnlocked: ColorPair,
    /** Gradient for a locked / not-yet-earned milestone tile */
    val achievementLocked: ColorPair,
    /** Text / icon color for the "✓ Unlocked" state */
    val achievementUnlockedText: Color,
) {
    /**
     * Returns the appropriate text colour to use on top of the given tile gradient.
     * Picks [onLightTile] for light tiles and [onDarkTile] for dark tiles so that
     * text is always legible regardless of which palette is active.
     */
    fun contentColorFor(tileStart: Color): Color =
        if (tileStart.luminance() > 0.35f) onLightTile else onDarkTile

    fun contentColorFor(tile: ColorPair): Color = contentColorFor(tile.start)

    /** Colour for a pinyin syllable of the given tone number (0 = neutral). */
    fun toneColor(tone: Int): Color = when (tone) {
        1    -> tone1
        2    -> tone2
        3    -> tone3
        4    -> tone4
        else -> toneNeutral
    }

    /** Returns the [ColorPair] to use for a Tone Trainer choice button. */
    fun toneButtonGradient(tone: Int): ColorPair = when (tone) {
        1    -> toneTile1
        2    -> toneTile2
        3    -> toneTile3
        4    -> toneTile4
        else -> toneTileNeutral
    }

    /** Returns the gradient for a category given its [ScenarioCategory] ordinal name. */
    fun categoryGradient(categoryName: String): ColorPair = when (categoryName) {
        "ESSENTIALS"           -> categoryEssentials
        "AT_SCHOOL"            -> categoryAtSchool
        "SCHOOL_SUBJECTS"      -> categorySchoolSubjects
        "FOOD_AND_EATING"      -> categoryFoodAndEating
        "FEELINGS_AND_HEALTH"  -> categoryFeelingsHealth
        "PLAY_AND_HOBBIES"     -> categoryPlayHobbies
        "HOME"                 -> categoryHome
        "OUT_AND_ABOUT"        -> categoryOutAndAbout
        else                   -> categoryDefault
    }

    /** Returns the mastery colour for the given box level. */
    fun masteryColor(level: Int): Color = when {
        level >= 7 -> masteryHigh
        level >= 4 -> masteryMid
        else       -> masteryLow
    }

    fun masteryGradient(level: Int): ColorPair = when {
        level >= 7 -> masteryHighGradient
        level >= 4 -> masteryMidGradient
        else       -> masteryLowGradient
    }
}
