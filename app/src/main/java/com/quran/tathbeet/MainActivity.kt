package com.quran.tathbeet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.quran.tathbeet.ui.TathbeetApp
import com.quran.tathbeet.ui.theme.TathbeetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TathbeetTheme {
                TathbeetApp()
            }
        }
    }
}
