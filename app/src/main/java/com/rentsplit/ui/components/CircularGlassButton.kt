package com.rentsplit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rentsplit.ui.theme.LocalAppColors

@Composable
fun CircularGlassButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 22.dp,
    buttonSize: Dp = 40.dp,
    tint: Color = LocalAppColors.current.textPrimary
) {
    val colors = LocalAppColors.current
    Box(
        modifier = modifier
            .size(buttonSize)
            .clip(CircleShape)
            // Translucent glass look
            .background(colors.surface3.copy(alpha = 0.4f))
            // Subtle edge highlight (inner shadow/border)
            .border(0.5.dp, colors.textPrimary.copy(alpha = 0.15f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}
