package com.ikhwan.mandarinkids.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class MilestoneType(val label: String, val unit: String, val emoji: String) {
    MASTERY_DEFAULT("Flashcard at ★10 — Default mode", "flashcards", "🔊字"),
    MASTERY_LISTENING("Flashcard at ★10 — Listening mode", "flashcards", "🔊"),
    MASTERY_READING("Flashcard at ★10 — Reading mode", "flashcards", "字"),
    TOTAL_XP("Total XP earned", "XP", "✨"),
    SPECIFIC_BADGE("Specific badge earned", "badge", "🏅"),
    // Scenario mastery level completions (3★ required to advance)
    LEVEL_1_COMPLETIONS("Scenarios passed Level 1 (3★)", "scenarios", "1️⃣"),
    LEVEL_2_COMPLETIONS("Scenarios passed Level 2 (3★)", "scenarios", "2️⃣"),
    LEVEL_3_COMPLETIONS("Scenarios passed Level 3 (3★)", "scenarios", "3️⃣"),
    LEVEL_4_COMPLETIONS("Scenarios passed Level 4 (3★)", "scenarios", "4️⃣"),
    LEVEL_5_COMPLETIONS("Scenarios completed Level 5 (3★)", "scenarios", "5️⃣"),
    // Goal-based counters — progress measured from when the parent set the goal
    TONE_CORRECT_SINCE("Tone questions correct (since goal set)", "correct", "🎵"),
    SENTENCE_CORRECT_SINCE("Sentences built correctly (since goal set)", "correct", "🧩"),
}

@Serializable
data class MilestoneCondition(
    val type: String,
    val targetValue: Int,
    val badgeId: String? = null,
    /** Cumulative count at the moment the parent set this goal. Progress = current − baseline. */
    val baselineValue: Int = 0
)

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
