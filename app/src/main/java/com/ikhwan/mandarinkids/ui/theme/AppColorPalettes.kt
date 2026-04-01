package com.ikhwan.mandarinkids.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Shared semantic colors — fixed across ALL palettes ────────────────────────
// These must never change per palette because children learn their meaning:
//   green = correct / go, red = wrong / stop, amber = stars/mastery-mid
private val SharedActionPositive   = ColorPair(Color(0xFF388E3C), Color(0xFF66BB6A))
private val SharedActionNegative   = ColorPair(Color(0xFFC62828), Color(0xFFEF5350))
private val SharedActionNeutral    = ColorPair(Color(0xFF9E9E9E), Color(0xFFBDBDBD))
private val SharedAnswerCorrect    = ColorPair(Color(0xFF388E3C), Color(0xFF66BB6A))
private val SharedAnswerWrongSoft  = ColorPair(Color(0xFFFFCDD2), Color(0xFFFFEBEE))
private val SharedAnswerCorrectText = Color(0xFF2E7D32)
private val SharedAnswerWrongText   = Color(0xFFC62828)
private val SharedStarFilled = Color(0xFFFFC107)
private val SharedStarEmpty  = Color(0xFFBDBDBD)
// Tone colours — must be consistent everywhere pinyin is rendered
private val SharedTone1       = Color(0xFFD32F2F) // Red    — 1st tone flat ā
private val SharedTone2       = Color(0xFFE65100) // Orange — 2nd tone rising á
private val SharedTone3       = Color(0xFF2E7D32) // Green  — 3rd tone dip ǎ
private val SharedTone4       = Color(0xFF1565C0) // Blue   — 4th tone falling à
private val SharedToneNeutral = Color(0xFF757575) // Grey   — neutral tone
private val SharedToneTile1       = ColorPair(Color(0xFFEF5350), Color(0xFFEF9A9A))
private val SharedToneTile2       = ColorPair(Color(0xFFFF6D00), Color(0xFFFFAB40))
private val SharedToneTile3       = ColorPair(Color(0xFF2E7D32), Color(0xFF66BB6A))
private val SharedToneTile4       = ColorPair(Color(0xFF1565C0), Color(0xFF42A5F5))
private val SharedToneTileNeutral = ColorPair(Color(0xFF616161), Color(0xFF9E9E9E))
private val SharedToneTileCorrect = ColorPair(Color(0xFF388E3C), Color(0xFF66BB6A))
private val SharedToneTileWrong   = ColorPair(Color(0xFFC62828), Color(0xFFEF5350))
private val SharedToneTileIdle    = ColorPair(Color(0xFF9E9E9E), Color(0xFFBDBDBD))
// Mastery: red(weak) → amber(mid) → green(excellent) — fixed semantic
private val SharedMasteryLow      = Color(0xFFEF5350)
private val SharedMasteryMid      = Color(0xFFFFA726)
private val SharedMasteryHigh     = Color(0xFF66BB6A)
private val SharedMasteryLowGrad  = ColorPair(Color(0xFFEF5350), Color(0xFFE57373))
private val SharedMasteryMidGrad  = ColorPair(Color(0xFFFF8F00), Color(0xFFFFA726))
private val SharedMasteryHighGrad = ColorPair(Color(0xFF43A047), Color(0xFF66BB6A))
private val SharedXpGainText = Color(0xFF4CAF50)

// ── 1. Sage (Default) ─────────────────────────────────────────────────────────
val DefaultPalette = AppColorScheme(
    tileBlue    = ColorPair(Color(0xFFD0E8F8), Color(0xFFE4F2FB)),
    tileGreen   = ColorPair(Color(0xFFD4EDD0), Color(0xFFE8F5E2)),
    tileAmber   = ColorPair(Color(0xFFFFF0B3), Color(0xFFFFF8D9)),
    tilePurple  = ColorPair(Color(0xFFE8E4F5), Color(0xFFF2EFF9)),
    tileGrey    = ColorPair(Color(0xFFEEEEEE), Color(0xFFF5F5F5)),
    actionPositive = SharedActionPositive,
    actionNegative = SharedActionNegative,
    actionNeutral  = SharedActionNeutral,
    answerCorrect     = SharedAnswerCorrect,
    answerWrongSoft   = SharedAnswerWrongSoft,
    answerCorrectText = SharedAnswerCorrectText,
    answerWrongText   = SharedAnswerWrongText,
    categoryEssentials      = ColorPair(Color(0xFFD0E8F8), Color(0xFFE4F2FB)), // blue
    categoryAtSchool        = ColorPair(Color(0xFFD4EDD0), Color(0xFFE6F4E4)), // green
    categorySchoolSubjects  = ColorPair(Color(0xFFE8E4F5), Color(0xFFF2EFF9)), // purple
    categoryFoodAndEating   = ColorPair(Color(0xFFFFDDB5), Color(0xFFFFEDD4)), // orange/peach
    categoryFeelingsHealth  = ColorPair(Color(0xFFF5E0E0), Color(0xFFFAEEEE)), // rose
    categoryPlayHobbies     = ColorPair(Color(0xFFE8F5C4), Color(0xFFF3FAD8)), // lime
    categoryHome            = ColorPair(Color(0xFFB2EDE8), Color(0xFFCCF5F2)), // teal
    categoryOutAndAbout     = ColorPair(Color(0xFFD4D0F8), Color(0xFFE4E0FC)), // periwinkle
    categoryDefault         = ColorPair(Color(0xFFF5F4ED), Color(0xFFF5F4ED)),
    practiceTypeDefault   = ColorPair(Color(0xFFD0E8F8), Color(0xFFE4F2FB)),
    practiceTypeListening = ColorPair(Color(0xFFB8E4F0), Color(0xFFD4EFF8)),
    practiceTypeReading   = ColorPair(Color(0xFFE8E4F5), Color(0xFFF2EFF9)),
    modeAll     = ColorPair(Color(0xFFD0E8F8), Color(0xFFE4F2FB)),
    modeWeak    = ColorPair(Color(0xFFF5E0E0), Color(0xFFFAEEEE)),
    modeMastery = ColorPair(Color(0xFFD4EDD0), Color(0xFFE8F5E2)),
    onLightTile   = Color(0xFF2A2D27),
    onDarkTile    = Color(0xFFE8E4D9),
    textSecondary = Color(0xFF757575),
    xpGainText    = SharedXpGainText,
    starFilled = SharedStarFilled,
    starEmpty  = SharedStarEmpty,
    tone1       = SharedTone1,
    tone2       = SharedTone2,
    tone3       = SharedTone3,
    tone4       = SharedTone4,
    toneNeutral = SharedToneNeutral,
    toneTile1       = SharedToneTile1,
    toneTile2       = SharedToneTile2,
    toneTile3       = SharedToneTile3,
    toneTile4       = SharedToneTile4,
    toneTileNeutral = SharedToneTileNeutral,
    toneTileCorrect = SharedToneTileCorrect,
    toneTileWrong   = SharedToneTileWrong,
    toneTileIdle    = SharedToneTileIdle,
    masteryLow  = SharedMasteryLow,
    masteryMid  = SharedMasteryMid,
    masteryHigh = SharedMasteryHigh,
    masteryLowGradient  = SharedMasteryLowGrad,
    masteryMidGradient  = SharedMasteryMidGrad,
    masteryHighGradient = SharedMasteryHighGrad,
    achievementUnlocked     = ColorPair(Color(0xFFD4EDD0), Color(0xFFE8F5E2)),
    achievementLocked       = ColorPair(Color(0xFFEEEEEE), Color(0xFFF5F5F5)),
    achievementUnlockedText = Color(0xFF388E3C),
)

