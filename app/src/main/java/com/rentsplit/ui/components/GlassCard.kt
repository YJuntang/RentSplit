package com.rentsplit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rentsplit.ui.theme.LocalAppColors

/**
 * A glassmorphic card that creates a deep dark translucent surface with
 * a subtle gradient border glow — the premium look for RentSplit.
 *
 * @param modifier Modifier to apply
 * @param shape Shape of the card (defaults to RoundedCornerShape(16.dp) at call site)
 * @param backgroundColor The base fill color of the glass surface
 * @param borderColor The border glow color (defaults to Surface4 — dim highlight)
 * @param borderWidth Border thickness
 * @param content Content to display inside the glass surface
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    backgroundColor: Color = LocalAppColors.current.surface2.copy(alpha = 0.85f),
    borderColor: Color = LocalAppColors.current.surface4,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.9f),
                        backgroundColor.copy(alpha = 0.75f)
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor.copy(alpha = 0.6f),
                        borderColor.copy(alpha = 0.1f),
                        borderColor.copy(alpha = 0.4f)
                    )
                ),
                shape = shape
            ),
        content = content
    )
}
