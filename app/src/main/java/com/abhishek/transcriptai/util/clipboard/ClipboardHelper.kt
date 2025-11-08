package com.abhishek.transcriptai.util.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.abhishek.transcriptai.util.Logger

/**
 * Utility object for clipboard operations in the TranscriptAI application.
 * Provides thread-safe methods for copying and retrieving text from the system clipboard.
 */
object ClipboardHelper {

    /**
     * Copies text to clipboard and shows a toast message.
     *
     * This method is thread-safe and handles exceptions gracefully.
     * A toast notification is displayed to inform the user of the operation result.
     *
     * @param context Android context required for accessing the clipboard service
     * @param text Text to copy to clipboard
     * @param label Label for the clipboard entry (default: "Subtitle")
     */
    fun copyToClipboard(context: Context, text: String, label: String = "Subtitle") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)

            Logger.logI("ClipboardHelper: Copied ${text.length} characters to clipboard")
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Logger.logE("ClipboardHelper: Failed to copy to clipboard", e)
            Toast.makeText(context, "Failed to copy: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Gets text from clipboard.
     *
     * This method is thread-safe and handles exceptions gracefully.
     *
     * @param context Android context required for accessing the clipboard service
     * @return Text from clipboard or null if unavailable or empty
     */
    fun getFromClipboard(context: Context): String? {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboard.primaryClip

            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString()
                Logger.logD("ClipboardHelper: Retrieved ${text?.length ?: 0} characters from clipboard")
                text
            } else {
                Logger.logD("ClipboardHelper: Clipboard is empty")
                null
            }
        } catch (e: Exception) {
            Logger.logE("ClipboardHelper: Failed to get from clipboard", e)
            null
        }
    }
}
