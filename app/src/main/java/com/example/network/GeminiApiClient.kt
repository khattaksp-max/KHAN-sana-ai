package com.example.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    fun hasApiKey(): Boolean {
        val key = getApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * Generate multi-turn text with emotional awareness, role instruction, and supported actions.
     */
    suspend fun generateChatResponse(
        messages: List<Pair<String, String>>, // role to text
        systemInstruction: String,
        model: String = "gemini-3.5-flash",
        memories: List<String> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please set your key in AI Studio Secrets.")
            )
        }

        try {
            val root = JSONObject()

            // System instruction + memory context
            val fullSystemPrompt = buildString {
                append(systemInstruction)
                if (memories.isNotEmpty()) {
                    append("\n\nUser approved memories and preferences:\n")
                    memories.forEach { mem -> append("- ").append(mem).append("\n") }
                }
            }

            val sysObj = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", fullSystemPrompt))
            sysObj.put("parts", sysParts)
            root.put("systemInstruction", sysObj)

            // Multi-turn contents
            val contentsArray = JSONArray()
            for ((role, text) in messages) {
                val contentObj = JSONObject()
                contentObj.put("role", if (role == "user") "user" else "model")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", text))
                contentObj.put("parts", parts)
                contentsArray.put(contentObj)
            }
            root.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.75)
            genConfig.put("topP", 0.95)
            genConfig.put("topK", 40)
            root.put("generationConfig", genConfig)

            val url = "$BASE_URL/$model:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Chat request failed: ${response.code} $responseBody")
                return@withContext Result.failure(Exception("API Error (${response.code}): $responseBody"))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val textBuilder = StringBuilder()
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        val text = part.optString("text")
                        if (text.isNotBlank()) {
                            textBuilder.append(text)
                        }
                    }
                }
                val resultText = textBuilder.toString()
                if (resultText.isNotBlank()) {
                    return@withContext Result.success(resultText)
                }
            }
            Result.failure(Exception("Empty response from SANA brain"))
        } catch (e: Exception) {
            Log.e(TAG, "Error generating chat response", e)
            Result.failure(e)
        }
    }

    /**
     * Generate high-quality image using gemini-3-pro-image-preview
     * Supports image sizes: "1K", "2K", "4K"
     */
    suspend fun generateMeditationImage(
        prompt: String,
        imageSize: String = "1K",
        cacheDir: File
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured for image generation.")
            )
        }

        try {
            val root = JSONObject()
            val contents = JSONArray()
            val contentObj = JSONObject()
            val parts = JSONArray()
            parts.put(JSONObject().put("text", prompt))
            contentObj.put("parts", parts)
            contents.put(contentObj)
            root.put("contents", contents)

            val genConfig = JSONObject()
            val imageConfig = JSONObject()
            imageConfig.put("aspectRatio", "1:1")
            imageConfig.put("imageSize", imageSize) // 1K, 2K, 4K
            genConfig.put("imageConfig", imageConfig)

            val modalities = JSONArray()
            modalities.put("TEXT")
            modalities.put("IMAGE")
            genConfig.put("responseModalities", modalities)
            root.put("generationConfig", genConfig)

            val model = "gemini-3-pro-image-preview"
            val url = "$BASE_URL/$model:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Image generation failed: ${response.code} $responseBody")
                return@withContext Result.failure(Exception("Image generation error (${response.code})"))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val partsArr = content?.optJSONArray("parts")
                if (partsArr != null) {
                    for (i in 0 until partsArr.length()) {
                        val part = partsArr.getJSONObject(i)
                        val inlineData = part.optJSONObject("inlineData")
                        if (inlineData != null) {
                            val base64Data = inlineData.optString("data")
                            val mimeType = inlineData.optString("mimeType", "image/png")
                            if (base64Data.isNotBlank()) {
                                // Save to file cache for smooth Coil rendering
                                val extension = if (mimeType.contains("jpeg") || mimeType.contains("jpg")) ".jpg" else ".png"
                                val imageFile = File(cacheDir, "meditation_${System.currentTimeMillis()}$extension")
                                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                FileOutputStream(imageFile).use { it.write(decodedBytes) }
                                return@withContext Result.success(imageFile.absolutePath)
                            }
                        }
                    }
                }
            }
            Result.failure(Exception("No image returned from Gemini model"))
        } catch (e: Exception) {
            Log.e(TAG, "Exception during image generation", e)
            Result.failure(e)
        }
    }

    /**
     * Generate speech audio using gemini-3.8-flash-tts
     */
    suspend fun generateSpeechAudio(
        text: String,
        voiceName: String = "Kore", // "Puck", "Charon", "Kore", "Fenrir", "Aoede"
        cacheDir: File
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured for TTS.")
            )
        }

        try {
            val root = JSONObject()
            val contents = JSONArray()
            val contentObj = JSONObject()
            val parts = JSONArray()
            parts.put(JSONObject().put("text", text))
            contentObj.put("parts", parts)
            contents.put(contentObj)
            root.put("contents", contents)

            val genConfig = JSONObject()
            val modalities = JSONArray()
            modalities.put("AUDIO")
            genConfig.put("responseModalities", modalities)

            val speechConfig = JSONObject()
            val voiceConfig = JSONObject()
            val prebuiltVoiceConfig = JSONObject()
            prebuiltVoiceConfig.put("voiceName", voiceName)
            voiceConfig.put("prebuiltVoiceConfig", prebuiltVoiceConfig)
            speechConfig.put("voiceConfig", voiceConfig)
            genConfig.put("speechConfig", speechConfig)

            root.put("generationConfig", genConfig)

            val model = "gemini-3.8-flash-tts"
            val url = "$BASE_URL/$model:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "TTS failed: ${response.code} $responseBody")
                return@withContext Result.failure(Exception("TTS generation error (${response.code})"))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val partsArr = content?.optJSONArray("parts")
                if (partsArr != null) {
                    for (i in 0 until partsArr.length()) {
                        val part = partsArr.getJSONObject(i)
                        val inlineData = part.optJSONObject("inlineData")
                        if (inlineData != null) {
                            val base64Data = inlineData.optString("data")
                            if (base64Data.isNotBlank()) {
                                val audioFile = File(cacheDir, "sana_tts_${System.currentTimeMillis()}.mp3")
                                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                FileOutputStream(audioFile).use { it.write(decodedBytes) }
                                return@withContext Result.success(audioFile.absolutePath)
                            }
                        }
                    }
                }
            }
            Result.failure(Exception("No audio generated"))
        } catch (e: Exception) {
            Log.e(TAG, "Exception during TTS generation", e)
            Result.failure(e)
        }
    }
}
