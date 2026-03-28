package com.ikhwan.mandarinkids.ui.theme

import androidx.compose.ui.graphics.Color

// ── Surfaces (Warm Almond) ────────────────────────────────────────────────────
val AppBackground       = Color(0xFFFBF9F4)   // warm almond — base background color
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

// ── Dark mode surfaces ────────────────────────────────────────────────────────
val DarkBackground    = Color(0xFF1A1C19)   // very dark warm-tinted black
val DarkSurface       = Color(0xFF1F2118)   // dark surface

// ── Dark mode — Primary (lighter sage) ───────────────────────────────────────
val DarkColorPrimary            = Color(0xFF9FD696)
val DarkColorOnPrimary          = Color(0xFF003910)
val DarkColorPrimaryContainer   = Color(0xFF1C5022)
val DarkColorOnPrimaryContainer = Color(0xFFBAF2B0)

// ── Dark mode — Secondary (lighter slate blue) ────────────────────────────────
val DarkColorSecondary            = Color(0xFFB4CAE0)
val DarkColorOnSecondary          = Color(0xFF1E3347)
val DarkColorSecondaryContainer   = Color(0xFF354A5E)
val DarkColorOnSecondaryContainer = Color(0xFFD0E8F8)

// ── Dark mode — Tertiary (lighter amber) ──────────────────────────────────────
val DarkColorTertiary            = Color(0xFFE5C18A)
val DarkColorOnTertiary          = Color(0xFF422C00)
val DarkColorTertiaryContainer   = Color(0xFF5D4118)
val DarkColorOnTertiaryContainer = Color(0xFFFFDDB5)

// ── Dark mode — Neutrals ──────────────────────────────────────────────────────
val DarkColorOnSurface        = Color(0xFFE2E4DC)
val DarkColorSurfaceVariant   = Color(0xFF3D4438)
val DarkColorOnSurfaceVariant = Color(0xFFC3C9BB)
val DarkColorOutline          = Color(0xFF8D9387)
val DarkColorOutlineVariant   = Color(0xFF43483D)

// ── Legacy aliases (keep old names so existing Theme.kt compiles) ─────────────
val Purple80      = ColorPrimary
val PurpleGrey80  = ColorSecondary
val Pink80        = ColorTertiary
val Purple40      = ColorPrimary
val PurpleGrey40  = ColorSecondary
val Pink40        = ColorTertiary
