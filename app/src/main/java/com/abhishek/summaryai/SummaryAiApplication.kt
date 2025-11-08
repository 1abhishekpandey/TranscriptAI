package com.abhishek.summaryai

import android.app.Application
import com.abhishek.summaryai.util.Logger
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for SummaryAI
 * Entry point for Hilt dependency injection and app-level initialization
 */
@HiltAndroidApp
class SummaryAiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Logger
        Logger.init()
        Logger.logI("SummaryAiApplication: Application started")
        Logger.logD("SummaryAiApplication: Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        Logger.logV("SummaryAiApplication: Package: ${BuildConfig.APPLICATION_ID}")
    }

    override fun onTerminate() {
        Logger.logI("SummaryAiApplication: Application terminated")
        super.onTerminate()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Logger.logW("SummaryAiApplication: Low memory warning")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Logger.logD("SummaryAiApplication: Trim memory - level: $level")
    }
}
