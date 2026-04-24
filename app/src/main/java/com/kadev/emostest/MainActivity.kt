package com.kadev.emostest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kadev.emostest.ui.QuoteNavigation
import com.kadev.emostest.ui.theme.EMOSTESTTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EMOSTESTTheme {
                QuoteNavigation()
            }
        }
    }
}

