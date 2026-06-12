package com.rentsplit.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.rentsplit.ui.theme.LocalAppColors

/**
 * A custom modifier that applies a smooth, animated shimmer skeleton loader effect.
 * It is fully theme-aware and adapts automatically to Light and Dark modes.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val colors = LocalAppColors.current
    val transition = rememberInfiniteTransition(label = "shimmer")
    
    val shimmerPosition by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerPosition"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            colors.surface2,
            colors.surface3,
            colors.surface2
        ),
        start = Offset(shimmerPosition, shimmerPosition),
        end = Offset(shimmerPosition + 300f, shimmerPosition + 300f)
    )

    this.background(shimmerBrush)
}
