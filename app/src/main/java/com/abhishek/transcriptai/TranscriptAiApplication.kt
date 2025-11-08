package com.abhishek.transcriptai

import android.app.Application
import com.abhishek.transcriptai.util.Logger
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for TranscriptAI
 * Entry point for Hilt dependency injection and app-level initialization
 */
@HiltAndroidApp
class TranscriptAiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Logger
        Logger.init()
        Logger.logI("TranscriptAiApplication: Application started")
        Logger.logD("TranscriptAiApplication: Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        Logger.logV("TranscriptAiApplication: Package: ${BuildConfig.APPLICATION_ID}")
    }

    override fun onTerminate() {
        Logger.logI("TranscriptAiApplication: Application terminated")
        super.onTerminate()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Logger.logW("TranscriptAiApplication: Low memory warning")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Logger.logD("TranscriptAiApplication: Trim memory - level: $level")
    }
}
