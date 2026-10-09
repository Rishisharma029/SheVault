package com.shevault.core.network

/**
 * Robust sealed result container ensuring all network calls are safely wrapped
 * and that no network error will cause an uncaught crash or infinite spinner.
 */
sealed interface NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>
    data class ApiError(
        val code: String,
        val message: String,
        val httpCode: Int = 0,
        val requestId: String? = null,
        val details: Map<String, Any?>? = null
    ) : NetworkResult<Nothing>
    data class NetworkFailure(val throwable: Throwable) : NetworkResult<Nothing>

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is ApiError || this is NetworkFailure

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is NetworkFailure -> throwable
        is ApiError -> ApiException(code, message, requestId, httpCode, details)
        else -> null
    }
}

inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(transform(data))
    is NetworkResult.ApiError -> this
    is NetworkResult.NetworkFailure -> this
}

inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) action(data)
    return this
}

inline fun <T> NetworkResult<T>.onError(action: (code: String, message: String) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.ApiError) action(code, message)
    return this
}

inline fun <T> NetworkResult<T>.onFailure(action: (Throwable) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.NetworkFailure) action(throwable)
    return this
}

