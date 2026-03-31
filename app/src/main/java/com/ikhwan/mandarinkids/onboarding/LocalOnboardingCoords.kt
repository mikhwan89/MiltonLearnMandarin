package com.ikhwan.mandarinkids.onboarding

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.geometry.Rect

/**
 * CompositionLocal that carries a shared SnapshotStateMap of tagged UI
 * element bounds (in root-relative coordinates). Each participating
 * composable writes its measured bounds via onGloballyPositioned +
 * boundsInRoot(). The interactive onboarding overlay reads these to
 * position spotlights and tooltip bubbles.
 */
val LocalOnboardingCoords = compositionLocalOf<SnapshotStateMap<String, Rect>> {
    error("LocalOnboardingCoords not provided")
}

/** Stable string keys for each element targeted by the tour. */
object OnboardingKey {
    const val THEME_BUTTON  = "theme_button"
    const val STATS_ROW     = "stats_row"
    const val CATEGORY_GRID = "category_grid"
    const val NAV_BAR       = "nav_bar"
}
