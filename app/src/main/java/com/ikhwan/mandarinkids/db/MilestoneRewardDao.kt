package com.ikhwan.mandarinkids.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneRewardDao {

    @Insert
    suspend fun insert(reward: MilestoneReward)

    @Query("SELECT * FROM milestone_rewards ORDER BY createdAt ASC")
    fun getAll(): Flow<List<MilestoneReward>>

    @Query("UPDATE milestone_rewards SET isClaimed = 1 WHERE id = :id")
    suspend fun claim(id: Int)

    @Query("DELETE FROM milestone_rewards WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM milestone_rewards")
    suspend fun deleteAll()
}
