package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmotionDao {
    @Query("SELECT * FROM emotion_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<EmotionRecord>>

    @Query("SELECT * FROM emotion_records WHERE id = :id LIMIT 1")
    fun getRecordById(id: Int): Flow<EmotionRecord?>

    @Query("SELECT * FROM emotion_records WHERE inputText LIKE '%' || :query || '%' OR dominantEmotion LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchRecords(query: String): Flow<List<EmotionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: EmotionRecord): Long

    @Delete
    suspend fun deleteRecord(record: EmotionRecord)

    @Query("DELETE FROM emotion_records")
    suspend fun clearAllRecords()
}
