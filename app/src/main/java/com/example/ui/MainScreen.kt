package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MeditationScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.viewmodel.SanaViewModel

enum class SanaNavDestination(val label: String, val icon: ImageVector) {
    VOICE("Voice", Icons.Default.GraphicEq),
    SANCTUARY("Sanctuary", Icons.Default.SelfImprovement),
    DIALOGUE("Dialogue", Icons.AutoMirrored.Filled.Chat),
    MEMORY("Memory", Icons.Default.Psychology),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun MainScreen(viewModel: SanaViewModel) {
    var currentDestination by rememberSaveable { mutableStateOf(SanaNavDestination.VOICE) }

    // Handle back button on sub-screens to return to Voice screen
    BackHandler(enabled = currentDestination != SanaNavDestination.VOICE) {
        currentDestination = SanaNavDestination.VOICE
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF090D1C),
                tonalElevation = 6.dp
            ) {
                SanaNavDestination.entries.forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00F0FF),
                            selectedTextColor = Color(0xFF00F0FF),
                            indicatorColor = Color(0xFF162345),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_tab_${destination.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                SanaNavDestination.VOICE -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToMeditation = { currentDestination = SanaNavDestination.SANCTUARY }
                )
                SanaNavDestination.SANCTUARY -> MeditationScreen(
                    viewModel = viewModel
                )
                SanaNavDestination.DIALOGUE -> ChatScreen(
                    viewModel = viewModel
                )
                SanaNavDestination.MEMORY -> MemoryScreen(
                    viewModel = viewModel
                )
                SanaNavDestination.SETTINGS -> SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