private val SageMd3 = lightColorScheme(
    primary                = Color(0xFF386A34),
    onPrimary              = Color(0xFFFFFFFF),
    primaryContainer       = Color(0xFFBAF2B0),
    onPrimaryContainer     = Color(0xFF002204),
    secondary              = Color(0xFF4A6780),
    onSecondary            = Color(0xFFFFFFFF),
    secondaryContainer     = Color(0xFFD0E8F8),
    onSecondaryContainer   = Color(0xFF021D2E),
    tertiary               = Color(0xFF7A5C30),
    onTertiary             = Color(0xFFFFFFFF),
    tertiaryContainer      = Color(0xFFFFDDB5),
    onTertiaryContainer    = Color(0xFF2A1600),
    error                  = Color(0xFFBA1A1A),
    onError                = Color(0xFFFFFFFF),
    background             = Color(0xFFFBF9F4),
    onBackground           = Color(0xFF31332E),
    surface                = Color(0xFFFBF9F4),
    onSurface              = Color(0xFF31332E),
    surfaceVariant         = Color(0xFFE4E3DA),
    onSurfaceVariant       = Color(0xFF4A4C47),
    outline                = Color(0xFF7A7D76),
    outlineVariant         = Color(0xFFC9CBC5),
)

// ── 2. Ocean ─────────────────────────────────────────────────────────────────
val OceanPalette = AppColorScheme(
    tileBlue    = ColorPair(Color(0xFFB2EBF2), Color(0xFFDEF6FA)),
    tileGreen   = ColorPair(Color(0xFFB2DFDB), Color(0xFFCEECE8)),
    tileAmber   = ColorPair(Color(0xFFFFF9C4), Color(0xFFFEFDE8)),
    tilePurple  = ColorPair(Color(0xFFC5CAE9), Color(0xFFDEE0F5)),
    tileGrey    = ColorPair(Color(0xFFECEFF1), Color(0xFFF5F7F8)),
    actionPositive = SharedActionPositive,
    actionNegative = SharedActionNegative,
    actionNeutral  = SharedActionNeutral,
    answerCorrect     = SharedAnswerCorrect,
    answerWrongSoft   = SharedAnswerWrongSoft,
    answerCorrectText = SharedAnswerCorrectText,
    answerWrongText   = SharedAnswerWrongText,
    categoryEssentials      = ColorPair(Color(0xFFB2EBF2), Color(0xFFD4F5F9)), // cyan
    categoryAtSchool        = ColorPair(Color(0xFFB2DFDB), Color(0xFFCCEAE7)), // teal-green
    categorySchoolSubjects  = ColorPair(Color(0xFFC5CAE9), Color(0xFFDDE0F4)), // indigo
    categoryFoodAndEating   = ColorPair(Color(0xFFFFF9C4), Color(0xFFFEFDE8)), // yellow
    categoryFeelingsHealth  = ColorPair(Color(0xFFFFCCBC), Color(0xFFFFE5D9)), // salmon
    categoryPlayHobbies     = ColorPair(Color(0xFFD8EDA0), Color(0xFFE8F6B8)), // lime
    categoryHome            = ColorPair(Color(0xFF80DEEA), Color(0xFFB2EBF2)), // deeper cyan/teal
    categoryOutAndAbout     = ColorPair(Color(0xFFD8D0F4), Color(0xFFE6E0FA)), // lavender
    categoryDefault         = ColorPair(Color(0xFFECF4F5), Color(0xFFECF4F5)),
    practiceTypeDefault   = ColorPair(Color(0xFFB2EBF2), Color(0xFFD4F5F9)),
    practiceTypeListening = ColorPair(Color(0xFF80DEEA), Color(0xFFB2EBF2)),
    practiceTypeReading   = ColorPair(Color(0xFFC5CAE9), Color(0xFFDDE0F4)),
    modeAll     = ColorPair(Color(0xFFB2EBF2), Color(0xFFD4F5F9)),
    modeWeak    = ColorPair(Color(0xFFF5E0E0), Color(0xFFFAEEEE)),
    modeMastery = ColorPair(Color(0xFFB2DFDB), Color(0xFFCEECE8)),
    onLightTile   = Color(0xFF1A2526),
    onDarkTile    = Color(0xFFE6F4F5),
    textSecondary = Color(0xFF607D8B),
    xpGainText    = SharedXpGainText,
    starFilled = SharedStarFilled,
    starEmpty  = SharedStarEmpty,
    tone1       = SharedTone1,
    tone2       = SharedTone2,
    tone3       = SharedTone3,
    tone4       = SharedTone4,
    toneNeutral = SharedToneNeutral,
    toneTile1       = SharedToneTile1,
    toneTile2       = SharedToneTile2,
    toneTile3       = SharedToneTile3,
    toneTile4       = SharedToneTile4,
    toneTileNeutral = SharedToneTileNeutral,
    toneTileCorrect = SharedToneTileCorrect,
    toneTileWrong   = SharedToneTileWrong,
    toneTileIdle    = SharedToneTileIdle,
    masteryLow  = SharedMasteryLow,
    masteryMid  = SharedMasteryMid,
    masteryHigh = SharedMasteryHigh,
    masteryLowGradient  = SharedMasteryLowGrad,
    masteryMidGradient  = SharedMasteryMidGrad,
    masteryHighGradient = SharedMasteryHighGrad,
    achievementUnlocked     = ColorPair(Color(0xFFB2DFDB), Color(0xFFCEECE8)),
    achievementLocked       = ColorPair(Color(0xFFECEFF1), Color(0xFFF5F7F8)),
    achievementUnlockedText = Color(0xFF00695C),
)

private val OceanMd3 = lightColorScheme(
    primary                = Color(0xFF005F73),
    onPrimary              = Color(0xFFFFFFFF),
    primaryContainer       = Color(0xFFA2D2DB),
    onPrimaryContainer     = Color(0xFF001E24),
    secondary              = Color(0xFF3D6B84),
    onSecondary            = Color(0xFFFFFFFF),
    secondaryContainer     = Color(0xFFC0DDE8),
    onSecondaryContainer   = Color(0xFF001E2C),
    tertiary               = Color(0xFF2E6B55),
    onTertiary             = Color(0xFFFFFFFF),
    tertiaryContainer      = Color(0xFFACF0C6),
    onTertiaryContainer    = Color(0xFF00210F),
    error                  = Color(0xFFBA1A1A),
    onError                = Color(0xFFFFFFFF),
    background             = Color(0xFFF0FAFB),
    onBackground           = Color(0xFF181C1D),
    surface                = Color(0xFFF0FAFB),
    onSurface              = Color(0xFF181C1D),
    surfaceVariant         = Color(0xFFD6E4E8),
    onSurfaceVariant       = Color(0xFF3F5057),
    outline                = Color(0xFF6E8C94),
    outlineVariant         = Color(0xFFBAD0D6),
)

