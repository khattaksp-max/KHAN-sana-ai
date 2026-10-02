package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.SanaVoiceConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(
    private val context: Context,
    private val onSpeechResult: (String) -> Unit,
    private val onSpeakingFinished: () -> Unit
) : TextToSpeech.OnInitListener {

    private val TAG = "SpeechManager"

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isTtsInitialized = false

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _isRecognizing = MutableStateFlow(false)
    val isRecognizing: StateFlow<Boolean> = _isRecognizing.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        initTts()
        initSpeechRecognizer()
    }

    private fun initTts() {
        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            textToSpeech?.language = Locale.getDefault()
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onSpeakingFinished()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    onSpeakingFinished()
                }
            })
        } else {
            Log.e(TAG, "TTS Initialization failed with status: $status")
        }
    }

    private fun initSpeechRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isRecognizing.value = true
                        }

                        override fun onBeginningOfSpeech() {}

                        override fun onRmsChanged(rmsdB: Float) {
                            // Map RMS dB (usually -2 to 10) to 0.0 .. 1.0 range
                            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
                            _audioRms.value = normalized
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _isRecognizing.value = false
                            _audioRms.value = 0f
                        }

                        override fun onError(error: Int) {
                            _isRecognizing.value = false
                            _audioRms.value = 0f
                            Log.w(TAG, "SpeechRecognizer error: $error")
                        }

                        override fun onResults(results: Bundle?) {
                            _isRecognizing.value = false
                            _audioRms.value = 0f
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                val spokenText = matches[0]
                                onSpeechResult(spokenText)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                // partial transcript
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "SpeechRecognizer initialization failed", e)
        }
    }

    fun startListening() {
        interruptSpeaking()
        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(intent)
            _isRecognizing.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            _isRecognizing.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recognition", e)
        }
        _isRecognizing.value = false
        _audioRms.value = 0f
    }

    fun speak(text: String, voiceConfig: SanaVoiceConfig) {
        interruptSpeaking()

        if (!isTtsInitialized || textToSpeech == null) {
            return
        }

        try {
            // Apply voice parameters
            textToSpeech?.setSpeechRate(voiceConfig.speechRate)

            // Adjust pitch based on preset or user setting
            val pitchMultiplier = when (voiceConfig.voicePreset) {
                "Sana Serene" -> 0.95f
                "Sana Radiant" -> 1.15f
                "Sana Clarity" -> 1.0f
                else -> 1.05f // Sana Warm
            }
            textToSpeech?.setPitch(voiceConfig.pitch * pitchMultiplier)

            val utteranceId = "sana_utterance_${System.currentTimeMillis()}"
            _isSpeaking.value = true
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking text", e)
            _isSpeaking.value = false
        }
    }

    fun playAudioFile(filePath: String) {
        interruptSpeaking()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setOnPreparedListener {
                    _isSpeaking.value = true
                    start()
                }
                setOnCompletionListener {
                    _isSpeaking.value = false
                    release()
                    mediaPlayer = null
                    onSpeakingFinished()
                }
                setOnErrorListener { _, _, _ ->
                    _isSpeaking.value = false
                    release()
                    mediaPlayer = null
                    onSpeakingFinished()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio file", e)
            _isSpeaking.value = false
            onSpeakingFinished()
        }
    }

    fun interruptSpeaking() {
        try {
            if (_isSpeaking.value) {
                textToSpeech?.stop()
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                _isSpeaking.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error interrupting speech", e)
        }
    }

    fun release() {
        stopListening()
        interruptSpeaking()
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
