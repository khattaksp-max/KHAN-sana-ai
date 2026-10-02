package com.example.data.model

import java.util.UUID

enum class SanaState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

enum class ActionType {
    OPEN_WHATSAPP,
    OPEN_YOUTUBE,
    OPEN_SETTINGS,
    OPEN_MAPS,
    OPEN_CAMERA,
    CALL_CONTACT,
    SET_ALARM,
    WEB_SEARCH,
    GUIDED_MEDITATION,
    NONE
}

data class PhoneAction(
    val type: ActionType,
    val title: String,
    val description: String,
    val param: String = "",
    val requiresConfirmation: Boolean = false
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val emotionalTone: String = "neutral",
    val phoneAction: PhoneAction? = null,
    val isStreaming: Boolean = false
)

data class SanaVoiceConfig(
    val voicePreset: String = "Sana Warm",
    val speechRate: Float = 0.95f,
    val pitch: Float = 1.05f,
    val continuousConversation: Boolean = true,
    val backgroundServiceEnabled: Boolean = false,
    val memoryEnabled: Boolean = true,
    val useGeminiTts: Boolean = false,
    val activeModel: String = "gemini-3.5-flash",
    val meditationImageResolution: String = "1K" // "1K", "2K", "4K"
)
