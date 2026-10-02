package com.example.ui.screens

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PhoneAction
import com.example.data.model.SanaState
import com.example.ui.components.GlassCard
import com.example.ui.components.SanaOrb
import com.example.ui.components.WaveformVisualizer
import com.example.ui.viewmodel.SanaViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: SanaViewModel,
    onNavigateToMeditation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sanaState by viewModel.sanaState.collectAsState()
    val speechInput by viewModel.speechInput.collectAsState()
    val sanaResponse by viewModel.sanaResponse.collectAsState()
    val audioAmp by viewModel.audioAmplitude.collectAsState()
    val pendingAction by viewModel.pendingAction.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val voiceConfig by viewModel.voiceConfig.collectAsState()

    var hasMicPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            viewModel.startListening()
        }
    }

    // Sensitive Action Confirmation Dialog (e.g. Call Contact)
    pendingAction?.let { action ->
        ActionConfirmationDialog(
            action = action,
            onConfirm = { viewModel.executeConfirmedAction(context) },
            onDismiss = { viewModel.dismissPendingAction() }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070913),
                        Color(0xFF0D1224),
                        Color(0xFF0A0D1B)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Assistant Title and Status Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SANA AI",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Intelligent Voice & Sanctuary",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // State Pill
                StateBadge(state = sanaState)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Primary Interactive Animated Orb
            Box(
                modifier = Modifier.padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                SanaOrb(
                    state = sanaState,
                    amplitude = audioAmp,
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    size = 250.dp
                )
            }

            // Status Text and Waveform
            val stateText = when (sanaState) {
                SanaState.IDLE -> "Tap orb to speak with SANA"
                SanaState.LISTENING -> "Listening..."
                SanaState.THINKING -> "Thinking..."
                SanaState.SPEAKING -> "SANA is speaking..."
                SanaState.ERROR -> "Gentle connection pause"
            }

            Text(
                text = stateText,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = when (sanaState) {
                        SanaState.LISTENING -> Color(0xFF00F0FF)
                        SanaState.THINKING -> Color(0xFFA855F7)
                        SanaState.SPEAKING -> Color(0xFF38BDF8)
                        SanaState.ERROR -> Color(0xFFF87171)
                        SanaState.IDLE -> Color(0xFF94A3B8)
                    },
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.testTag("state_status_text")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Waveform reactive animation
            WaveformVisualizer(
                amplitude = audioAmp,
                isSpeakingOrListening = sanaState == SanaState.LISTENING || sanaState == SanaState.SPEAKING,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // SANA Response / Spoken Transcript Glass Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sana_speech_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    if (speechInput.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "You said:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Text(
                            text = "\"$speechInput\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFCBD5E1),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            modifier = Modifier.padding(start = 22.dp, top = 2.dp, bottom = 12.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SANA:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFFA855F7),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = sanaResponse,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            lineHeight = 22.sp
                        ),
                        modifier = Modifier.testTag("sana_response_text")
                    )

                    // Error warning if any
                    errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFFCA5A5),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Voice Prompts & Phone Controls
            Text(
                text = "Natural Voice Commands",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickCommandChip(label = "Open WhatsApp") {
                    viewModel.processVoiceOrTextInput("Open WhatsApp")
                }
                QuickCommandChip(label = "Open YouTube") {
                    viewModel.processVoiceOrTextInput("Open YouTube")
                }
                QuickCommandChip(label = "Open Settings") {
                    viewModel.processVoiceOrTextInput("Open Settings")
                }
                QuickCommandChip(label = "Deep Calm Meditation") {
                    viewModel.processVoiceOrTextInput("Guide me through a calming meditation for stress relief")
                }
                QuickCommandChip(label = "Call Contact") {
                    viewModel.processVoiceOrTextInput("Call Mom")
                }
                QuickCommandChip(label = "I feel stressed") {
                    viewModel.processVoiceOrTextInput("I feel really stressed and overwhelmed right now")
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom Voice Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interruption / Stop speaking button
                IconButton(
                    onClick = { viewModel.interrupt() },
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFF1E293B), CircleShape)
                        .testTag("interrupt_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Interrupt Voice",
                        tint = Color(0xFFF87171)
                    )
                }

                // Primary Mic Action Button
                Surface(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("primary_mic_button"),
                    shape = CircleShape,
                    color = if (sanaState == SanaState.LISTENING) Color(0xFF00F0FF) else Color(0xFF6366F1),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (sanaState == SanaState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Microphone",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Quick Navigation to Guided Meditation Sanctuary
                IconButton(
                    onClick = onNavigateToMeditation,
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFF1E293B), CircleShape)
                        .testTag("open_meditation_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = "Guided Meditation Sanctuary",
                        tint = Color(0xFF38BDF8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (voiceConfig.continuousConversation) "Continuous conversation: ON" else "Continuous conversation: OFF",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StateBadge(state: SanaState) {
    val (label, bg, fg) = when (state) {
        SanaState.IDLE -> Triple("IDLE", Color(0xFF1E293B), Color(0xFF94A3B8))
        SanaState.LISTENING -> Triple("LISTENING", Color(0xFF083344), Color(0xFF00F0FF))
        SanaState.THINKING -> Triple("THINKING", Color(0xFF3B0764), Color(0xFFA855F7))
        SanaState.SPEAKING -> Triple("SPEAKING", Color(0xFF0C4A6E), Color(0xFF38BDF8))
        SanaState.ERROR -> Triple("PAUSE", Color(0xFF450A0A), Color(0xFFFCA5A5))
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = fg
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun QuickCommandChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF131A30),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.testTag("quick_chip_${label.lowercase().replace(" ", "_")}")
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ActionConfirmationDialog(
    action: PhoneAction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Confirm Action", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                text = "SANA is about to ${action.title} (${action.description}). Would you like to proceed?",
                color = Color(0xFFCBD5E1)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                modifier = Modifier.testTag("confirm_action_button")
            ) {
                Text("Proceed", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_action_button")
            ) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF161F38)
    )
}
