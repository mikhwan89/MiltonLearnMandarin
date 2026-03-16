package com.ikhwan.mandarinkids.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Query("SELECT * FROM scenario_progress WHERE scenarioId = :id")
    fun getById(id: String): Flow<ScenarioProgressEntity?>

    @Query("SELECT * FROM scenario_progress")
    fun getAll(): Flow<List<ScenarioProgressEntity>>

    @Query("SELECT COALESCE(SUM(xp), 0) FROM scenario_progress")
    fun getTotalXp(): Flow<Int>

    @Upsert
    suspend fun upsert(entity: ScenarioProgressEntity)
}
