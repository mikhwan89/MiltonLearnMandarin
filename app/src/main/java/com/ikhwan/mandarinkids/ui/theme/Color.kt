package com.ikhwan.mandarinkids.ui.theme

import androidx.compose.ui.graphics.Color

// ── Surfaces (Warm Almond) ────────────────────────────────────────────────────
val AppBackground       = Color(0x00000000)   // transparent — background.png shows through
val AppSurface          = Color(0xFFFBF9F4)   // warm almond for cards/surfaces
val AppSurfaceContainerLow     = Color(0xFFF5F4ED)   // section layer
val AppSurfaceContainerLowest  = Color(0xFFFFFFFF)   // card / interactive layer

// ── Primary — Sage Green ─────────────────────────────────────────────────────
val ColorPrimary              = Color(0xFF386A34)
val ColorOnPrimary            = Color(0xFFFFFFFF)
val ColorPrimaryContainer     = Color(0xFFBAF2B0)
val ColorOnPrimaryContainer   = Color(0xFF002204)
val ColorPrimaryFixed         = Color(0xFFBAF2B0)
val ColorPrimaryFixedDim      = Color(0xFF9ED496)   // thread indicator / subtle accent

// ── Secondary — Slate Blue ───────────────────────────────────────────────────
val ColorSecondary             = Color(0xFF4A6780)
val ColorOnSecondary           = Color(0xFFFFFFFF)
val ColorSecondaryContainer    = Color(0xFFD0E8F8)
val ColorOnSecondaryContainer  = Color(0xFF021D2E)

// ── Tertiary — Warm Amber ────────────────────────────────────────────────────
val ColorTertiary              = Color(0xFF7A5C30)
val ColorOnTertiary            = Color(0xFFFFFFFF)
val ColorTertiaryContainer     = Color(0xFFFFDDB5)
val ColorOnTertiaryContainer   = Color(0xFF2A1600)

// ── Error ─────────────────────────────────────────────────────────────────────
val ColorError   = Color(0xFFBA1A1A)
val ColorOnError = Color(0xFFFFFFFF)

// ── Neutrals ──────────────────────────────────────────────────────────────────
val ColorOnSurface        = Color(0xFF31332E)   // near-black, not pure black
val ColorSurfaceVariant   = Color(0xFFE4E3DA)
val ColorOnSurfaceVariant = Color(0xFF4A4C47)
val ColorOutline          = Color(0xFF7A7D76)
val ColorOutlineVariant   = Color(0xFFC9CBC5)

// ── Legacy aliases (keep old names so existing Theme.kt compiles) ─────────────
val Purple80      = ColorPrimary
val PurpleGrey80  = ColorSecondary
val Pink80        = ColorTertiary
val Purple40      = ColorPrimary
val PurpleGrey40  = ColorSecondary
val Pink40        = ColorTertiary
