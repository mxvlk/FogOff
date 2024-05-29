package com.android.fogapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.android.fogapp.ui.map.MapScreen
import com.android.fogapp.ui.settings.SettingsScreen
import com.android.fogapp.ui.stats.StatsScreen

/**
 * The navigation host of the app. Created a nav host and adds all the screens.
 *
 * @param navController the nav host controller
 */
@Composable
fun NavigationHost(
    navController: NavHostController
) {

    NavHost(
        navController = navController,
        startDestination = NavigationItem.Home.route
    ){
        composable(NavigationItem.Home.route) {
            MapScreen()
        }

        composable(NavigationItem.Settings.route) {
            SettingsScreen()
        }

        composable(NavigationItem.Stats.route) {
            StatsScreen()
        }

    }
}