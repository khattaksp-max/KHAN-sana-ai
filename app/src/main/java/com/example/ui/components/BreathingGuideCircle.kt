package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun BreathingGuideCircle(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    var breathPhase by remember { mutableStateOf("Breathe In") }
    val scale = remember { Animatable(0.7f) }

    LaunchedEffect(isPlaying) {
        if (!isPlaying) {
            breathPhase = "Paused"
            scale.snapTo(0.7f)
            return@LaunchedEffect
        }

        while (true) {
            // Inhale (4s)
            breathPhase = "Inhale"
            scale.animateTo(1.15f, animationSpec = tween(4000, easing = FastOutSlowInEasing))

            // Hold (4s)
            breathPhase = "Hold"
            delay(4000)

            // Exhale (6s)
            breathPhase = "Exhale"
            scale.animateTo(0.7f, animationSpec = tween(6000, easing = FastOutSlowInEasing))

            // Rest (2s)
            breathPhase = "Rest"
            delay(2000)
        }
    }

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val currentRadius = (size.minDimension / 2.5f) * scale.value

            // Outer soft glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.35f),
                        Color(0xFFA855F7).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * 1.35f
                ),
                radius = currentRadius * 1.35f,
                center = center
            )

            // Main breathing orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE0F2FE).copy(alpha = 0.85f),
                        Color(0xFF38BDF8).copy(alpha = 0.55f),
                        Color(0xFF1E293B).copy(alpha = 0.3f)
                    ),
                    center = center,
                    radius = currentRadius
                ),
                radius = currentRadius,
                center = center
            )

            // Stroke outline
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = 0.7f),
                radius = currentRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = breathPhase,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isPlaying) "Follow the rhythm" else "Tap play to start",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            )
        }
    }
}
