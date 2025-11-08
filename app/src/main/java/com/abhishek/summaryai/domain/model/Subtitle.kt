package com.abhishek.summaryai.domain.model

/**
 * Domain model representing a subtitle result
 * This is a simple model since we're just dealing with plain text
 */
data class SubtitleResult(
    val text: String,
    val videoId: String,
    val videoTitle: String? = null
)

/**
 * Sealed class representing the result of an operation
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
