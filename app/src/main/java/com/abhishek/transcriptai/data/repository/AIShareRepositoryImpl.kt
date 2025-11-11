package com.abhishek.transcriptai.data.repository

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.abhishek.transcriptai.domain.model.ShareTarget
import com.abhishek.transcriptai.domain.repository.AIShareRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of AIShareRepository for sharing content to AI applications.
 * Data layer - handles Android-specific implementation details.
 *
 * Sharing Strategy:
 * 1. Try ACTION_SEND intent directly to the target app (prefills text if supported)
 * 2. If ACTION_SEND not supported, fallback to clipboard + launch app
 * 3. Handle errors gracefully with user feedback via toasts
 */
class AIShareRepositoryImpl(
    private val context: Context
) : AIShareRepository {

    companion object {
        private const val TAG = "AIShareRepository"
    }

    /**
     * Share text content to the specified target application.
     * Uses ACTION_SEND with smart fallback to clipboard + launch.
     */
    override suspend fun shareToApp(text: String, target: ShareTarget): Result<Unit> =
        withContext(Dispatchers.Main) {
            try {
                when (target) {
                    ShareTarget.CLIPBOARD -> {
                        // Special case: clipboard only
                        copyToClipboard(text, target.displayName)
                        Result.success(Unit)
                    }

                    ShareTarget.CHATGPT, ShareTarget.CLAUDE -> {
                        // Check if app is installed
                        if (!isAppInstalled(target)) {
                            showToast("${target.displayName} is not installed")
                            return@withContext Result.failure(
                                Exception("${target.displayName} not installed")
                            )
                        }

                        // Try ACTION_SEND first
                        if (tryActionSendIntent(text, target)) {
                            Log.d(TAG, "Successfully shared to ${target.displayName} via ACTION_SEND")
                            Result.success(Unit)
                        } else {
                            // Fallback: Copy to clipboard and launch app
                            Log.d(TAG, "${target.displayName} doesn't support ACTION_SEND, using clipboard fallback")
                            copyToClipboard(text, target.displayName)
                            launchApp(target)
                            Result.success(Unit)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sharing to ${target.displayName}", e)
                showToast("Error sharing to ${target.displayName}: ${e.message}")
                Result.failure(e)
            }
        }

    /**
     * Check if the target application is installed.
     */
    override fun isAppInstalled(target: ShareTarget): Boolean {
        if (target == ShareTarget.CLIPBOARD) return true

        return try {
            context.packageManager.getPackageInfo(target.packageName, 0)
            true
        } catch (e: Exception) {
            Log.d(TAG, "${target.displayName} not installed")
            false
        }
    }

    /**
     * Check if the target application supports ACTION_SEND intent.
     */
    override fun supportsDirectSharing(target: ShareTarget): Boolean {
        if (target == ShareTarget.CLIPBOARD) return false

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            setPackage(target.packageName)
        }

        return sendIntent.resolveActivity(context.packageManager) != null
    }

    /**
     * Copy text to system clipboard.
     */
    override fun copyToClipboard(text: String, label: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)

            showToast("Text copied to clipboard. Ready to paste in $label")
            Log.d(TAG, "Copied ${text.length} characters to clipboard for $label")
        } catch (e: Exception) {
            Log.e(TAG, "Error copying to clipboard", e)
            showToast("Error copying to clipboard")
        }
    }

    /**
     * Attempt to share text via ACTION_SEND intent directly to the target app.
     * Returns true if successful, false if the app doesn't support ACTION_SEND.
     */
    private fun tryActionSendIntent(text: String, target: ShareTarget): Boolean {
        return try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                setPackage(target.packageName)

                // Different flag combinations for different apps
                when (target) {
                    ShareTarget.CHATGPT -> {
                        // ChatGPT needs to be restarted fresh to properly receive ACTION_SEND
                        // FLAG_ACTIVITY_NEW_TASK: Start in a new task
                        // FLAG_ACTIVITY_CLEAR_TASK: Clear entire task stack, forcing fresh start
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        Log.d(TAG, "Using CLEAR_TASK flags for ChatGPT to ensure fresh start")
                    }
                    ShareTarget.CLAUDE -> {
                        // Claude handles existing instances well with CLEAR_TOP
                        // FLAG_ACTIVITY_NEW_TASK: Start in a new task
                        // FLAG_ACTIVITY_CLEAR_TOP: Clear activities on top to deliver intent to existing activity
                        // FLAG_ACTIVITY_SINGLE_TOP: Ensure only one instance exists
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        Log.d(TAG, "Using CLEAR_TOP flags for Claude")
                    }
                    else -> {
                        // Default behavior
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
            }

            // Check if the intent can be resolved
            if (sendIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(sendIntent)
                Log.d(TAG, "Launched ${target.displayName} via ACTION_SEND with ${text.length} characters")
                true
            } else {
                Log.d(TAG, "${target.displayName} doesn't resolve ACTION_SEND intent")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error with ACTION_SEND for ${target.displayName}", e)
            false
        }
    }

    /**
     * Launch the target application.
     */
    private fun launchApp(target: ShareTarget) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(target.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                Log.d(TAG, "Launched ${target.displayName} app")
            } else {
                Log.e(TAG, "Could not get launch intent for ${target.displayName}")
                showToast("Could not launch ${target.displayName}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching ${target.displayName}", e)
            showToast("Error launching ${target.displayName}")
        }
    }

    /**
     * Show a toast message to the user.
     */
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
