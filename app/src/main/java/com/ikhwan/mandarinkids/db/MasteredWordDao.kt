package com.ikhwan.mandarinkids.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MasteredWordDao {

    @Upsert
    suspend fun upsert(entity: MasteredWordEntity)

    /** Insert only — silently skips rows that already exist (preserves earned boxLevel). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entity: MasteredWordEntity)

    @Query("SELECT * FROM mastered_words ORDER BY masteredAt DESC")
    fun getAll(): Flow<List<MasteredWordEntity>>

    /** All words for a specific practice type. */
    @Query("SELECT * FROM mastered_words WHERE practiceType = :practiceType ORDER BY masteredAt DESC")
    fun getAllByType(practiceType: String): Flow<List<MasteredWordEntity>>

    /** Count of distinct Chinese words (across all practice types). */
    @Query("SELECT COUNT(DISTINCT chinese) FROM mastered_words")
    fun getTotalCount(): Flow<Int>

    /** Words whose nextReviewDate is at or before [now] (due for review). */
    @Query("SELECT * FROM mastered_words WHERE nextReviewDate <= :now ORDER BY nextReviewDate ASC")
    fun getDueWords(now: Long): Flow<List<MasteredWordEntity>>

    @Query("SELECT COUNT(*) FROM mastered_words WHERE nextReviewDate <= :now")
    fun getDueCount(now: Long): Flow<Int>

    @Query("SELECT COUNT(DISTINCT chinese) FROM mastered_words WHERE boxLevel >= 10")
    fun getHighMasteryWordCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT chinese) FROM mastered_words WHERE practiceType = :practiceType AND boxLevel >= 10")
    fun getHighMasteryCountByType(practiceType: String): Flow<Int>

    @Query("DELETE FROM mastered_words")
    suspend fun deleteAll()
}
