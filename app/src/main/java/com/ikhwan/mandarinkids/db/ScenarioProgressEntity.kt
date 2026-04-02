package com.ikhwan.mandarinkids.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scenario_progress")
data class ScenarioProgressEntity(
    @PrimaryKey val scenarioId: String,
    val stars: Int,
    val xp: Int,
    val lastPlayedAt: Long = 0L,
    val speechRateOverride: Float? = null,
    val masteryLevel: Int = 1,
    /** Stars earned on the *current* mastery level. Resets to 0 when a new level unlocks. */
    val starsAtCurrentLevel: Int = 0
)
