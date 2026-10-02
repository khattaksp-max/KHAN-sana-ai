package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.MeditationSession
import com.example.ui.components.BreathingGuideCircle
import com.example.ui.components.GlassCard
import com.example.ui.viewmodel.SanaViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MeditationScreen(
    viewModel: SanaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessions by viewModel.meditationSessions.collectAsState()
    val isGenerating by viewModel.isGeneratingMeditation.collectAsState()
    val statusText by viewModel.meditationStatusText.collectAsState()
    val activeSession by viewModel.activeMeditation.collectAsState()

    var customTheme by remember { mutableStateOf("") }
    var selectedPresetTheme by remember { mutableStateOf("Deep Calm & Stress Relief") }
    var selectedDuration by remember { mutableIntStateOf(5) }
    var selectedResolution by remember { mutableStateOf("1K") } // Affordance for 1K, 2K, 4K

    val presetThemes = listOf(
        "Deep Calm & Stress Relief",
        "Celestial Sleep Journey",
        "Sacred Forest Breathwork",
        "Morning Clarity & Energy",
        "Loving-Kindness & Healing"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF060913),
                        Color(0xFF0F172A),
                        Color(0xFF080D1A)
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Meditation Sanctuary",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Bespoke visuals & personalized voiceovers by SANA",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8)
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Custom Session Creator Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_meditation_creator_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Craft Custom Session",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Choose your theme, duration, and visual resolution",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Preset themes chips
                        Text(
                            text = "Theme / Intention",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presetThemes.forEach { theme ->
                                val isSelected = selectedPresetTheme == theme && customTheme.isBlank()
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedPresetTheme = theme
                                        customTheme = ""
                                    },
                                    label = { Text(theme, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF0369A1),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFF1E293B),
                                        labelColor = Color(0xFFCBD5E1)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom Theme text input
                        OutlinedTextField(
                            value = customTheme,
                            onValueChange = { customTheme = it },
                            placeholder = { Text("Or describe your custom mindfulness goal...", color = Color(0xFF64748B), fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_theme_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Duration Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Duration:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFFE2E8F0),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            listOf(3, 5, 10, 15).forEach { min ->
                                val isSelected = selectedDuration == min
                                Surface(
                                    onClick = { selectedDuration = min },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Color(0xFFA855F7) else Color(0xFF1E293B),
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .testTag("duration_${min}m")
                                ) {
                                    Text(
                                        text = "${min}m",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Image Resolution Affordance (1K, 2K, 4K) using gemini-3-pro-image-preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HighQuality,
                                contentDescription = null,
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Visual Quality (Gemini 3 Pro):",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFFE2E8F0),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            listOf("1K", "2K", "4K").forEach { res ->
                                val isSelected = selectedResolution == res
                                Surface(
                                    onClick = { selectedResolution = res },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .testTag("resolution_$res")
                                ) {
                                    Text(
                                        text = res,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Generate Button
                        Button(
                            onClick = {
                                val finalTheme = if (customTheme.isNotBlank()) customTheme else selectedPresetTheme
                                viewModel.generateCustomMeditation(
                                    theme = finalTheme,
                                    durationMinutes = selectedDuration,
                                    resolution = selectedResolution,
                                    cacheDir = context.cacheDir
                                )
                            },
                            enabled = !isGenerating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_meditation_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0284C7),
                                disabledContainerColor = Color(0xFF1E293B)
                            )
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Generating bespoke session...", color = Color.White)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Generate Session with SANA AI",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (isGenerating && statusText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Saved Sacred Journeys",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // List of saved sessions
            if (sessions.isEmpty()) {
                item {
                    Text(
                        text = "No saved meditations yet. Generate your first one above!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF64748B)
                        ),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(sessions, key = { it.id }) { session ->
                    SessionCard(
                        session = session,
                        onPlay = { viewModel.playMeditationSession(session) },
                        onDelete = { /* can delete via viewmodel */ }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Active Session Bottom Sheet Player
        activeSession?.let { currentSession ->
            ActiveMeditationPlayerSheet(
                session = currentSession,
                viewModel = viewModel,
                onDismiss = { viewModel.closeActiveMeditation() }
            )
        }
    }
}

@Composable
fun SessionCard(
    session: MeditationSession,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("session_card_${session.id}")
            .clickable(onClick = onPlay)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork thumbnail or placeholder
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                if (session.imageBase64OrUri.isNotBlank()) {
                    AsyncImage(
                        model = session.imageBase64OrUri,
                        contentDescription = "Session artwork",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${session.durationMinutes} min",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFA855F7),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${session.resolution} Visual",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF38BDF8)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = session.script,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8)
                    ),
                    maxLines = 2
                )
            }

            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF0369A1), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Session",
                    tint = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveMeditationPlayerSheet(
    session: MeditationSession,
    viewModel: SanaViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isNarrating by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0B0F1D)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "${session.durationMinutes} Minutes • ${session.resolution} High-Resolution Visual",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF38BDF8)
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bespoke Generated Artwork (using gemini-3-pro-image-preview)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                if (session.imageBase64OrUri.isNotBlank()) {
                    AsyncImage(
                        model = session.imageBase64OrUri,
                        contentDescription = "Sacred Meditation Visual",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Visual placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF0369A1),
                                        Color(0xFF0F172A)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        BreathingGuideCircle(isPlaying = isNarrating)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Guided Breathing Visualizer
            BreathingGuideCircle(isPlaying = isNarrating)

            Spacer(modifier = Modifier.height(14.dp))

            // Soothing Voiceover Script preview
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SANA Guided Voiceover Script",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFFA855F7),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = session.script,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFE2E8F0),
                            lineHeight = 20.sp
                        ),
                        maxLines = 4
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Play / Pause Voiceover Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (isNarrating) {
                            viewModel.stopMeditationVoiceover()
                            isNarrating = false
                        } else {
                            viewModel.playMeditationVoiceover(session.script)
                            isNarrating = true
                        }
                    },
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("play_pause_voiceover_button"),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNarrating) Color(0xFFEF4444) else Color(0xFF0284C7)
                    )
                ) {
                    Icon(
                        imageVector = if (isNarrating) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isNarrating) "Pause Guided Voiceover" else "Play Guided Voiceover",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
