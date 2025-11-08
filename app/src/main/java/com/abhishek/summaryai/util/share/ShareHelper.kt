package com.abhishek.summaryai.util.share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast

/**
 * Helper object for sharing text to various apps
 */
object ShareHelper {
    private const val TAG = "ShareHelper"

    // Package names for popular AI apps
    const val CHATGPT_PACKAGE = "com.openai.chatgpt"
    const val CLAUDE_PACKAGE = "com.anthropic.claude"

    /**
     * Check if an app is installed on the device
     *
     * @param context Android context
     * @param packageName Package name of the app to check
     * @return true if the app is installed, false otherwise
     */
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d(TAG, "App not installed: $packageName")
            false
        }
    }

    /**
     * Share text to a specific app by package name
     *
     * For ChatGPT and Claude apps, this copies the text to clipboard first,
     * then launches the app. The user can then paste the text into the app.
     *
     * @param context Android context
     * @param text Text to share
     * @param packageName Package name of the target app
     * @return true if sharing was initiated successfully, false otherwise
     */
    fun shareToApp(context: Context, text: String, packageName: String): Boolean {
        return try {
            if (!isAppInstalled(context, packageName)) {
                Toast.makeText(
                    context,
                    "App is not installed",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }

            // For ChatGPT and Claude, copy to clipboard first
            if (packageName == CHATGPT_PACKAGE || packageName == CLAUDE_PACKAGE) {
                copyToClipboard(context, text, getAppDisplayName(packageName))
            }

            // Launch the app
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                Log.d(TAG, "Successfully launched $packageName with ${text.length} characters in clipboard")
                true
            } else {
                // Fallback to ACTION_SEND
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    setPackage(packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Log.d(TAG, "Successfully shared ${text.length} characters to $packageName")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing to app: $packageName", e)
            Toast.makeText(
                context,
                "Error sharing to app: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }

    /**
     * Copy text to clipboard
     *
     * @param context Android context
     * @param text Text to copy
     * @param label Label for the clipboard item
     */
    private fun copyToClipboard(context: Context, text: String, label: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(
                context,
                "Text copied to clipboard. Ready to paste in $label",
                Toast.LENGTH_LONG
            ).show()

            Log.d(TAG, "Copied ${text.length} characters to clipboard for $label")
        } catch (e: Exception) {
            Log.e(TAG, "Error copying to clipboard", e)
        }
    }

    /**
     * Get the display name for a package
     *
     * @param packageName Package name
     * @return Human-readable app name
     */
    fun getAppDisplayName(packageName: String): String {
        return when (packageName) {
            CHATGPT_PACKAGE -> "ChatGPT"
            CLAUDE_PACKAGE -> "Claude"
            else -> packageName
        }
    }
}
