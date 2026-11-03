package com.abhishek.transcriptai.util.share

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

/**
 * Helper for share UI that needs to know which apps are on the device.
 * Sharing itself goes through AIShareRepository.
 */
object ShareHelper {
    private const val TAG = "TranscriptAI-ShareHelper"

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
}
