package com.example.data.repository

import com.example.data.local.dao.MeditationDao
import com.example.data.local.dao.SanaMemoryDao
import com.example.data.local.entity.MeditationSession
import com.example.data.local.entity.SanaMemory
import kotlinx.coroutines.flow.Flow

class SanaRepository(
    private val memoryDao: SanaMemoryDao,
    private val meditationDao: MeditationDao
) {
    val allMemories: Flow<List<SanaMemory>> = memoryDao.getAllMemories()
    val activeMemories: Flow<List<SanaMemory>> = memoryDao.getActiveMemories()
    val allMeditationSessions: Flow<List<MeditationSession>> = meditationDao.getAllSessions()

    suspend fun insertMemory(memory: SanaMemory): Long {
        return memoryDao.insertMemory(memory)
    }

    suspend fun updateMemory(memory: SanaMemory) {
        memoryDao.updateMemory(memory)
    }

    suspend fun deleteMemory(memory: SanaMemory) {
        memoryDao.deleteMemory(memory)
    }

    suspend fun deleteMemoryById(id: Long) {
        memoryDao.deleteMemoryById(id)
    }

    suspend fun clearAllMemories() {
        memoryDao.clearAllMemories()
    }

    suspend fun insertMeditationSession(session: MeditationSession): Long {
        return meditationDao.insertSession(session)
    }

    suspend fun deleteMeditationSession(session: MeditationSession) {
        meditationDao.deleteSession(session)
    }

    suspend fun getMeditationSessionById(id: Long): MeditationSession? {
        return meditationDao.getSessionById(id)
    }
}
