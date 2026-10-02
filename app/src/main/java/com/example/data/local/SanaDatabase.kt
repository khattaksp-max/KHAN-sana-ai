package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.MeditationDao
import com.example.data.local.dao.SanaMemoryDao
import com.example.data.local.entity.MeditationSession
import com.example.data.local.entity.SanaMemory

@Database(
    entities = [SanaMemory::class, MeditationSession::class],
    version = 1,
    exportSchema = false
)
abstract class SanaDatabase : RoomDatabase() {
    abstract fun sanaMemoryDao(): SanaMemoryDao
    abstract fun meditationDao(): MeditationDao

    companion object {
        @Volatile
        private var INSTANCE: SanaDatabase? = null

        fun getInstance(context: Context): SanaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SanaDatabase::class.java,
                    "sana_ai_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
