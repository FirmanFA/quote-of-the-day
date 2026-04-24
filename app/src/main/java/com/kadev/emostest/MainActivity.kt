package com.kadev.emostest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kadev.emostest.ui.navigation.HomeRoute
import com.kadev.emostest.ui.QuoteNavigation
import com.kadev.emostest.ui.screen.home.HomeScreen
import com.kadev.emostest.ui.theme.EMOSTESTTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = true
        setContent {
            EMOSTESTTheme {
                QuoteNavigation()
            }
        }
    }
}

