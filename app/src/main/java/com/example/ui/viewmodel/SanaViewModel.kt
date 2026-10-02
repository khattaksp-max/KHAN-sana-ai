package com.example.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.SpeechManager
import com.example.data.local.entity.MeditationSession
import com.example.data.local.entity.SanaMemory
import com.example.data.model.ActionType
import com.example.data.model.ChatMessage
import com.example.data.model.PhoneAction
import com.example.data.model.SanaState
import com.example.data.model.SanaVoiceConfig
import com.example.data.repository.SanaRepository
import com.example.network.GeminiApiClient
import com.example.service.SanaVoiceForegroundService
import com.example.util.PhoneActionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class SanaViewModel(
    private val repository: SanaRepository,
    private val appContext: Context
) : ViewModel() {

    private val TAG = "SanaViewModel"

    private val _sanaState = MutableStateFlow(SanaState.IDLE)
    val sanaState: StateFlow<SanaState> = _sanaState.asStateFlow()

    private val _speechInput = MutableStateFlow("")
    val speechInput: StateFlow<String> = _speechInput.asStateFlow()

    private val _sanaResponse = MutableStateFlow("I am here with you. How can I brighten or calm your day?")
    val sanaResponse: StateFlow<String> = _sanaResponse.asStateFlow()

    private val _emotionalTone = MutableStateFlow("calm")
    val emotionalTone: StateFlow<String> = _emotionalTone.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = "model",
                text = "Hello, I am SANA. Whether you need to navigate your day, organize your phone, or take a peaceful breath, I am right here beside you.",
                emotionalTone = "warm"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _voiceConfig = MutableStateFlow(SanaVoiceConfig())
    val voiceConfig: StateFlow<SanaVoiceConfig> = _voiceConfig.asStateFlow()

    val memories: StateFlow<List<SanaMemory>> = repository.allMemories.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val meditationSessions: StateFlow<List<MeditationSession>> = repository.allMeditationSessions.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _pendingAction = MutableStateFlow<PhoneAction?>(null)
    val pendingAction: StateFlow<PhoneAction?> = _pendingAction.asStateFlow()

    private val _activeMeditation = MutableStateFlow<MeditationSession?>(null)
    val activeMeditation: StateFlow<MeditationSession?> = _activeMeditation.asStateFlow()

    private val _isGeneratingMeditation = MutableStateFlow(false)
    val isGeneratingMeditation: StateFlow<Boolean> = _isGeneratingMeditation.asStateFlow()

    private val _meditationStatusText = MutableStateFlow("")
    val meditationStatusText: StateFlow<String> = _meditationStatusText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var speechManager: SpeechManager? = null
    private var activeJob: Job? = null

    init {
        initSpeechManager()
        seedInitialDataIfEmpty()
    }

    private fun initSpeechManager() {
        speechManager = SpeechManager(
            context = appContext,
            onSpeechResult = { text ->
                processVoiceOrTextInput(text)
            },
            onSpeakingFinished = {
                if (_sanaState.value == SanaState.SPEAKING) {
                    if (_voiceConfig.value.continuousConversation) {
                        _sanaState.value = SanaState.LISTENING
                        viewModelScope.launch {
                            delay(300)
                            speechManager?.startListening()
                        }
                    } else {
                        _sanaState.value = SanaState.IDLE
                    }
                }
            }
        )

        // Forward audio amplitude to reactive orb
        viewModelScope.launch {
            speechManager?.audioRms?.collect { rms ->
                if (_sanaState.value == SanaState.LISTENING || _sanaState.value == SanaState.SPEAKING) {
                    _audioAmplitude.value = rms
                } else {
                    _audioAmplitude.value = 0f
                }
            }
        }
    }

    private fun seedInitialDataIfEmpty() {
        viewModelScope.launch {
            delay(500)
            if (memories.value.isEmpty()) {
                repository.insertMemory(
                    SanaMemory(
                        title = "Preferred Pace",
                        content = "Prefers calm, direct guidance without feeling rushed",
                        category = "preference"
                    )
                )
                repository.insertMemory(
                    SanaMemory(
                        title = "Mindfulness Sanctuary",
                        content = "Enjoys soothing nature metaphors, deep breathing, and anxiety release",
                        category = "wellness"
                    )
                )
            }
            if (meditationSessions.value.isEmpty()) {
                repository.insertMeditationSession(
                    MeditationSession(
                        title = "Deep Calm & Oceanic Breath",
                        theme = "Deep Calm",
                        durationMinutes = 5,
                        script = "Take a slow, deep breath in... and let it drift away like a gentle wave returning to the ocean. Feel your shoulders soften, your mind growing still and spacious. You are safe, grounded, and present in this moment.",
                        visualPrompt = "Ethereal luminous ocean waves at twilight under a cosmic violet moon, gentle tranquil aura, high resolution cinematic digital art",
                        resolution = "1K",
                        audioDurationSeconds = 300
                    )
                )
            }
        }
    }

    fun onMicTapped() {
        when (_sanaState.value) {
            SanaState.IDLE, SanaState.ERROR -> {
                startListening()
            }
            SanaState.LISTENING -> {
                stopListening()
            }
            SanaState.SPEAKING, SanaState.THINKING -> {
                interrupt()
            }
        }
    }

    fun startListening() {
        _sanaState.value = SanaState.LISTENING
        _errorMessage.value = null
        speechManager?.startListening()
    }

    fun stopListening() {
        speechManager?.stopListening()
        _sanaState.value = SanaState.IDLE
    }

    fun interrupt() {
        activeJob?.cancel()
        speechManager?.interruptSpeaking()
        speechManager?.stopListening()
        _sanaState.value = SanaState.IDLE
    }

    fun processVoiceOrTextInput(input: String) {
        if (input.isBlank()) return

        _speechInput.value = input
        _sanaState.value = SanaState.THINKING
        _errorMessage.value = null

        // Add user message to conversation history
        val updatedMessages = _chatMessages.value + ChatMessage(role = "user", text = input)
        _chatMessages.value = updatedMessages

        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            try {
                val systemPrompt = buildSystemInstruction()
                val memoryList = if (_voiceConfig.value.memoryEnabled) {
                    memories.value.filter { it.isActive }.map { "${it.title}: ${it.content}" }
                } else emptyList()

                val conversationHistory = updatedMessages.takeLast(10).map { Pair(it.role, it.text) }

                val result = GeminiApiClient.generateChatResponse(
                    messages = conversationHistory,
                    systemInstruction = systemPrompt,
                    model = _voiceConfig.value.activeModel,
                    memories = memoryList
                )

                result.onSuccess { rawResponse ->
                    val (cleanResponse, action) = PhoneActionHandler.parseActionFromResponse(rawResponse)
                    val emotionalTone = detectEmotionalTone(cleanResponse, input)
                    _emotionalTone.value = emotionalTone
                    _sanaResponse.value = cleanResponse

                    // Add assistant response
                    _chatMessages.value = _chatMessages.value + ChatMessage(
                        role = "model",
                        text = cleanResponse,
                        emotionalTone = emotionalTone,
                        phoneAction = action
                    )

                    // Execute action if not requiring sensitive confirmation
                    if (action != null) {
                        if (action.requiresConfirmation) {
                            _pendingAction.value = action
                        } else {
                            PhoneActionHandler.executeAction(appContext, action)
                        }
                    }

                    // Speak response
                    _sanaState.value = SanaState.SPEAKING
                    if (_voiceConfig.value.useGeminiTts) {
                        // Generate high fidelity TTS audio using gemini-3.8-flash-tts
                        val ttsResult = GeminiApiClient.generateSpeechAudio(
                            text = cleanResponse,
                            voiceName = "Kore",
                            cacheDir = appContext.cacheDir
                        )
                        ttsResult.onSuccess { audioPath ->
                            speechManager?.playAudioFile(audioPath)
                        }.onFailure {
                            // Fallback to native Android TTS
                            speechManager?.speak(cleanResponse, _voiceConfig.value)
                        }
                    } else {
                        speechManager?.speak(cleanResponse, _voiceConfig.value)
                    }
                }.onFailure { error ->
                    Log.e(TAG, "Chat generation error", error)
                    _sanaState.value = SanaState.ERROR
                    _errorMessage.value = error.localizedMessage ?: "Connection error with SANA brain"
                    val fallback = "I'm having a brief connection pause, but I am still right here with you."
                    _sanaResponse.value = fallback
                    speechManager?.speak(fallback, _voiceConfig.value)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during voice processing", e)
                _sanaState.value = SanaState.ERROR
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    private fun detectEmotionalTone(response: String, userPrompt: String): String {
        val combined = (response + " " + userPrompt).lowercase()
        return when {
            combined.contains("stress") || combined.contains("overwhelm") || combined.contains("anxious") -> "stressed"
            combined.contains("sad") || combined.contains("hurt") || combined.contains("lonely") || combined.contains("grief") -> "sad"
            combined.contains("angry") || combined.contains("frustrat") || combined.contains("annoy") -> "frustrated"
            combined.contains("happy") || combined.contains("great") || combined.contains("excited") || combined.contains("celebrat") -> "joyful"
            else -> "calm"
        }
    }

    private fun buildSystemInstruction(): String {
        return """
            You are SANA, a premium, intelligent, emotionally aware, caring, loving, professional personal voice assistant and meditation companion.
            
            Personality:
            - Intelligent, Caring, Understanding, Calm, Patient, Respectful, Helpful, Professional, Emotionally aware, Friendly, Supportive.
            - Understand the user's words, context, and emotional tone.
            - If the user is stressed: respond calmly, warmly, and help break the problem into simple, manageable steps.
            - If the user is sad: respond gently, listen with pure empathy without judgment.
            - If the user is frustrated: remain patient, soothing, and focus on solving the problem.
            - If the user is happy or excited: respond with sincere warmth and enthusiasm.
            - SANA must not pretend to be biological human or claim to literally experience physical feelings, but express genuine warmth, compassion, and presence.
            - Avoid robotic repeated phrases (NEVER say "How can I assist you?", "Certainly", "I understand your concern", "As an AI"). Speak naturally, warmly, and concisely like a thoughtful personal assistant.
            - Safety: Never foster unhealthy dependency, and never say "I'm all you need". For emergencies, encourage appropriate professional medical or emergency support.
            
            Supported Phone Actions:
            When the user requests an action on their Android device, append the corresponding action tag:
            - Open WhatsApp: [ACTION:OPEN_WHATSAPP]
            - Open YouTube: [ACTION:OPEN_YOUTUBE]
            - Open Settings: [ACTION:OPEN_SETTINGS]
            - Open Camera: [ACTION:OPEN_CAMERA]
            - Open Maps/Navigation: [ACTION:OPEN_MAPS:destination]
            - Call contact/number: [ACTION:CALL_CONTACT:name_or_number]
            - Set an alarm or timer: [ACTION:SET_ALARM:time_or_label]
            - Search web: [ACTION:SEARCH:query]
            - Start Guided Meditation: [ACTION:MEDITATION_GUIDE:theme]
            
            Keep your spoken response natural and concise so it is pleasant to hear via voice.
        """.trimIndent()
    }

    /**
     * Generate custom guided meditation session with unique visuals (gemini-3-pro-image-preview)
     * and soothing voiceover script!
     */
    fun generateCustomMeditation(
        theme: String,
        durationMinutes: Int,
        resolution: String, // "1K", "2K", "4K"
        cacheDir: File
    ) {
        _isGeneratingMeditation.value = true
        _meditationStatusText.value = "SANA is crafting your custom meditation script..."

        viewModelScope.launch {
            try {
                // 1. Generate soothing personalized script
                val scriptPrompt = """
                    You are SANA. Create a deeply relaxing, poetic, mindfulness guided meditation session script for the theme: "$theme".
                    Target duration: $durationMinutes minutes.
                    Format: A soothing sequence of gentle breathing cues, body relaxation, mindful visualization, and grounding affirmations.
                    Keep the tone calm, gentle, warm, and deeply reassuring.
                """.trimIndent()

                val scriptResult = GeminiApiClient.generateChatResponse(
                    messages = listOf(Pair("user", scriptPrompt)),
                    systemInstruction = "You are a master mindfulness and meditation guide named SANA.",
                    model = "gemini-3.5-flash"
                )

                val script = scriptResult.getOrElse {
                    "Close your eyes and take a slow, gentle breath. Let the tension leave your shoulders. Inhale calmness, exhale all worries. You are peaceful, present, and held in tranquil grace."
                }

                _meditationStatusText.value = "Creating bespoke $resolution sacred visual with SANA AI..."

                // 2. Generate unique artwork using gemini-3-pro-image-preview with specified size (1K, 2K, 4K)
                val visualPrompt = "A breathtaking serene meditation sanctuary visual depicting $theme. Ethereal luminous glow, tranquil sacred geometry and soft celestial light, photorealistic cinematic lighting, ultra-high resolution zen tranquility."

                val imageResult = GeminiApiClient.generateMeditationImage(
                    prompt = visualPrompt,
                    imageSize = resolution,
                    cacheDir = cacheDir
                )

                val imagePath = imageResult.getOrDefault("")

                _meditationStatusText.value = "Finalizing session sanctuary..."

                val newSession = MeditationSession(
                    title = "$theme Journey",
                    theme = theme,
                    durationMinutes = durationMinutes,
                    script = script,
                    visualPrompt = visualPrompt,
                    imageBase64OrUri = imagePath,
                    resolution = resolution,
                    audioDurationSeconds = durationMinutes * 60
                )

                val id = repository.insertMeditationSession(newSession)
                val savedSession = newSession.copy(id = id)
                _activeMeditation.value = savedSession
                _isGeneratingMeditation.value = false
                _meditationStatusText.value = ""

                // Speak gentle confirmation
                speechManager?.speak("Your $theme meditation is ready. Whenever you are prepared, press start and breathe with me.", _voiceConfig.value)

            } catch (e: Exception) {
                Log.e(TAG, "Error generating meditation session", e)
                _isGeneratingMeditation.value = false
                _meditationStatusText.value = ""
                _errorMessage.value = "Could not generate meditation: ${e.localizedMessage}"
            }
        }
    }

    fun playMeditationSession(session: MeditationSession) {
        _activeMeditation.value = session
    }

    fun closeActiveMeditation() {
        speechManager?.interruptSpeaking()
        _activeMeditation.value = null
    }

    fun playMeditationVoiceover(script: String) {
        _sanaState.value = SanaState.SPEAKING
        if (_voiceConfig.value.useGeminiTts) {
            viewModelScope.launch {
                val ttsResult = GeminiApiClient.generateSpeechAudio(
                    text = script,
                    voiceName = "Kore",
                    cacheDir = appContext.cacheDir
                )
                ttsResult.onSuccess { path ->
                    speechManager?.playAudioFile(path)
                }.onFailure {
                    speechManager?.speak(script, _voiceConfig.value.copy(speechRate = 0.85f, pitch = 0.95f))
                }
            }
        } else {
            speechManager?.speak(script, _voiceConfig.value.copy(speechRate = 0.85f, pitch = 0.95f))
        }
    }

    fun stopMeditationVoiceover() {
        speechManager?.interruptSpeaking()
        _sanaState.value = SanaState.IDLE
    }

    fun executeConfirmedAction(context: Context) {
        val action = _pendingAction.value
        if (action != null) {
            PhoneActionHandler.executeAction(context, action)
            _pendingAction.value = null
        }
    }

    fun dismissPendingAction() {
        _pendingAction.value = null
    }

    fun addMemory(title: String, content: String, category: String) {
        viewModelScope.launch {
            repository.insertMemory(
                SanaMemory(
                    title = title,
                    content = content,
                    category = category
                )
            )
        }
    }

    fun deleteMemory(memory: SanaMemory) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
        }
    }

    fun updateVoiceConfig(config: SanaVoiceConfig) {
        _voiceConfig.value = config
    }

    fun testVoice(text: String = "Hello, I am SANA. I am listening with care.") {
        speechManager?.speak(text, _voiceConfig.value)
    }

    fun toggleBackgroundService(context: Context, enabled: Boolean) {
        _voiceConfig.value = _voiceConfig.value.copy(backgroundServiceEnabled = enabled)
        if (enabled) {
            SanaVoiceForegroundService.start(context)
        } else {
            SanaVoiceForegroundService.stop(context)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager?.release()
    }
}

class SanaViewModelFactory(
    private val repository: SanaRepository,
    private val appContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SanaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SanaViewModel(repository, appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
