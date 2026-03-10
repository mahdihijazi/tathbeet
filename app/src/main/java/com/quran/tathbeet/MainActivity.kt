package com.quran.tathbeet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.quran.tathbeet.app.AppContainer
import com.quran.tathbeet.ui.TathbeetApp
import com.quran.tathbeet.ui.theme.TathbeetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appContainer = remember { AppContainer(applicationContext) }
            TathbeetTheme {
                TathbeetApp(appContainer = appContainer)
            }
        }
    }
}
