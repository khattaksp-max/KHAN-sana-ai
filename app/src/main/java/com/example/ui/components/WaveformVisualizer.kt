package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    amplitude: Float,
    isSpeakingOrListening: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    barCount: Int = 24
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barWidth = (totalWidth / (barCount * 1.6f)).coerceIn(3f, 8f)
        val spacing = (totalWidth - (barWidth * barCount)) / (barCount - 1)

        val baseAmp = if (isSpeakingOrListening) amplitude.coerceIn(0.12f, 1f) else 0.05f

        for (i in 0 until barCount) {
            val progress = i.toFloat() / barCount.toFloat()
            // Sine harmonic wave calculation
            val wave = (sin(progress * 4f + phase) + 1f) / 2f
            val envelope = sin(progress * Math.PI.toFloat()) // Windowing so edges are smaller
            val barAmp = (wave * 0.45f + 0.15f) * envelope * baseAmp

            val calculatedHeight = (canvasHeight * barAmp).coerceIn(6f, canvasHeight * 0.95f)
            val x = i * (barWidth + spacing)
            val y = (canvasHeight - calculatedHeight) / 2f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00F0FF),
                        Color(0xFFA855F7),
                        Color(0xFF6366F1)
                    )
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, calculatedHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
