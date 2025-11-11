package com.abhishek.transcriptai.domain.repository

import com.abhishek.transcriptai.domain.model.ShareTarget

/**
 * Repository interface for sharing content to AI applications.
 * Domain layer - defines contract without Android implementation details.
 */
interface AIShareRepository {

    /**
     * Share text content to the specified target application.
     * Attempts to use ACTION_SEND intent first, falls back to clipboard + launch if needed.
     *
     * @param text The text content to share
     * @param target The target application to share to
     * @return Result indicating success or failure with error message
     */
    suspend fun shareToApp(text: String, target: ShareTarget): Result<Unit>

    /**
     * Check if the specified application is installed on the device.
     *
     * @param target The target application to check
     * @return true if the app is installed, false otherwise
     */
    fun isAppInstalled(target: ShareTarget): Boolean

    /**
     * Check if the specified application supports ACTION_SEND intent for text sharing.
     *
     * @param target The target application to check
     * @return true if the app supports ACTION_SEND, false otherwise
     */
    fun supportsDirectSharing(target: ShareTarget): Boolean

    /**
     * Copy text to system clipboard.
     *
     * @param text The text to copy
     * @param label Optional label for the clipboard entry
     */
    fun copyToClipboard(text: String, label: String = "Text")
}
