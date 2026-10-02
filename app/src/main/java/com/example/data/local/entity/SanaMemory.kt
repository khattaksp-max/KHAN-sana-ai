package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sana_memories")
data class SanaMemory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "general", // "preference", "personal", "wellness", "instruction"
    val timestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