// ── 3. Sunset ────────────────────────────────────────────────────────────────
val SunsetPalette = AppColorScheme(
    tileBlue    = ColorPair(Color(0xFFFFCCBC), Color(0xFFFFE5D9)),
    tileGreen   = ColorPair(Color(0xFFDCEDC8), Color(0xFFEEF6E2)),
    tileAmber   = ColorPair(Color(0xFFFFE082), Color(0xFFFFF3E0)),
    tilePurple  = ColorPair(Color(0xFFF8BBD0), Color(0xFFFDE8EE)),
    tileGrey    = ColorPair(Color(0xFFF3ECE6), Color(0xFFF8F4F0)),
    actionPositive = SharedActionPositive,
    actionNegative = SharedActionNegative,
    actionNeutral  = SharedActionNeutral,
    answerCorrect     = SharedAnswerCorrect,
    answerWrongSoft   = SharedAnswerWrongSoft,
    answerCorrectText = SharedAnswerCorrectText,
    answerWrongText   = SharedAnswerWrongText,
    categoryEssentials      = ColorPair(Color(0xFFFFCCBC), Color(0xFFFFE5D9)), // peach
    categoryAtSchool        = ColorPair(Color(0xFFDCEDC8), Color(0xFFEEF6E2)), // green
    categorySchoolSubjects  = ColorPair(Color(0xFFF8BBD0), Color(0xFFFDE8EE)), // pink/rose
    categoryFoodAndEating   = ColorPair(Color(0xFFFFE082), Color(0xFFFFF3E0)), // amber
    categoryFeelingsHealth  = ColorPair(Color(0xFFFFAB91), Color(0xFFFFCCBC)), // coral
    categoryPlayHobbies     = ColorPair(Color(0xFFD8EDA8), Color(0xFFE8F5C0)), // lime
    categoryHome            = ColorPair(Color(0xFFB4D8F4), Color(0xFFD0E8FA)), // sky blue
    categoryOutAndAbout     = ColorPair(Color(0xFFD0C8F8), Color(0xFFDED8FC)), // lavender
    categoryDefault         = ColorPair(Color(0xFFF5EDE8), Color(0xFFF5EDE8)),
    practiceTypeDefault   = ColorPair(Color(0xFFFFCCBC), Color(0xFFFFE5D9)),
    practiceTypeListening = ColorPair(Color(0xFFFFCC80), Color(0xFFFFE0A0)),
    practiceTypeReading   = ColorPair(Color(0xFFF8BBD0), Color(0xFFFDE8EE)),
    modeAll     = ColorPair(Color(0xFFFFCCBC), Color(0xFFFFE5D9)),
    modeWeak    = ColorPair(Color(0xFFF5E0E0), Color(0xFFFAEEEE)),
    modeMastery = ColorPair(Color(0xFFDCEDC8), Color(0xFFEEF6E2)),
    onLightTile   = Color(0xFF2C1C14),
    onDarkTile    = Color(0xFFF5EDE8),
    textSecondary = Color(0xFF8D6E63),
    xpGainText    = SharedXpGainText,
    starFilled = SharedStarFilled,
    starEmpty  = SharedStarEmpty,
    tone1       = SharedTone1,
    tone2       = SharedTone2,
    tone3       = SharedTone3,
    tone4       = SharedTone4,
    toneNeutral = SharedToneNeutral,
    toneTile1       = SharedToneTile1,
    toneTile2       = SharedToneTile2,
    toneTile3       = SharedToneTile3,
    toneTile4       = SharedToneTile4,
    toneTileNeutral = SharedToneTileNeutral,
    toneTileCorrect = SharedToneTileCorrect,
    toneTileWrong   = SharedToneTileWrong,
    toneTileIdle    = SharedToneTileIdle,
    masteryLow  = SharedMasteryLow,
    masteryMid  = SharedMasteryMid,
    masteryHigh = SharedMasteryHigh,
    masteryLowGradient  = SharedMasteryLowGrad,
    masteryMidGradient  = SharedMasteryMidGrad,
    masteryHighGradient = SharedMasteryHighGrad,
    achievementUnlocked     = ColorPair(Color(0xFFDCEDC8), Color(0xFFEEF6E2)),
    achievementLocked       = ColorPair(Color(0xFFF3ECE6), Color(0xFFF8F4F0)),
    achievementUnlockedText = Color(0xFF558B2F),
)

private val SunsetMd3 = lightColorScheme(
    primary                = Color(0xFFC23B00),
    onPrimary              = Color(0xFFFFFFFF),
    primaryContainer       = Color(0xFFFFCCBC),
    onPrimaryContainer     = Color(0xFF3E0800),
    secondary              = Color(0xFF7D4E3C),
    onSecondary            = Color(0xFFFFFFFF),
    secondaryContainer     = Color(0xFFFFD8C8),
    onSecondaryContainer   = Color(0xFF2A1100),
    tertiary               = Color(0xFF7B5E00),
    onTertiary             = Color(0xFFFFFFFF),
    tertiaryContainer      = Color(0xFFFFE57F),
    onTertiaryContainer    = Color(0xFF251800),
    error                  = Color(0xFFBA1A1A),
    onError                = Color(0xFFFFFFFF),
    background             = Color(0xFFFFF8F2),
    onBackground           = Color(0xFF2C1C14),
    surface                = Color(0xFFFFF8F2),
    onSurface              = Color(0xFF2C1C14),
    surfaceVariant         = Color(0xFFF0DFD4),
    onSurfaceVariant       = Color(0xFF544036),
    outline                = Color(0xFF857268),
    outlineVariant         = Color(0xFFD8C2B4),
)

// ── 4. Forest ────────────────────────────────────────────────────────────────
val ForestPalette = AppColorScheme(
    tileBlue    = ColorPair(Color(0xFFA5D6A7), Color(0xFFC2E6C4)),
    tileGreen   = ColorPair(Color(0xFF80CBC4), Color(0xFFB0E0DC)),
    tileAmber   = ColorPair(Color(0xFFFFE0B2), Color(0xFFFFF3E0)),
    tilePurple  = ColorPair(Color(0xFFD7CCC8), Color(0xFFEFEBE9)),
    tileGrey    = ColorPair(Color(0xFFECEDE8), Color(0xFFF5F4F0)),
    actionPositive = SharedActionPositive,
    actionNegative = SharedActionNegative,
    actionNeutral  = SharedActionNeutral,
    answerCorrect     = SharedAnswerCorrect,
    answerWrongSoft   = SharedAnswerWrongSoft,
    answerCorrectText = SharedAnswerCorrectText,
    answerWrongText   = SharedAnswerWrongText,
    categoryEssentials      = ColorPair(Color(0xFFA5D6A7), Color(0xFFC2E6C4)), // light green
    categoryAtSchool        = ColorPair(Color(0xFF80CBC4), Color(0xFFB0E0DC)), // teal
    categorySchoolSubjects  = ColorPair(Color(0xFFD7CCC8), Color(0xFFEFEBE9)), // taupe
    categoryFoodAndEating   = ColorPair(Color(0xFFFFE0B2), Color(0xFFFFF3E0)), // amber
    categoryFeelingsHealth  = ColorPair(Color(0xFFFFAB91), Color(0xFFFFCCBC)), // salmon
    categoryPlayHobbies     = ColorPair(Color(0xFFDFF0A8), Color(0xFFECF8BC)), // lime
    categoryHome            = ColorPair(Color(0xFFB8D4F0), Color(0xFFD0E4F8)), // sky blue
    categoryOutAndAbout     = ColorPair(Color(0xFFCCC8F4), Color(0xFFDCDAFA)), // periwinkle
    categoryDefault         = ColorPair(Color(0xFFECEDE8), Color(0xFFECEDE8)),
    practiceTypeDefault   = ColorPair(Color(0xFFA5D6A7), Color(0xFFC2E6C4)),
    practiceTypeListening = ColorPair(Color(0xFF80CBC4), Color(0xFFB0E0DC)),
    practiceTypeReading   = ColorPair(Color(0xFFD7CCC8), Color(0xFFEFEBE9)),
    modeAll     = ColorPair(Color(0xFFA5D6A7), Color(0xFFC2E6C4)),
    modeWeak    = ColorPair(Color(0xFFF5E0E0), Color(0xFFFAEEEE)),
    modeMastery = ColorPair(Color(0xFF80CBC4), Color(0xFFB0E0DC)),
    onLightTile   = Color(0xFF0F1E10),
    onDarkTile    = Color(0xFFE8F2E8),
    textSecondary = Color(0xFF5D7157),
    xpGainText    = SharedXpGainText,
    starFilled = SharedStarFilled,
    starEmpty  = SharedStarEmpty,
    tone1       = SharedTone1,
    tone2       = SharedTone2,
    tone3       = SharedTone3,
    tone4       = SharedTone4,
    toneNeutral = SharedToneNeutral,
    toneTile1       = SharedToneTile1,
    toneTile2       = SharedToneTile2,
    toneTile3       = SharedToneTile3,
    toneTile4       = SharedToneTile4,
    toneTileNeutral = SharedToneTileNeutral,
    toneTileCorrect = SharedToneTileCorrect,
    toneTileWrong   = SharedToneTileWrong,
    toneTileIdle    = SharedToneTileIdle,
    masteryLow  = SharedMasteryLow,
    masteryMid  = SharedMasteryMid,
    masteryHigh = SharedMasteryHigh,
    masteryLowGradient  = SharedMasteryLowGrad,
    masteryMidGradient  = SharedMasteryMidGrad,
    masteryHighGradient = SharedMasteryHighGrad,
    achievementUnlocked     = ColorPair(Color(0xFFA5D6A7), Color(0xFFC2E6C4)),
    achievementLocked       = ColorPair(Color(0xFFECEDE8), Color(0xFFF5F4F0)),
    achievementUnlockedText = Color(0xFF1B5E20),
)

