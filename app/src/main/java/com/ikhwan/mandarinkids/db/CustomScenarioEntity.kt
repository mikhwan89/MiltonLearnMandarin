package com.ikhwan.mandarinkids.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_scenarios")
data class CustomScenarioEntity(
    @PrimaryKey val id: String,
    val scenarioJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
