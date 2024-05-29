package com.android.fogapp.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class to define all navigable screens of the app
 */
sealed class NavigationItem(
    var route: String,
    var icon: ImageVector,
    var iconSelected: ImageVector,
    var title: String
) {
        /**
         * The main screen
         */
        data object Home : NavigationItem(
            route = "home",
            icon = Icons.Outlined.Home,
            iconSelected = Icons.Filled.Home,
            title = "Home"
        )

        /**
         * The settings screen
         */
        data object Settings : NavigationItem(
            route = "settings",
            icon = Icons.Outlined.Settings,
            iconSelected = Icons.Filled.Settings,
            title = "Settings"
        )

        /**
         * The stats screen
         */
        data object Stats : NavigationItem(
            route = "stats",
            icon = Icons.Outlined.QueryStats,
            iconSelected = Icons.Filled.QueryStats,
            title = "Stats"
        )
}