private val ForestMd3 = lightColorScheme(
    primary                = Color(0xFF1B5E20),
    onPrimary              = Color(0xFFFFFFFF),
    primaryContainer       = Color(0xFFA5D6A7),
    onPrimaryContainer     = Color(0xFF002105),
    secondary              = Color(0xFF5D4037),
    onSecondary            = Color(0xFFFFFFFF),
    secondaryContainer     = Color(0xFFD7CCC8),
    onSecondaryContainer   = Color(0xFF1C0A05),
    tertiary               = Color(0xFF7B5800),
    onTertiary             = Color(0xFFFFFFFF),
    tertiaryContainer      = Color(0xFFFFE082),
    onTertiaryContainer    = Color(0xFF251800),
    error                  = Color(0xFFBA1A1A),
    onError                = Color(0xFFFFFFFF),
    background             = Color(0xFFF1F8F0),
    onBackground           = Color(0xFF0F1E10),
    surface                = Color(0xFFF1F8F0),
    onSurface              = Color(0xFF0F1E10),
    surfaceVariant         = Color(0xFFD5E6D2),
    onSurfaceVariant       = Color(0xFF3A4D39),
    outline                = Color(0xFF6B856A),
    outlineVariant         = Color(0xFFB9CDB7),
)

// ── 5. Berry ─────────────────────────────────────────────────────────────────
val BerryPalette = AppColorScheme(
    tileBlue    = ColorPair(Color(0xFFE1BEE7), Color(0xFFF0DDF5)),
    tileGreen   = ColorPair(Color(0xFFF8BBD0), Color(0xFFFCE4EC)),
    tileAmber   = ColorPair(Color(0xFFFFE082), Color(0xFFFFF8E1)),
    tilePurple  = ColorPair(Color(0xFFCE93D8), Color(0xFFE4C4EC)),
    tileGrey    = ColorPair(Color(0xFFF0EBF5), Color(0xFFF7F3FA)),
    actionPositive = SharedActionPositive,
    actionNegative = SharedActionNegative,
    actionNeutral  = SharedActionNeutral,
    answerCorrect     = SharedAnswerCorrect,
    answerWrongSoft   = SharedAnswerWrongSoft,
    answerCorrectText = SharedAnswerCorrectText,
    answerWrongText   = SharedAnswerWrongText,
    categoryEssentials      = ColorPair(Color(0xFFE1BEE7), Color(0xFFF0DDF5)), // lavender
    categoryAtSchool        = ColorPair(Color(0xFFF8BBD0), Color(0xFFFCE4EC)), // pink
    categorySchoolSubjects  = ColorPair(Color(0xFFCE93D8), Color(0xFFE4C4EC)), // purple
    categoryFoodAndEating   = ColorPair(Color(0xFFFFE082), Color(0xFFFFF3E0)), // amber
    categoryFeelingsHealth  = ColorPair(Color(0xFFFFAB91), Color(0xFFFFCCBC)), // coral
    categoryPlayHobbies     = ColorPair(Color(0xFFD8F0A8), Color(0xFFE8F8BC)), // lime
    categoryHome            = ColorPair(Color(0xFFB8D8F8), Color(0xFFD0E8FC)), // sky blue
    categoryOutAndAbout     = ColorPair(Color(0xFFB0EDE8), Color(0xFFCCF5F2)), // teal/mint
    categoryDefault         = ColorPair(Color(0xFFF0EBF5), Color(0xFFF0EBF5)),
    practiceTypeDefault   = ColorPair(Color(0xFFE1BEE7), Color(0xFFF0DDF5)),
    practiceTypeListening = ColorPair(Color(0xFFB39DDB), Color(0xFFD1C4E9)),
    practiceTypeReading   = ColorPair(Color(0xFFF8BBD0), Color(0xFFFCE4EC)),
    modeAll     = ColorPair(Color(0xFFE1BEE7), Color(0xFFF0DDF5)),
    modeWeak    = ColorPair(Color(0xFFF5E0E0), Color(0xFFFAEEEE)),
    modeMastery = ColorPair(Color(0xFFC8E6C9), Color(0xFFDCF0DC)),
    onLightTile   = Color(0xFF1D0A27),
    onDarkTile    = Color(0xFFF5EEF8),
    textSecondary = Color(0xFF7B6889),
    xpGainText    = SharedXpGainText,
    starFilled = SharedStarFilled,
    starEmpty  = SharedStarEmpty,
    tone1       = SharedTone1,
    tone2       = SharedTone2,
    tone3       = SharedTone3,
    tone4       = SharedTone4,
    toneNeutral = SharedToneNeutral,
    toneTile1       = SharedToneTile1,
    toneTile2       = SharedToneTile2,
    toneTile3       = SharedToneTile3,
    toneTile4       = SharedToneTile4,
    toneTileNeutral = SharedToneTileNeutral,
    toneTileCorrect = SharedToneTileCorrect,
    toneTileWrong   = SharedToneTileWrong,
    toneTileIdle    = SharedToneTileIdle,
    masteryLow  = SharedMasteryLow,
    masteryMid  = SharedMasteryMid,
    masteryHigh = SharedMasteryHigh,
    masteryLowGradient  = SharedMasteryLowGrad,
    masteryMidGradient  = SharedMasteryMidGrad,
    masteryHighGradient = SharedMasteryHighGrad,
    achievementUnlocked     = ColorPair(Color(0xFFCE93D8), Color(0xFFE4C4EC)),
    achievementLocked       = ColorPair(Color(0xFFF0EBF5), Color(0xFFF7F3FA)),
    achievementUnlockedText = Color(0xFF6A1B9A),
)

private val BerryMd3 = lightColorScheme(
    primary                = Color(0xFF6A1B9A),
    onPrimary              = Color(0xFFFFFFFF),
    primaryContainer       = Color(0xFFCE93D8),
    onPrimaryContainer     = Color(0xFF1A0035),
    secondary              = Color(0xFFAD1457),
    onSecondary            = Color(0xFFFFFFFF),
    secondaryContainer     = Color(0xFFF8BBD0),
    onSecondaryContainer   = Color(0xFF3A0016),
    tertiary               = Color(0xFF6D4C41),
    onTertiary             = Color(0xFFFFFFFF),
    tertiaryContainer      = Color(0xFFD7CCC8),
    onTertiaryContainer    = Color(0xFF180B07),
    error                  = Color(0xFFBA1A1A),
    onError                = Color(0xFFFFFFFF),
    background             = Color(0xFFFDF5FF),
    onBackground           = Color(0xFF1D0A27),
    surface                = Color(0xFFFDF5FF),
    onSurface              = Color(0xFF1D0A27),
    surfaceVariant         = Color(0xFFEADFF0),
    onSurfaceVariant       = Color(0xFF4A3856),
    outline                = Color(0xFF7B6989),
    outlineVariant         = Color(0xFFD4C5DF),
)

