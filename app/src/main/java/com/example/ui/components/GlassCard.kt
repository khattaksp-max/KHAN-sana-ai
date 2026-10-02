package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    backgroundColor: Color = Color(0xFF11172A).copy(alpha = 0.72f),
    borderColor: Color = Color(0xFF38BDF8).copy(alpha = 0.22f),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(
            borderWidth,
            Brush.linearGradient(
                colors = listOf(
                    borderColor,
                    Color(0xFFA855F7).copy(alpha = 0.18f),
                    Color(0xFF38BDF8).copy(alpha = 0.08f)
                )
            )
        ),
        shadowElevation = 0.dp
    ) {
        Box {
            content()
        }
    }
}
