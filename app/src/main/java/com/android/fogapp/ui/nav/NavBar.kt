package com.android.fogapp.ui.nav

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState


/**
 * Composable that implements the navigation bottom bar
 *
 * @param navController the nav host controller
 */
@Composable
fun NavBar(
    navController: NavController
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Stats,
        NavigationItem.Settings
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    if(currentRoute == item.route) {
                        Icon(item.iconSelected, "")
                    }
                    else {
                        Icon(item.icon, "")
                    }

                },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    selectedItem = index
                    navController.navigate(item.route)
                },
                alwaysShowLabel = false
            )
        }
    }
}