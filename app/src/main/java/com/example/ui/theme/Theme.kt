package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val SanaDarkColorScheme = darkColorScheme(
    primary = SanaCyanPrimary,
    onPrimary = Color(0xFF03264A),
    primaryContainer = Color(0xFF034A7A),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = SanaPurpleSecondary,
    onSecondary = Color(0xFF381E72),
    secondaryContainer = Color(0xFF4F378B),
    onSecondaryContainer = Color(0xFFEADDFF),
    tertiary = SanaAccentRose,
    background = SanaDarkBackground,
    onBackground = SanaTextPrimary,
    surface = SanaDarkSurface,
    onSurface = SanaTextPrimary,
    surfaceVariant = SanaDarkSurfaceVariant,
    onSurfaceVariant = SanaTextSecondary,
    outline = SanaBorderGlow
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // SANA AI is designed with an immersive dark aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SanaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
