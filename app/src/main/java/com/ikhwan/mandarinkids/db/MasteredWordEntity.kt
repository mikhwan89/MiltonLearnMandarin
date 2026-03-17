package com.ikhwan.mandarinkids.db

import androidx.room.Entity

@Entity(
    tableName = "mastered_words",
    primaryKeys = ["scenarioId", "chinese"]
)
data class MasteredWordEntity(
    val scenarioId: String,
    val chinese: String,
    val pinyin: String,
    val english: String,
    val indonesian: String,
    val note: String?,
    val masteredAt: Long = System.currentTimeMillis(),
    /** Leitner box level 1–5. All words start at 1. */
    val boxLevel: Int = 1,
    /** Epoch millis when this word is next due for review. 0 = due immediately. */
    val nextReviewDate: Long = 0L
)