// ── Dark-theme shared semantic constants ──────────────────────────────────────
// These differ from light-mode shared constants to suit dark backgrounds:
//   • Action/answer button gradients are deeper (less harsh on dark screens)
//   • Answer feedback card background is dark-tinted, not bright pink
//   • Correct/wrong text colours are lightened for legibility on dark tiles
//   • Tone tile gradients are dark-saturated (immersive but not harsh)
//   • Mastery gradients are deeply coloured darks (red/amber/green semantic kept)
private val DarkActionPositive   = ColorPair(Color(0xFF1E7A1E), Color(0xFF2E9A2E))
private val DarkActionNegative   = ColorPair(Color(0xFF9A1818), Color(0xFFC02828))
private val DarkActionNeutral    = ColorPair(Color(0xFF383838), Color(0xFF484848))
private val DarkAnswerCorrect    = ColorPair(Color(0xFF1E7A1E), Color(0xFF2E9A2E))
private val DarkAnswerWrongSoft  = ColorPair(Color(0xFF3C1010), Color(0xFF4C1A1A))
private val DarkAnswerCorrectText = Color(0xFF80D878)
private val DarkAnswerWrongText   = Color(0xFFF08888)
private val DarkStarFilled = Color(0xFFFFB820)
private val DarkStarEmpty  = Color(0xFF4E4E4E)
private val DarkTone1       = Color(0xFFFF7070)
private val DarkTone2       = Color(0xFFFFB060)
private val DarkTone3       = Color(0xFF68D068)
private val DarkTone4       = Color(0xFF60A8F0)
private val DarkToneNeutral = Color(0xFF9A9A9A)
private val DarkToneTile1       = ColorPair(Color(0xFF5C1414), Color(0xFF8C2020))
private val DarkToneTile2       = ColorPair(Color(0xFF5C2C06), Color(0xFF8C4610))
private val DarkToneTile3       = ColorPair(Color(0xFF0E3810), Color(0xFF1A5C18))
private val DarkToneTile4       = ColorPair(Color(0xFF0C1A5C), Color(0xFF162E8C))
private val DarkToneTileNeutral = ColorPair(Color(0xFF2C2C2C), Color(0xFF3C3C3C))
private val DarkToneTileCorrect = ColorPair(Color(0xFF0E3810), Color(0xFF1A5C18))
private val DarkToneTileWrong   = ColorPair(Color(0xFF5C0C0C), Color(0xFF8C1A1A))
private val DarkToneTileIdle    = ColorPair(Color(0xFF282828), Color(0xFF383838))
private val DarkMasteryLow  = Color(0xFFE05C5C)
private val DarkMasteryMid  = Color(0xFFE09C40)
private val DarkMasteryHigh = Color(0xFF5CC85C)
private val DarkMasteryLowGrad  = ColorPair(Color(0xFF4A1010), Color(0xFF6A1E1E))
private val DarkMasteryMidGrad  = ColorPair(Color(0xFF4A2C00), Color(0xFF6A4200))
private val DarkMasteryHighGrad = ColorPair(Color(0xFF0E3C10), Color(0xFF1A5E1A))
private val DarkXpGainText = Color(0xFF70D070)

// ── 6. Night ─────────────────────────────────────────────────────────────────
val NightPalette = AppColorScheme(
    tileBlue    = ColorPair(Color(0xFF1C2C36), Color(0xFF243644)),
    tileGreen   = ColorPair(Color(0xFF182C18), Color(0xFF203820)),
    tileAmber   = ColorPair(Color(0xFF2A2610), Color(0xFF362E14)),
    tilePurple  = ColorPair(Color(0xFF222035), Color(0xFF2C2842)),
    tileGrey    = ColorPair(Color(0xFF222422), Color(0xFF2C2E2A)),
    actionPositive = DarkActionPositive,
    actionNegative = DarkActionNegative,
    actionNeutral  = DarkActionNeutral,
    answerCorrect     = DarkAnswerCorrect,
    answerWrongSoft   = DarkAnswerWrongSoft,
    answerCorrectText = DarkAnswerCorrectText,
    answerWrongText   = DarkAnswerWrongText,
    categoryEssentials      = ColorPair(Color(0xFF1C2C36), Color(0xFF243644)), // dark blue
    categoryAtSchool        = ColorPair(Color(0xFF182C18), Color(0xFF203820)), // dark green
    categorySchoolSubjects  = ColorPair(Color(0xFF222035), Color(0xFF2C2842)), // dark purple
    categoryFoodAndEating   = ColorPair(Color(0xFF2A2610), Color(0xFF362E14)), // dark amber
    categoryFeelingsHealth  = ColorPair(Color(0xFF2C1A1A), Color(0xFF381E1E)), // dark rose
    categoryPlayHobbies     = ColorPair(Color(0xFF262C0C), Color(0xFF303814)), // dark olive/lime
    categoryHome            = ColorPair(Color(0xFF182830), Color(0xFF20323C)), // dark teal
    categoryOutAndAbout     = ColorPair(Color(0xFF1C1C38), Color(0xFF242442)), // dark indigo
    categoryDefault         = ColorPair(Color(0xFF222422), Color(0xFF2C2E2A)),
    practiceTypeDefault   = ColorPair(Color(0xFF1C2C36), Color(0xFF243644)),
    practiceTypeListening = ColorPair(Color(0xFF182830), Color(0xFF20323C)),
    practiceTypeReading   = ColorPair(Color(0xFF222035), Color(0xFF2C2842)),
    modeAll     = ColorPair(Color(0xFF1C2C36), Color(0xFF243644)),
    modeWeak    = ColorPair(Color(0xFF2A1414), Color(0xFF361818)),
    modeMastery = ColorPair(Color(0xFF182C18), Color(0xFF203820)),
    onLightTile   = Color(0xFFD4DDD4),
    onDarkTile    = Color(0xFFD4DDD4),
    textSecondary = Color(0xFF8A928A),
    xpGainText    = DarkXpGainText,
    starFilled = DarkStarFilled,
    starEmpty  = DarkStarEmpty,
    tone1       = DarkTone1,
    tone2       = DarkTone2,
    tone3       = DarkTone3,
    tone4       = DarkTone4,
    toneNeutral = DarkToneNeutral,
    toneTile1       = DarkToneTile1,
    toneTile2       = DarkToneTile2,
    toneTile3       = DarkToneTile3,
    toneTile4       = DarkToneTile4,
    toneTileNeutral = DarkToneTileNeutral,
    toneTileCorrect = DarkToneTileCorrect,
    toneTileWrong   = DarkToneTileWrong,
    toneTileIdle    = DarkToneTileIdle,
    masteryLow  = DarkMasteryLow,
    masteryMid  = DarkMasteryMid,
    masteryHigh = DarkMasteryHigh,
    masteryLowGradient  = DarkMasteryLowGrad,
    masteryMidGradient  = DarkMasteryMidGrad,
    masteryHighGradient = DarkMasteryHighGrad,
    achievementUnlocked     = ColorPair(Color(0xFF1A2E1A), Color(0xFF243824)),
    achievementLocked       = ColorPair(Color(0xFF222422), Color(0xFF2C2E2A)),
    achievementUnlockedText = Color(0xFF70C870),
)

private val NightMd3 = androidx.compose.material3.darkColorScheme(
    primary                = Color(0xFF7EC870),
    onPrimary              = Color(0xFF1C3A1A),
    primaryContainer       = Color(0xFF2E5A2C),
    onPrimaryContainer     = Color(0xFFB0E4A8),
    secondary              = Color(0xFF7EB0C8),
    onSecondary            = Color(0xFF1A2E3A),
    secondaryContainer     = Color(0xFF264858),
    onSecondaryContainer   = Color(0xFFA8D4E8),
    tertiary               = Color(0xFFCBA860),
    onTertiary             = Color(0xFF2A2008),
    tertiaryContainer      = Color(0xFF3A3010),
    onTertiaryContainer    = Color(0xFFF0D890),
    error                  = Color(0xFFCF6679),
    onError                = Color(0xFF2C0A0E),
    background             = Color(0xFF1C1E1A),
    onBackground           = Color(0xFFD4DDD4),
    surface                = Color(0xFF1C1E1A),
    onSurface              = Color(0xFFD4DDD4),
    surfaceVariant         = Color(0xFF262822),
    onSurfaceVariant       = Color(0xFFA4ACA4),
    outline                = Color(0xFF6A726A),
    outlineVariant         = Color(0xFF3A3E3A),
)

