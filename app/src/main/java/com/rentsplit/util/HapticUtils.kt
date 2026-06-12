package com.rentsplit.util

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Utility object for performing standard haptic feedback effects consistently across RentSplit.
 */
object HapticUtils {

    /**
     * Light tap for regular click events (button presses, navigation, tab switches).
     */
    fun performClickHaptic(haptic: HapticFeedback) {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (e: Exception) {
            // Ignore if device doesn't support or permission is not granted
        }
    }

    /**
     * Slightly heavier haptic for confirmations, saves, successfully completed tasks.
     */
    fun performConfirmHaptic(haptic: HapticFeedback) {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Distinct feedback for errors, warnings, and invalid inputs.
     */
    fun performErrorHaptic(haptic: HapticFeedback) {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Heavy haptic feedback for destructive actions (deletions, resets, clear data).
     */
    fun performDeleteHaptic(haptic: HapticFeedback) {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
