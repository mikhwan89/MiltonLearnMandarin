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

    /** Words whose nextReviewDate is at or before [now] (due for review). */
    @Query("SELECT * FROM mastered_words WHERE nextReviewDate <= :now ORDER BY nextReviewDate ASC")
    fun getDueWords(now: Long): Flow<List<MasteredWordEntity>>

    @Query("SELECT COUNT(*) FROM mastered_words WHERE nextReviewDate <= :now")
    fun getDueCount(now: Long): Flow<Int>

    @Query("SELECT COUNT(DISTINCT chinese) FROM mastered_words WHERE boxLevel >= 7")
    fun getHighMasteryWordCount(): Flow<Int>

    @Query("DELETE FROM mastered_words")
    suspend fun deleteAll()
}
