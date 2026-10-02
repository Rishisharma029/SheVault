package com.shevault.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Generic Result wrapper for domain and data operations.
 */
sealed interface SafetyResult<out T> {
    data class Success<T>(val data: T) : SafetyResult<T>
    data class Error(val exception: Throwable, val message: String = exception.message.orEmpty()) : SafetyResult<Nothing>
    data object Loading : SafetyResult<Nothing>
}

interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}
