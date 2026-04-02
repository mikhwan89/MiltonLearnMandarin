package com.ikhwan.mandarinkids.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomScenarioDao {
    @Query("SELECT * FROM custom_scenarios ORDER BY createdAt DESC")
    fun getAll(): Flow<List<CustomScenarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CustomScenarioEntity)

    @Query("DELETE FROM custom_scenarios WHERE id = :id")
    suspend fun deleteById(id: String)
}
