package com.kadev.emostest.domain.error

sealed class AppError : Exception() {
    data class NetworkError(val statusCode: Int?, override val message: String) : AppError()
    data class DatabaseError(override val message: String) : AppError()
    data class ServerError(val statusCode: Int, override val message: String) : AppError()
    data class ClientError(val statusCode: Int, override val message: String) : AppError()
    data class TimeoutError(override val message: String = "Request timeout") : AppError()
    data class ParseError(override val message: String = "Failed to parse response") : AppError()
    data class UnknownError(override val message: String = "An unexpected error occurred") : AppError()

    companion object {
        fun from(throwable: Throwable): AppError = when {
            throwable is AppError -> throwable
            throwable.message?.contains("timeout", ignoreCase = true) == true -> TimeoutError()
            throwable.message?.contains("failed to parse", ignoreCase = true) == true -> ParseError()
            else -> UnknownError(throwable.message ?: "Unknown error")
        }
    }
}
