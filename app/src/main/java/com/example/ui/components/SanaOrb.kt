package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.SanaState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SanaOrb(
    state: SanaState,
    amplitude: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbInfinite")

    // Slow organic breathing
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // Continuous rotation for cosmic aura
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Ripple wave for speaking / listening
    val rippleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple"
    )

    // Dynamic amplitude scale
    val animatedAmp = remember { Animatable(0f) }
    LaunchedEffect(amplitude) {
        animatedAmp.animateTo(
            targetValue = amplitude.coerceIn(0f, 1f),
            animationSpec = tween(100)
        )
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .testTag("sana_animated_orb")
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2.8f
            val reactiveBoost = (animatedAmp.value * 28f)
            val effectiveRadius = (baseRadius * breathScale) + reactiveBoost

            drawOrbAtmosphere(
                center = center,
                baseRadius = effectiveRadius,
                state = state,
                rotationAngle = rotationAngle,
                rippleProgress = rippleProgress,
                amplitude = animatedAmp.value
            )
        }
    }
}

private fun DrawScope.drawOrbAtmosphere(
    center: Offset,
    baseRadius: Float,
    state: SanaState,
    rotationAngle: Float,
    rippleProgress: Float,
    amplitude: Float
) {
    // State-specific palette
    val (colorOuter, colorCore, colorGlow) = when (state) {
        SanaState.IDLE -> Triple(
            Color(0xFF6B46C1).copy(alpha = 0.45f), // Purple glow
            Color(0xFF38BDF8),                     // Cyan core
            Color(0xFF818CF8)                      // Indigo aura
        )
        SanaState.LISTENING -> Triple(
            Color(0xFF00F0FF).copy(alpha = 0.7f),  // Bright electric cyan
            Color(0xFFE0F7FA),                     // White-cyan intense core
            Color(0xFF00D4FF)                      // High energy neon
        )
        SanaState.THINKING -> Triple(
            Color(0xFFA855F7).copy(alpha = 0.6f),  // Ethereal violet
            Color(0xFFF472B6),                     // Rose light
            Color(0xFFC084FC)                      // Lavender aura
        )
        SanaState.SPEAKING -> Triple(
            Color(0xFF06B6D4).copy(alpha = 0.65f), // Cyan pulse
            Color(0xFFF9A8D4),                     // Warm soothing rose-pink
            Color(0xFF818CF8)                      // Melodic purple
        )
        SanaState.ERROR -> Triple(
            Color(0xFFF87171).copy(alpha = 0.4f),  // Soft gentle rose
            Color(0xFFFCA5A5),                     // Calming pink
            Color(0xFFFEF3C7)                      // Warm amber
        )
    }

    // 1. Expanding outer ripples if listening or speaking
    if (state == SanaState.LISTENING || state == SanaState.SPEAKING) {
        val rippleRadius = baseRadius + (rippleProgress * 65f) + (amplitude * 35f)
        val rippleAlpha = (1f - rippleProgress) * 0.45f
        drawCircle(
            color = colorGlow.copy(alpha = rippleAlpha.coerceIn(0f, 1f)),
            radius = rippleRadius,
            center = center,
            style = Stroke(width = 2.5f)
        )
    }

    // 2. Soft outer ambient light glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                colorOuter.copy(alpha = 0.5f),
                colorGlow.copy(alpha = 0.25f),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius * 1.55f
        ),
        radius = baseRadius * 1.55f,
        center = center
    )

    // 3. Orbital light flares
    val radAngle = Math.toRadians(rotationAngle.toDouble())
    val flareDist = baseRadius * 0.65f
    val flareX = center.x + (cos(radAngle) * flareDist).toFloat()
    val flareY = center.y + (sin(radAngle) * flareDist).toFloat()

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(colorCore.copy(alpha = 0.9f), Color.Transparent),
            center = Offset(flareX, flareY),
            radius = baseRadius * 0.6f
        ),
        radius = baseRadius * 0.6f,
        center = Offset(flareX, flareY)
    )

    // 4. Core glowing sphere
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                colorCore,
                colorGlow,
                colorOuter
            ),
            center = center,
            radius = baseRadius
        ),
        radius = baseRadius,
        center = center
    )

    // 5. Crisp inner glass rim
    drawCircle(
        color = Color.White.copy(alpha = 0.35f),
        radius = baseRadius,
        center = center,
        style = Stroke(width = 1.8f)
    )

    // 6. Delicate starlight sparkles in core
    val sparkles = listOf(
        Pair(0.2f, -0.3f),
        Pair(-0.25f, 0.2f),
        Pair(0.35f, 0.25f),
        Pair(-0.15f, -0.2f)
    )
    sparkles.forEach { (dx, dy) ->
        val spCenter = Offset(center.x + baseRadius * dx, center.y + baseRadius * dy)
        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = 2.5f,
            center = spCenter
        )
    }
}
