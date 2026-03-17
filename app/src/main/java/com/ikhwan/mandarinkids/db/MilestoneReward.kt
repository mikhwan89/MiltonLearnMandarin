package com.ikhwan.mandarinkids.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MilestoneType(val label: String, val unit: String) {
    XP_THRESHOLD("Reach XP", "XP"),
    SCENARIOS_COMPLETED("Complete scenarios", "scenarios"),
    STREAK_DAYS("Streak days", "days"),
    WORDS_MASTERED("Master words", "words")
}

@Entity(tableName = "milestone_rewards")
data class MilestoneReward(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val milestoneType: String,   // MilestoneType.name
    val targetValue: Int,
    val rewardText: String,
    val isClaimed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
