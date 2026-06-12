package com.rentsplit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rentsplit.ui.theme.MemberPalette

/**
 * Circular member avatar showing the first initial of the member's name,
 * coloured by the member's assigned color hex.
 *
 * @param name Member's full name
 * @param colorHex Hex color string (e.g. "#00D4FF")
 * @param size Avatar diameter
 * @param modifier Modifier
 */
@Composable
fun MemberAvatar(
    name: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val bgColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        MemberPalette.first()
    }

    Box(
        modifier = modifier
            .size(size)
            .background(bgColor.copy(alpha = 0.25f), CircleShape)
            .background(Color.Transparent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring
        Box(
            modifier = Modifier
                .size(size)
                .background(bgColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Inner solid
            Box(
                modifier = Modifier
                    .size(size * 0.82f)
                    .background(bgColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.38f).sp,
                        color = bgColor
                    )
                )
            }
        }
    }
}
