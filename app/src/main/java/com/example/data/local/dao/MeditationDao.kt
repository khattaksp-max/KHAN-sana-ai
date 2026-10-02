package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MeditationSession
import kotlinx.coroutines.flow.Flow

@Dao
interface MeditationDao {
    @Query("SELECT * FROM meditation_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<MeditationSession>>

    @Query("SELECT * FROM meditation_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): MeditationSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: MeditationSession): Long

    @Update
    suspend fun updateSession(session: MeditationSession)

    @Delete
    suspend fun deleteSession(session: MeditationSession)

    @Query("DELETE FROM meditation_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)
}
