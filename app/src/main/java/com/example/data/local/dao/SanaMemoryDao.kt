package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SanaMemory
import kotlinx.coroutines.flow.Flow

@Dao
interface SanaMemoryDao {
    @Query("SELECT * FROM sana_memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<SanaMemory>>

    @Query("SELECT * FROM sana_memories WHERE isActive = 1 ORDER BY timestamp DESC")
    fun getActiveMemories(): Flow<List<SanaMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: SanaMemory): Long

    @Update
    suspend fun updateMemory(memory: SanaMemory)

    @Delete
    suspend fun deleteMemory(memory: SanaMemory)

    @Query("DELETE FROM sana_memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM sana_memories")
    suspend fun clearAllMemories()
}