// ── 7. Midnight ───────────────────────────────────────────────────────────────
val MidnightPalette = AppColorScheme(
    tileBlue    = ColorPair(Color(0xFF142030), Color(0xFF1C2C40)),
    tileGreen   = ColorPair(Color(0xFF0C2422), Color(0xFF122E2C)),
    tileAmber   = ColorPair(Color(0xFF241C0C), Color(0xFF302610)),
    tilePurple  = ColorPair(Color(0xFF14163A), Color(0xFF1C204C)),
    tileGrey    = ColorPair(Color(0xFF1A1E28), Color(0xFF22263A)),
    actionPositive = DarkActionPositive,
    actionNegative = DarkActionNegative,
    actionNeutral  = DarkActionNeutral,
    answerCorrect     = DarkAnswerCorrect,
    answerWrongSoft   = DarkAnswerWrongSoft,
    answerCorrectText = DarkAnswerCorrectText,
    answerWrongText   = DarkAnswerWrongText,
    categoryEssentials      = ColorPair(Color(0xFF142030), Color(0xFF1C2C40)), // dark navy
    categoryAtSchool        = ColorPair(Color(0xFF0C2422), Color(0xFF122E2C)), // dark teal-green
    categorySchoolSubjects  = ColorPair(Color(0xFF14163A), Color(0xFF1C204C)), // dark indigo
    categoryFoodAndEating   = ColorPair(Color(0xFF241C0C), Color(0xFF302610)), // dark amber
    categoryFeelingsHealth  = ColorPair(Color(0xFF261418), Color(0xFF32181E)), // dark rose
    categoryPlayHobbies     = ColorPair(Color(0xFF1E2408), Color(0xFF282E10)), // dark olive
    categoryHome            = ColorPair(Color(0xFF0E2030), Color(0xFF142A3C)), // dark teal
    categoryOutAndAbout     = ColorPair(Color(0xFF16183A), Color(0xFF1E2048)), // dark violet
    categoryDefault         = ColorPair(Color(0xFF1A1E28), Color(0xFF22263A)),
    practiceTypeDefault   = ColorPair(Color(0xFF142030), Color(0xFF1C2C40)),
    practiceTypeListening = ColorPair(Color(0xFF0E2030), Color(0xFF142A3C)),
    practiceTypeReading   = ColorPair(Color(0xFF14163A), Color(0xFF1C204C)),
    modeAll     = ColorPair(Color(0xFF142030), Color(0xFF1C2C40)),
    modeWeak    = ColorPair(Color(0xFF281014), Color(0xFF34141A)),
    modeMastery = ColorPair(Color(0xFF0C2422), Color(0xFF122E2C)),
    onLightTile   = Color(0xFFCCD8F0),
    onDarkTile    = Color(0xFFCCD8F0),
    textSecondary = Color(0xFF7A90A8),
    xpGainText    = DarkXpGainText,
    starFilled = DarkStarFilled,
    starEmpty  = DarkStarEmpty,
    tone1       = DarkTone1,
    tone2       = DarkTone2,
    tone3       = DarkTone3,
    tone4       = DarkTone4,
    toneNeutral = DarkToneNeutral,
    toneTile1       = DarkToneTile1,
    toneTile2       = DarkToneTile2,
    toneTile3       = DarkToneTile3,
    toneTile4       = DarkToneTile4,
    toneTileNeutral = DarkToneTileNeutral,
    toneTileCorrect = DarkToneTileCorrect,
    toneTileWrong   = DarkToneTileWrong,
    toneTileIdle    = DarkToneTileIdle,
    masteryLow  = DarkMasteryLow,
    masteryMid  = DarkMasteryMid,
    masteryHigh = DarkMasteryHigh,
    masteryLowGradient  = DarkMasteryLowGrad,
    masteryMidGradient  = DarkMasteryMidGrad,
    masteryHighGradient = DarkMasteryHighGrad,
    achievementUnlocked     = ColorPair(Color(0xFF0C2422), Color(0xFF122E2C)),
    achievementLocked       = ColorPair(Color(0xFF1A1E28), Color(0xFF22263A)),
    achievementUnlockedText = Color(0xFF68C880),
)

private val MidnightMd3 = androidx.compose.material3.darkColorScheme(
    primary                = Color(0xFF6898E0),
    onPrimary              = Color(0xFF0E1E3A),
    primaryContainer       = Color(0xFF1A2E52),
    onPrimaryContainer     = Color(0xFFA8C8F0),
    secondary              = Color(0xFF60B8C8),
    onSecondary            = Color(0xFF082030),
    secondaryContainer     = Color(0xFF103040),
    onSecondaryContainer   = Color(0xFF98D8E8),
    tertiary               = Color(0xFFA888D0),
    onTertiary             = Color(0xFF180E30),
    tertiaryContainer      = Color(0xFF241848),
    onTertiaryContainer    = Color(0xFFD8C0F0),
    error                  = Color(0xFFCF6679),
    onError                = Color(0xFF2C0A0E),
    background             = Color(0xFF0E1420),
    onBackground           = Color(0xFFCCD8F0),
    surface                = Color(0xFF0E1420),
    onSurface              = Color(0xFFCCD8F0),
    surfaceVariant         = Color(0xFF18202E),
    onSurfaceVariant       = Color(0xFF9AB0C8),
    outline                = Color(0xFF5A6E88),
    outlineVariant         = Color(0xFF2A3444),
)

// ── 8. Ember ──────────────────────────────────────────────────────────────────
val EmberPalette = AppColorScheme(
    tileBlue    = ColorPair(Color(0xFF2E1C0C), Color(0xFF3C2610)),
    tileGreen   = ColorPair(Color(0xFF101C0E), Color(0xFF162812)),
    tileAmber   = ColorPair(Color(0xFF2A1A06), Color(0xFF38240A)),
    tilePurple  = ColorPair(Color(0xFF200C0E), Color(0xFF2C1012)),
    tileGrey    = ColorPair(Color(0xFF26201A), Color(0xFF302822)),
    actionPositive = DarkActionPositive,
    actionNegative = DarkActionNegative,
    actionNeutral  = DarkActionNeutral,
    answerCorrect     = DarkAnswerCorrect,
    answerWrongSoft   = DarkAnswerWrongSoft,
    answerCorrectText = DarkAnswerCorrectText,
    answerWrongText   = DarkAnswerWrongText,
    categoryEssentials      = ColorPair(Color(0xFF2E1C0C), Color(0xFF3C2610)), // dark amber/gold
    categoryAtSchool        = ColorPair(Color(0xFF101C0E), Color(0xFF162812)), // dark green
    categorySchoolSubjects  = ColorPair(Color(0xFF200C0E), Color(0xFF2C1012)), // dark crimson
    categoryFoodAndEating   = ColorPair(Color(0xFF2A1A06), Color(0xFF38240A)), // dark orange
    categoryFeelingsHealth  = ColorPair(Color(0xFF2A0E0E), Color(0xFF361212)), // dark red
    categoryPlayHobbies     = ColorPair(Color(0xFF22240A), Color(0xFF2E2E10)), // dark olive
    categoryHome            = ColorPair(Color(0xFF0A1A18), Color(0xFF102422)), // dark teal
    categoryOutAndAbout     = ColorPair(Color(0xFF14103A), Color(0xFF1C1A48)), // dark indigo
    categoryDefault         = ColorPair(Color(0xFF26201A), Color(0xFF302822)),
    practiceTypeDefault   = ColorPair(Color(0xFF2E1C0C), Color(0xFF3C2610)),
    practiceTypeListening = ColorPair(Color(0xFF0E1C1A), Color(0xFF142624)),
    practiceTypeReading   = ColorPair(Color(0xFF200C0E), Color(0xFF2C1012)),
    modeAll     = ColorPair(Color(0xFF2E1C0C), Color(0xFF3C2610)),
    modeWeak    = ColorPair(Color(0xFF260A0A), Color(0xFF320C0C)),
    modeMastery = ColorPair(Color(0xFF101C0E), Color(0xFF162812)),
    onLightTile   = Color(0xFFF0E0D0),
    onDarkTile    = Color(0xFFF0E0D0),
    textSecondary = Color(0xFFA09080),
    xpGainText    = Color(0xFFD0A860),
    starFilled = DarkStarFilled,
    starEmpty  = DarkStarEmpty,
    tone1       = DarkTone1,
    tone2       = DarkTone2,
    tone3       = DarkTone3,
    tone4       = DarkTone4,
    toneNeutral = DarkToneNeutral,
    toneTile1       = DarkToneTile1,
    toneTile2       = DarkToneTile2,
    toneTile3       = DarkToneTile3,
    toneTile4       = DarkToneTile4,
    toneTileNeutral = DarkToneTileNeutral,
    toneTileCorrect = DarkToneTileCorrect,
    toneTileWrong   = DarkToneTileWrong,
    toneTileIdle    = DarkToneTileIdle,
    masteryLow  = DarkMasteryLow,
    masteryMid  = DarkMasteryMid,
    masteryHigh = DarkMasteryHigh,
    masteryLowGradient  = DarkMasteryLowGrad,
    masteryMidGradient  = DarkMasteryMidGrad,
    masteryHighGradient = DarkMasteryHighGrad,
    achievementUnlocked     = ColorPair(Color(0xFF101C0E), Color(0xFF1A2C14)),
    achievementLocked       = ColorPair(Color(0xFF26201A), Color(0xFF302822)),
    achievementUnlockedText = Color(0xFF88C068),
)

