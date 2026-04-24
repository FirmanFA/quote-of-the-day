package com.kadev.emostest.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kadev.emostest.ui.navigation.FavoriteQuoteRoute
import com.kadev.emostest.ui.navigation.HomeRoute
import com.kadev.emostest.ui.screen.favorite.FavoriteQuoteScreen
import com.kadev.emostest.ui.screen.home.HomeScreen

@Composable
fun QuoteNavigation() {
    val navController = rememberNavController()
    val startDestination = HomeRoute

    NavHost(navController = navController, startDestination = startDestination) {
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToFav = {
                    navController.navigate(FavoriteQuoteRoute)
                },
            )
        }
        composable<FavoriteQuoteRoute> {
            FavoriteQuoteScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}