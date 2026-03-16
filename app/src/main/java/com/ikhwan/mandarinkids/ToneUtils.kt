package com.ikhwan.mandarinkids

import androidx.compose.ui.graphics.Color

object ToneUtils {

    /**
     * Detect the tone of a pinyin string by looking for diacritical marks.
     * Returns 1–4 for toned syllables, 0 for neutral tone.
     */
    fun detectTone(pinyin: String): Int = when {
        pinyin.any { it in "āēīōūǖĀĒĪŌŪǕ" } -> 1
        pinyin.any { it in "áéíóúǘÁÉÍÓÚǗ" } -> 2
        pinyin.any { it in "ǎěǐǒǔǚǍĚǏǑǓǙ" } -> 3
        pinyin.any { it in "àèìòùǜÀÈÌÒÙǛ" } -> 4
        else -> 0
    }

    /** Return the display colour for a given tone number (0 = neutral). */
    fun toneColor(tone: Int): Color = when (tone) {
        1 -> Color(0xFFD32F2F) // Red     — 1st tone: flat ā
        2 -> Color(0xFFE65100) // Orange  — 2nd tone: rising á
        3 -> Color(0xFF2E7D32) // Green   — 3rd tone: dip ǎ
        4 -> Color(0xFF1565C0) // Blue    — 4th tone: falling à
        else -> Color(0xFF757575) // Grey — neutral (ma, de, le…)
    }

    /** Shorthand: detect tone from pinyin string and return its colour. */
    fun pinyinColor(pinyin: String): Color = toneColor(detectTone(pinyin))

    /** Human-readable label for a tone number. */
    fun toneLabel(tone: Int): String = when (tone) {
        1 -> "Tone 1 — flat (ā)"
        2 -> "Tone 2 — rising (á)"
        3 -> "Tone 3 — dip (ǎ)"
        4 -> "Tone 4 — falling (à)"
        else -> "Neutral tone"
    }
}
