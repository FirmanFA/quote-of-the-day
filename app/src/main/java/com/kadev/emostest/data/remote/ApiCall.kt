package com.kadev.emostest.data.remote

import com.kadev.emostest.domain.error.AppError
import retrofit2.HttpException

fun HttpException.toAppError(): AppError = when (code()) {
    in 400..499 -> AppError.ClientError(code(), message())
    in 500..599 -> AppError.ServerError(code(), message())
    else -> AppError.NetworkError(code(), message())
}

suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: HttpException) {
    Result.failure(e.toAppError())
} catch (e: Exception) {
    Result.failure(AppError.from(e))
}