private val EmberMd3 = androidx.compose.material3.darkColorScheme(
    primary                = Color(0xFFE0904A),
    onPrimary              = Color(0xFF2C1400),
    primaryContainer       = Color(0xFF3C2008),
    onPrimaryContainer     = Color(0xFFF0C898),
    secondary              = Color(0xFFC87850),
    onSecondary            = Color(0xFF200C00),
    secondaryContainer     = Color(0xFF2C1408),
    onSecondaryContainer   = Color(0xFFE8B898),
    tertiary               = Color(0xFFC8A830),
    onTertiary             = Color(0xFF201800),
    tertiaryContainer      = Color(0xFF302200),
    onTertiaryContainer    = Color(0xFFE8D080),
    error                  = Color(0xFFCF6679),
    onError                = Color(0xFF2C0A0E),
    background             = Color(0xFF1E1208),
    onBackground           = Color(0xFFF0E0D0),
    surface                = Color(0xFF1E1208),
    onSurface              = Color(0xFFF0E0D0),
    surfaceVariant         = Color(0xFF2A1C10),
    onSurfaceVariant       = Color(0xFFC0A890),
    outline                = Color(0xFF806050),
    outlineVariant         = Color(0xFF3C2818),
)

// ── 9. Jungle ─────────────────────────────────────────────────────────────────
val JunglePalette = AppColorScheme(
    tileBlue    = ColorPair(Color(0xFF0E2020), Color(0xFF142A28)),
    tileGreen   = ColorPair(Color(0xFF102410), Color(0xFF162E16)),
    tileAmber   = ColorPair(Color(0xFF201C08), Color(0xFF2A260C)),
    tilePurple  = ColorPair(Color(0xFF1A1A0C), Color(0xFF222212)),
    tileGrey    = ColorPair(Color(0xFF182018), Color(0xFF1E2A1E)),
    actionPositive = DarkActionPositive,
    actionNegative = DarkActionNegative,
    actionNeutral  = DarkActionNeutral,
    answerCorrect     = DarkAnswerCorrect,
    answerWrongSoft   = DarkAnswerWrongSoft,
    answerCorrectText = DarkAnswerCorrectText,
    answerWrongText   = DarkAnswerWrongText,
    categoryEssentials      = ColorPair(Color(0xFF0E2020), Color(0xFF142A28)), // dark teal
    categoryAtSchool        = ColorPair(Color(0xFF102410), Color(0xFF162E16)), // dark green
    categorySchoolSubjects  = ColorPair(Color(0xFF180C2A), Color(0xFF201434)), // dark violet
    categoryFoodAndEating   = ColorPair(Color(0xFF201C08), Color(0xFF2A260C)), // dark olive-amber
    categoryFeelingsHealth  = ColorPair(Color(0xFF221410), Color(0xFF2C1A14)), // dark terracotta
    categoryPlayHobbies     = ColorPair(Color(0xFF1C2A08), Color(0xFF24340E)), // dark lime
    categoryHome            = ColorPair(Color(0xFF0C1E1C), Color(0xFF122826)), // dark teal (deeper)
    categoryOutAndAbout     = ColorPair(Color(0xFF0C1028), Color(0xFF121830)), // dark navy
    categoryDefault         = ColorPair(Color(0xFF182018), Color(0xFF1E2A1E)),
    practiceTypeDefault   = ColorPair(Color(0xFF0E2020), Color(0xFF142A28)),
    practiceTypeListening = ColorPair(Color(0xFF0C1E1C), Color(0xFF122826)),
    practiceTypeReading   = ColorPair(Color(0xFF1A1A0C), Color(0xFF222212)),
    modeAll     = ColorPair(Color(0xFF0E2020), Color(0xFF142A28)),
    modeWeak    = ColorPair(Color(0xFF220E0E), Color(0xFF2C1212)),
    modeMastery = ColorPair(Color(0xFF102410), Color(0xFF162E16)),
    onLightTile   = Color(0xFFCCE0CC),
    onDarkTile    = Color(0xFFCCE0CC),
    textSecondary = Color(0xFF789078),
    xpGainText    = DarkXpGainText,
    starFilled = DarkStarFilled,
    starEmpty  = DarkStarEmpty,
    tone1       = DarkTone1,
    tone2       = DarkTone2,
    tone3       = DarkTone3,
    tone4       = DarkTone4,
    toneNeutral = DarkToneNeutral,
    toneTile1       = DarkToneTile1,
    toneTile2       = DarkToneTile2,
    toneTile3       = DarkToneTile3,
    toneTile4       = DarkToneTile4,
    toneTileNeutral = DarkToneTileNeutral,
    toneTileCorrect = DarkToneTileCorrect,
    toneTileWrong   = DarkToneTileWrong,
    toneTileIdle    = DarkToneTileIdle,
    masteryLow  = DarkMasteryLow,
    masteryMid  = DarkMasteryMid,
    masteryHigh = DarkMasteryHigh,
    masteryLowGradient  = DarkMasteryLowGrad,
    masteryMidGradient  = DarkMasteryMidGrad,
    masteryHighGradient = DarkMasteryHighGrad,
    achievementUnlocked     = ColorPair(Color(0xFF102410), Color(0xFF162E16)),
    achievementLocked       = ColorPair(Color(0xFF182018), Color(0xFF1E2A1E)),
    achievementUnlockedText = Color(0xFF68C870),
)

