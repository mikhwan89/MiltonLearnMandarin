package com.ikhwan.mandarinkids.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MasteredWordDao {

    @Upsert
    suspend fun upsert(entity: MasteredWordEntity)

    @Query("SELECT * FROM mastered_words ORDER BY masteredAt DESC")
    fun getAll(): Flow<List<MasteredWordEntity>>

    @Query("SELECT COUNT(*) FROM mastered_words")
    fun getTotalCount(): Flow<Int>
}
