package com.ikhwan.mandarinkids.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class MilestoneType(val label: String, val unit: String, val emoji: String) {
    PERFECT_SCENARIOS("Perfect scenarios (3★)", "scenarios", "⭐"),
    HIGH_MASTERY_WORDS("Words at full mastery (★10, all modes)", "words", "🌟"),
    MASTERY_DEFAULT("Words at ★10 — Default mode", "words", "🔊字"),
    MASTERY_LISTENING("Words at ★10 — Listening mode", "words", "🔊"),
    MASTERY_READING("Words at ★10 — Reading mode", "words", "字"),
    TOTAL_XP("Total XP earned", "XP", "✨")
}

@Serializable
data class MilestoneCondition(val type: String, val targetValue: Int)

@Entity(tableName = "milestone_rewards")
data class MilestoneReward(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** JSON-encoded List<MilestoneCondition>. */
    val conditionsJson: String,
    /** "AND" — all conditions must be met. "OR" — any one is enough. */
    val logic: String = "AND",
    val rewardText: String,
    val isClaimed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

private val _rewardJson = Json { ignoreUnknownKeys = true }

fun MilestoneReward.decodeConditions(): List<MilestoneCondition> =
    try { _rewardJson.decodeFromString(conditionsJson) } catch (e: Exception) { emptyList() }

fun encodeConditions(conditions: List<MilestoneCondition>): String =
    _rewardJson.encodeToString(conditions)
