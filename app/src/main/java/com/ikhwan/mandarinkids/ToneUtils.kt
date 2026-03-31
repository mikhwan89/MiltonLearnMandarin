package com.ikhwan.mandarinkids

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.ikhwan.mandarinkids.ui.theme.AppColorScheme
import com.ikhwan.mandarinkids.ui.theme.DefaultPalette

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

    /** Return the display colour for a given tone number (0 = neutral). Uses [DefaultPalette]. */
    fun toneColor(tone: Int): Color = DefaultPalette.toneColor(tone)

    /** Shorthand: detect tone from pinyin string and return its colour. */
    fun pinyinColor(pinyin: String): Color = toneColor(detectTone(pinyin))

    /** Scheme-aware version — uses [scheme] for tone colours. */
    fun pinyinColor(pinyin: String, scheme: AppColorScheme): Color =
        scheme.toneColor(detectTone(splitSyllables(pinyin).firstOrNull() ?: pinyin))

    /** Human-readable label for a tone number. */
    fun toneLabel(tone: Int): String = when (tone) {
        1 -> "Tone 1 — flat (ā)"
        2 -> "Tone 2 — rising (á)"
        3 -> "Tone 3 — dip (ǎ)"
        4 -> "Tone 4 — falling (à)"
        else -> "Neutral tone"
    }

    /**
     * Split a (possibly multi-syllable) pinyin string into individual syllables.
     *
     * Algorithm: scan left-to-right tracking whether we have passed the vowel
     * nucleus of the current syllable. When a consonant is encountered after
     * a vowel it is the start of the next syllable — with two exceptions:
     *   • "ng"  always ends the current syllable (nasal final)
     *   • lone "n" is a final if followed by a consonant or end-of-string,
     *     and an initial of the next syllable if followed by a vowel
     *   • "r" at the very end of the string is erhua, not a new syllable
     *
     * Examples: "pángbiān"→["páng","biān"], "lǎoshī"→["lǎo","shī"],
     *           "wǒmen"→["wǒ","men"], "tóngxué"→["tóng","xué"]
     */
    fun splitSyllables(pinyin: String): List<String> {
        if (pinyin.isBlank()) return listOf(pinyin)

        val allVowels = "aeiouüāáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ"
        val splits = mutableListOf(0)
        var seenVowel = false
        var i = 0

        while (i < pinyin.length) {
            val c = pinyin[i].lowercaseChar()

            if (c in allVowels) {
                seenVowel = true
                i++
                continue
            }

            // c is a consonant
            if (!seenVowel) {
                // Still in the initial consonant cluster of the current syllable
                i++
                continue
            }

            // We are past the vowel nucleus — decide: final or new initial?

            // "ng" is always a nasal final; the character after it starts a new syllable
            if (c == 'n' && i + 1 < pinyin.length && pinyin[i + 1].lowercaseChar() == 'g') {
                i += 2
                if (i < pinyin.length) {
                    splits.add(i)
                    seenVowel = false
                }
                continue
            }

            // lone "n": final if followed by consonant/end, initial if followed by vowel
            if (c == 'n') {
                val next = if (i + 1 < pinyin.length) pinyin[i + 1].lowercaseChar() else '\u0000'
                if (next in allVowels) {
                    // "n" starts the next syllable
                    splits.add(i)
                    seenVowel = false
                }
                // else "n" is the final of this syllable — seenVowel stays true
                // so the next consonant will still trigger a split
                i++
                continue
            }

            // "r" at the very end = erhua final, not a new syllable
            if (c == 'r' && i == pinyin.length - 1) {
                i++
                continue
            }

            // Any other consonant after a vowel = start of a new syllable
            splits.add(i)
            seenVowel = false
            i++
        }

        splits.add(pinyin.length)
        return splits.zipWithNext { a, b -> pinyin.substring(a, b) }.filter { it.isNotEmpty() }
    }

    /**
     * Strip all tone diacritics from a pinyin string, leaving bare vowels.
     * ü retains its umlaut (it is a distinct vowel, not a tone mark).
     * Example: "māo" → "mao", "nǐ" → "ni", "lǖ" → "lü"
     */
    fun stripTones(pinyin: String): String = buildString {
        for (c in pinyin) {
            append(when (c) {
                'ā', 'á', 'ǎ', 'à' -> 'a'
                'ē', 'é', 'ě', 'è' -> 'e'
                'ī', 'í', 'ǐ', 'ì' -> 'i'
                'ō', 'ó', 'ǒ', 'ò' -> 'o'
                'ū', 'ú', 'ǔ', 'ù' -> 'u'
                'ǖ', 'ǘ', 'ǚ', 'ǜ' -> 'ü'
                else -> c
            })
        }
    }

    /**
     * Build an [AnnotatedString] where each syllable is coloured by its own tone.
     * Single-syllable words produce the same result as [pinyinColor]; multi-syllable
     * words get per-syllable colours (e.g. "pángbiān" → orange "páng" + red "biān").
     */
    fun coloredAnnotatedPinyin(pinyin: String): AnnotatedString =
        coloredAnnotatedPinyin(pinyin, DefaultPalette)

    /** Scheme-aware version — uses [scheme] for tone colours. */
    fun coloredAnnotatedPinyin(pinyin: String, scheme: AppColorScheme): AnnotatedString =
        buildAnnotatedString {
            splitSyllables(pinyin).forEach { syllable ->
                withStyle(SpanStyle(color = pinyinColor(syllable, scheme))) {
                    append(syllable)
                }
            }
        }
}