private val JungleMd3 = androidx.compose.material3.darkColorScheme(
    primary                = Color(0xFF68C878),
    onPrimary              = Color(0xFF0A2810),
    primaryContainer       = Color(0xFF123818),
    onPrimaryContainer     = Color(0xFFA8E8B8),
    secondary              = Color(0xFF60A898),
    onSecondary            = Color(0xFF082018),
    secondaryContainer     = Color(0xFF102E28),
    onSecondaryContainer   = Color(0xFF98D8C8),
    tertiary               = Color(0xFFB8A840),
    onTertiary             = Color(0xFF181800),
    tertiaryContainer      = Color(0xFF262400),
    onTertiaryContainer    = Color(0xFFE0D880),
    error                  = Color(0xFFCF6679),
    onError                = Color(0xFF2C0A0E),
    background             = Color(0xFF0C1A0E),
    onBackground           = Color(0xFFCCE0CC),
    surface                = Color(0xFF0C1A0E),
    onSurface              = Color(0xFFCCE0CC),
    surfaceVariant         = Color(0xFF162216),
    onSurfaceVariant       = Color(0xFF96B896),
    outline                = Color(0xFF527852),
    outlineVariant         = Color(0xFF243A24),
)

// ── 10. Dusk ──────────────────────────────────────────────────────────────────
val DuskPalette = AppColorScheme(
    tileBlue    = ColorPair(Color(0xFF1C1240), Color(0xFF26184E)),
    tileGreen   = ColorPair(Color(0xFF1C0C2A), Color(0xFF261238)),
    tileAmber   = ColorPair(Color(0xFF281608), Color(0xFF34200C)),
    tilePurple  = ColorPair(Color(0xFF200C38), Color(0xFF2C1048)),
    tileGrey    = ColorPair(Color(0xFF201828), Color(0xFF2A2034)),
    actionPositive = DarkActionPositive,
    actionNegative = DarkActionNegative,
    actionNeutral  = DarkActionNeutral,
    answerCorrect     = DarkAnswerCorrect,
    answerWrongSoft   = DarkAnswerWrongSoft,
    answerCorrectText = DarkAnswerCorrectText,
    answerWrongText   = DarkAnswerWrongText,
    categoryEssentials      = ColorPair(Color(0xFF1C1240), Color(0xFF26184E)), // dark indigo
    categoryAtSchool        = ColorPair(Color(0xFF0C2014), Color(0xFF122A1C)), // dark green
    categorySchoolSubjects  = ColorPair(Color(0xFF200C38), Color(0xFF2C1048)), // dark violet
    categoryFoodAndEating   = ColorPair(Color(0xFF281608), Color(0xFF34200C)), // dark amber
    categoryFeelingsHealth  = ColorPair(Color(0xFF2A0E18), Color(0xFF36121E)), // dark rose
    categoryPlayHobbies     = ColorPair(Color(0xFF1E2808), Color(0xFF283410)), // dark olive
    categoryHome            = ColorPair(Color(0xFF0A1E22), Color(0xFF10282E)), // dark teal
    categoryOutAndAbout     = ColorPair(Color(0xFF100C30), Color(0xFF18143E)), // dark deep violet
    categoryDefault         = ColorPair(Color(0xFF201828), Color(0xFF2A2034)),
    practiceTypeDefault   = ColorPair(Color(0xFF1C1240), Color(0xFF26184E)),
    practiceTypeListening = ColorPair(Color(0xFF141230), Color(0xFF1C1840)),
    practiceTypeReading   = ColorPair(Color(0xFF200C38), Color(0xFF2C1048)),
    modeAll     = ColorPair(Color(0xFF1C1240), Color(0xFF26184E)),
    modeWeak    = ColorPair(Color(0xFF2A0C14), Color(0xFF36101C)),
    modeMastery = ColorPair(Color(0xFF0E2818), Color(0xFF143420)),
    onLightTile   = Color(0xFFE0D0F0),
    onDarkTile    = Color(0xFFE0D0F0),
    textSecondary = Color(0xFF9888B0),
    xpGainText    = Color(0xFFC080E8),
    starFilled = DarkStarFilled,
    starEmpty  = DarkStarEmpty,
    tone1       = DarkTone1,
    tone2       = DarkTone2,
    tone3       = DarkTone3,
    tone4       = DarkTone4,
    toneNeutral = DarkToneNeutral,
    toneTile1       = DarkToneTile1,
    toneTile2       = DarkToneTile2,
    toneTile3       = DarkToneTile3,
    toneTile4       = DarkToneTile4,
    toneTileNeutral = DarkToneTileNeutral,
    toneTileCorrect = DarkToneTileCorrect,
    toneTileWrong   = DarkToneTileWrong,
    toneTileIdle    = DarkToneTileIdle,
    masteryLow  = DarkMasteryLow,
    masteryMid  = DarkMasteryMid,
    masteryHigh = DarkMasteryHigh,
    masteryLowGradient  = DarkMasteryLowGrad,
    masteryMidGradient  = DarkMasteryMidGrad,
    masteryHighGradient = DarkMasteryHighGrad,
    achievementUnlocked     = ColorPair(Color(0xFF200C38), Color(0xFF2C1450)),
    achievementLocked       = ColorPair(Color(0xFF201828), Color(0xFF2A2034)),
    achievementUnlockedText = Color(0xFFB878E8),
)

private val DuskMd3 = androidx.compose.material3.darkColorScheme(
    primary                = Color(0xFFB078E0),
    onPrimary              = Color(0xFF200838),
    primaryContainer       = Color(0xFF2C1050),
    onPrimaryContainer     = Color(0xFFD8B0F8),
    secondary              = Color(0xFF9870C0),
    onSecondary            = Color(0xFF180428),
    secondaryContainer     = Color(0xFF221038),
    onSecondaryContainer   = Color(0xFFC8A0E8),
    tertiary               = Color(0xFFE08060),
    onTertiary             = Color(0xFF280800),
    tertiaryContainer      = Color(0xFF381408),
    onTertiaryContainer    = Color(0xFFF8C0A0),
    error                  = Color(0xFFCF6679),
    onError                = Color(0xFF2C0A0E),
    background             = Color(0xFF180E28),
    onBackground           = Color(0xFFE0D0F0),
    surface                = Color(0xFF180E28),
    onSurface              = Color(0xFFE0D0F0),
    surfaceVariant         = Color(0xFF221838),
    onSurfaceVariant       = Color(0xFFB8A0D0),
    outline                = Color(0xFF786090),
    outlineVariant         = Color(0xFF362848),
)

// ── Theme variant registry ────────────────────────────────────────────────────

/**
 * Bundles a named, emoji-labelled palette together with its matching Material 3
 * [ColorScheme]. Pass an [AppThemeVariant] to [MandarinKidsTheme] to apply a
 * complete, consistent theme across every layer of the app.
 *
 * [isDark] controls status-bar icon colour (light icons on dark, dark icons on light).
 */
data class AppThemeVariant(
    val id: Int,
    val name: String,
    val emoji: String,
    val palette: AppColorScheme,
    val md3: ColorScheme,
    val isDark: Boolean = false,
)

/** Ordered list of all available themes. Indices 0–4 are light; 5–9 are dark. */
val AppThemes: List<AppThemeVariant> = listOf(
    // ── Light themes ──────────────────────────────────────────────────────────
    AppThemeVariant(0, "Sage",     "🌿", DefaultPalette,  SageMd3),
    AppThemeVariant(1, "Ocean",    "🌊", OceanPalette,    OceanMd3),
    AppThemeVariant(2, "Sunset",   "🌅", SunsetPalette,   SunsetMd3),
    AppThemeVariant(3, "Forest",   "🌳", ForestPalette,   ForestMd3),
    AppThemeVariant(4, "Berry",    "🫐", BerryPalette,    BerryMd3),
    // ── Dark themes ───────────────────────────────────────────────────────────
    AppThemeVariant(5, "Night",    "🌙", NightPalette,    NightMd3,    isDark = true),
    AppThemeVariant(6, "Midnight", "🌌", MidnightPalette, MidnightMd3, isDark = true),
    AppThemeVariant(7, "Ember",    "🔥", EmberPalette,    EmberMd3,    isDark = true),
    AppThemeVariant(8, "Jungle",   "🌴", JunglePalette,   JungleMd3,   isDark = true),
    AppThemeVariant(9, "Dusk",     "🌆", DuskPalette,     DuskMd3,     isDark = true),
)
