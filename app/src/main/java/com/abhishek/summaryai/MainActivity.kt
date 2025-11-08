package com.abhishek.summaryai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.abhishek.summaryai.presentation.home.HomeScreen
import com.abhishek.summaryai.ui.theme.SummaryAITheme
import com.abhishek.summaryai.util.Logger
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for SummaryAI
 * Entry point for the application UI
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.logI("MainActivity: onCreate")

        enableEdgeToEdge()
        setContent {
            SummaryAITheme {
                HomeScreen()
            }
        }
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
