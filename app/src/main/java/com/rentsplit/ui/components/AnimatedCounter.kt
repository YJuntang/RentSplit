package com.rentsplit.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * Animated monetary amount display that smoothly transitions between values
 * using a spring animation. Ideal for the hero total on the dashboard.
 *
 * @param targetValue The target monetary value (will animate toward it)
 * @param prefix Currency prefix (e.g. "RM ")
 * @param style Text style
 * @param color Text color
 * @param modifier Modifier
 */
@Composable
fun AnimatedCounter(
    targetValue: Double,
    prefix: String = "RM ",
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayMedium,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Text(
        text = "$prefix${"%.2f".format(animatedValue.value)}",
        style = style,
        color = color,
        modifier = modifier
    )
}
