package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.local.SanaDatabase
import com.example.data.repository.SanaRepository

class SanaApplication : Application() {

    lateinit var database: SanaDatabase
        private set

    lateinit var repository: SanaRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = SanaDatabase.getInstance(this)
        repository = SanaRepository(database.sanaMemoryDao(), database.meditationDao())
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_VOICE,
                "SANA Voice Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active voice conversation and background assistance status"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID_VOICE = "sana_voice_channel"
    }
}
