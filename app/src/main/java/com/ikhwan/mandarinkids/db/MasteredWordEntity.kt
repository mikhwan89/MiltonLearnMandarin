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
    val masteredAt: Long = System.currentTimeMillis()
)
