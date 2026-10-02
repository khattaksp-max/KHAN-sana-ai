package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meditation_sessions")
data class MeditationSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val theme: String,
    val durationMinutes: Int,
    val script: String,
    val visualPrompt: String = "",
    val imageBase64OrUri: String = "",
    val resolution: String = "1K", // 1K, 2K, 4K
    val audioDurationSeconds: Int = 180,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
