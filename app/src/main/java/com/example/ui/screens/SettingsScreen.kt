package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.GeminiApiClient
import com.example.ui.components.GlassCard
import com.example.ui.viewmodel.SanaViewModel

@Composable
fun SettingsScreen(
    viewModel: SanaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceConfig by viewModel.voiceConfig.collectAsState()

    val voicePresets = listOf(
        Pair("Sana Warm", "Gentle, warm, loving & supportive"),
        Pair("Sana Serene", "Soft, meditative, slow & grounding"),
        Pair("Sana Radiant", "Lively, optimistic & encouraging"),
        Pair("Sana Clarity", "Articulate, balanced & professional")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070A14),
                        Color(0xFF0E1325),
                        Color(0xFF070A15)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SANA Settings",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Voice timbre, responsiveness & background mode",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Voice Style Selector
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_settings_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SANA Voice Personality",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        voicePresets.forEach { (name, desc) ->
                            val isSelected = voiceConfig.voicePreset == name
                            Surface(
                                onClick = {
                                    viewModel.updateVoiceConfig(voiceConfig.copy(voicePreset = name))
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF0369A1) else Color(0xFF161F38),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("voice_preset_$name")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (isSelected) Color(0xFFE0F2FE) else Color(0xFF94A3B8),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Speaking Speed Slider
                        Text(
                            text = "Speaking Speed: ${String.format("%.2f", voiceConfig.speechRate)}x",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Slider(
                            value = voiceConfig.speechRate,
                            onValueChange = {
                                viewModel.updateVoiceConfig(voiceConfig.copy(speechRate = it))
                            },
                            valueRange = 0.7f..1.3f,
                            steps = 5,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF38BDF8),
                                activeTrackColor = Color(0xFF0284C7),
                                inactiveTrackColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.testTag("speech_rate_slider")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Speaking Pitch Slider
                        Text(
                            text = "Voice Pitch: ${String.format("%.2f", voiceConfig.pitch)}x",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Slider(
                            value = voiceConfig.pitch,
                            onValueChange = {
                                viewModel.updateVoiceConfig(voiceConfig.copy(pitch = it))
                            },
                            valueRange = 0.8f..1.2f,
                            steps = 4,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFA855F7),
                                activeTrackColor = Color(0xFF7E22CE),
                                inactiveTrackColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.testTag("speech_pitch_slider")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Test Voice Button
                        Button(
                            onClick = {
                                viewModel.testVoice("Hello, I am SANA. I am here to guide and support you.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("preview_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Preview Voice", color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Continuous Conversation & Background Conversation
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Continuous Voice Conversation",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "Automatically returns to listening after SANA speaks",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                                )
                            }
                            Switch(
                                checked = voiceConfig.continuousConversation,
                                onCheckedChange = { isEnabled ->
                                    viewModel.updateVoiceConfig(voiceConfig.copy(continuousConversation = isEnabled))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF0284C7)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Background Voice Service",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "Keeps SANA ready via persistent notification. Microphone is only active with explicit visual indicators.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                                )
                            }
                            Switch(
                                checked = voiceConfig.backgroundServiceEnabled,
                                onCheckedChange = { isEnabled ->
                                    viewModel.toggleBackgroundService(context, isEnabled)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF0284C7)
                                ),
                                modifier = Modifier.testTag("background_service_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Use Gemini 3.8 Flash TTS",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "High-fidelity AI neural audio synthesis",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                                )
                            }
                            Switch(
                                checked = voiceConfig.useGeminiTts,
                                onCheckedChange = { isEnabled ->
                                    viewModel.updateVoiceConfig(voiceConfig.copy(useGeminiTts = isEnabled))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFA855F7)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Gemini API Status Card
            item {
                val hasKey = GeminiApiClient.hasApiKey()
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (hasKey) Color(0xFF10B981).copy(alpha = 0.35f) else Color(0xFFF59E0B).copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = if (hasKey) Color(0xFF10B981) else Color(0xFFF59E0B),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (hasKey) "Gemini AI Brain Connected" else "API Key Configuration",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = if (hasKey)
                                    "Connected securely via AI Studio Secrets (BuildConfig.GEMINI_API_KEY)"
                                else
                                    "Gemini key placeholder detected. Add your key in the AI Studio Secrets panel.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
