package com.ikhwan.mandarinkids.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MilestoneType(val label: String, val unit: String, val emoji: String) {
    PERFECT_SCENARIOS("Scenarios with 3 stars", "scenarios", "⭐"),
    HIGH_MASTERY_WORDS("Flashcard words at full mastery (🌟 10/10)", "words", "🌟")
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
