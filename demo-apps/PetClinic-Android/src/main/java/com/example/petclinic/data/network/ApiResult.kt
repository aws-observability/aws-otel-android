package com.example.petclinic.data.network

/**
 * A generic wrapper for API responses that can represent success, error, or loading states
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val exception: ApiException) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}

/**
 * Custom exception for API errors
 */
sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkException(message: String, cause: Throwable? = null) : ApiException(message, cause)
    class HttpException(val code: Int, message: String) : ApiException(message)
    class ParseException(message: String, cause: Throwable? = null) : ApiException(message, cause)
    class UnknownException(message: String, cause: Throwable? = null) : ApiException(message, cause)
}

/**
 * Extension function to safely execute API calls and wrap results
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (e: Exception) {
        ApiResult.Error(
            when (e) {
                is java.net.UnknownHostException,
                is java.net.ConnectException,
                is java.net.SocketTimeoutException -> ApiException.NetworkException(
                    "Network error: ${e.message}",
                    e
                )
                is ApiException -> e
                else -> ApiException.UnknownException("Unknown error: ${e.message}", e)
            }
        )
    }
}
