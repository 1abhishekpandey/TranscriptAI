package com.abhishek.summaryai.util

import android.util.Log
import timber.log.Timber

/**
 * Logger utility for SummaryAI application
 * Tag: "SummaryAI"
 * Default level: VERBOSE
 */
object Logger {
    private const val TAG = "VideoSummaryAI"

    /**
     * Initialize the logger. Call this in Application.onCreate()
     */
    fun init() {
        if (Timber.treeCount == 0) {
            Timber.plant(object : Timber.DebugTree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    super.log(priority, TAG, message, t)
                }
            })
        }
        logI("Logger initialized")
    }

    /**
     * Log VERBOSE level message
     */
    fun logV(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(TAG).v(throwable, message)
        } else {
            Timber.tag(TAG).v(message)
        }
    }

    /**
     * Log DEBUG level message
     */
    fun logD(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(TAG).d(throwable, message)
        } else {
            Timber.tag(TAG).d(message)
        }
    }

    /**
     * Log INFO level message
     */
    fun logI(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(TAG).i(throwable, message)
        } else {
            Timber.tag(TAG).i(message)
        }
    }

    /**
     * Log WARNING level message
     */
    fun logW(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(TAG).w(throwable, message)
        } else {
            Timber.tag(TAG).w(message)
        }
    }

    /**
     * Log ERROR level message
     */
    fun logE(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(TAG).e(throwable, message)
        } else {
            Timber.tag(TAG).e(message)
        }
    }

    /**
     * Log method entry (verbose)
     */
    fun logEntry(methodName: String) {
        logV("→ Entering: $methodName")
    }

    /**
     * Log method exit (verbose)
     */
    fun logExit(methodName: String) {
        logV("← Exiting: $methodName")
    }
}
