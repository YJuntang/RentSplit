package com.rentsplit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rentsplit.data.model.Category
import com.rentsplit.ui.theme.CyanPrimary

/**
 * A pill-shaped chip showing a category's Material Icon and name with
 * a color-tinted background matching the category.
 */
@Composable
fun CategoryChip(
    category: Category?,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category?.colorHex ?: "#64748B"))
    } catch (e: Exception) {
        CyanPrimary
    }

    Row(
        modifier = modifier
            .background(categoryColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = CategoryIconHelper.getIconByName(category?.iconName ?: "Category"),
            contentDescription = category?.name ?: "Other",
            tint = categoryColor,
            modifier = Modifier.size(12.dp)
        )
        if (showLabel) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category?.name ?: "Other",
                style = MaterialTheme.typography.labelSmall,
                color = categoryColor
            )
        }
    }
}

/**
 * Small colored dot badge for category indicators in tight spaces.
 */
@Composable
fun CategoryDot(
    category: Category?,
    modifier: Modifier = Modifier,
    size: Int = 8
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category?.colorHex ?: "#64748B"))
    } catch (e: Exception) {
        CyanPrimary
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .background(categoryColor, CircleShape)
    )
}
