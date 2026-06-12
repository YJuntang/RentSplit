package com.rentsplit.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * A decoupled, global success/info event bus for snackbar notifications.
 * Allows any ViewModel to post messages that will be observed by the MainScreen Scaffold's SnackbarHost.
 */
object SnackbarManager {
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val messages = _messages.asSharedFlow()

    fun showMessage(message: String) {
        _messages.tryEmit(message)
    }
}
