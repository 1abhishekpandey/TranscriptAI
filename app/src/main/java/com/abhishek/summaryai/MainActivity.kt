package com.abhishek.summaryai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.abhishek.summaryai.presentation.home.HomeScreen
import com.abhishek.summaryai.ui.theme.SummaryAITheme
import com.abhishek.summaryai.util.Logger
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for SummaryAI
 * Entry point for the application UI
 * Handles deep links from YouTube URLs
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val sharedUrl = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.logI("MainActivity: onCreate")

        // Handle deep link intent
        handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            SummaryAITheme {
                HomeScreen(initialUrl = sharedUrl.value)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Logger.logI("MainActivity: onNewIntent")

        // Handle new intent when activity is already running (singleTop mode)
        handleIntent(intent)
    }

    /**
     * Extracts YouTube URL from incoming intent
     * Handles both ACTION_VIEW (direct links) and ACTION_SEND (share)
     */
    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                // Direct link clicked (e.g., from browser or other app)
                intent.data?.let { uri ->
                    val url = uri.toString()
                    Logger.logI("MainActivity: Deep link (VIEW) received - $url")
                    sharedUrl.value = url
                }
            }
            Intent.ACTION_SEND -> {
                // Shared from another app (e.g., YouTube share button)
                if (intent.type == "text/plain") {
                    intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                        Logger.logI("MainActivity: Shared text (SEND) received - $sharedText")
                        // Extract YouTube URL from shared text
                        val url = extractYouTubeUrl(sharedText)
                        if (url != null) {
                            Logger.logI("MainActivity: Extracted YouTube URL - $url")
                            sharedUrl.value = url
                        } else {
                            Logger.logW("MainActivity: No YouTube URL found in shared text")
                        }
                    }
                }
            }
            else -> {
                Logger.logV("MainActivity: Intent action: ${intent?.action}")
            }
        }
    }

    /**
     * Extracts YouTube URL from shared text
     * Handles various YouTube URL formats
     */
    private fun extractYouTubeUrl(text: String): String? {
        // YouTube URL patterns
        val patterns = listOf(
            "https?://(?:www\\.)?youtube\\.com/watch\\?v=[\\w-]+".toRegex(),
            "https?://(?:www\\.)?youtube\\.com/\\S+".toRegex(),
            "https?://youtu\\.be/[\\w-]+".toRegex(),
            "https?://m\\.youtube\\.com/watch\\?v=[\\w-]+".toRegex()
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.value
            }
        }

        return null
    }

    override fun onStart() {
        super.onStart()
        Logger.logV("MainActivity: onStart")
    }

    override fun onResume() {
        super.onResume()
        Logger.logV("MainActivity: onResume")
    }

    override fun onPause() {
        super.onPause()
        Logger.logV("MainActivity: onPause")
    }

    override fun onStop() {
        super.onStop()
        Logger.logV("MainActivity: onStop")
    }

    override fun onDestroy() {
        Logger.logI("MainActivity: onDestroy")
        super.onDestroy()
    }
}
