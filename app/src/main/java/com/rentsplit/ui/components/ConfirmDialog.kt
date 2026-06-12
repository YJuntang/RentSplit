package com.rentsplit.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rentsplit.ui.theme.LocalAppColors

/**
 * A beautiful, premium, custom-styled confirmation dialog for validating critical or destructive actions.
 */
@Composable
fun ConfirmDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!show) return

    val colors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) MaterialTheme.colorScheme.error else colors.cyanPrimary,
                    contentColor = if (isDestructive) MaterialTheme.colorScheme.onError else colors.surface0
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.bounceClick()
            ) {
                Text(
                    text = confirmText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.textSecondary
                ),
                modifier = Modifier.bounceClick()
            ) {
                Text(
                    text = dismissText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = colors.surface1,
        tonalElevation = 6.dp
    )
}
