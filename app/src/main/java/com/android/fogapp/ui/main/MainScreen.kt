package com.android.fogapp.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.android.fogapp.R
import com.android.fogapp.ui.nav.NavBar
import com.android.fogapp.ui.nav.NavigationHost
import com.android.fogapp.util.hasLocationPermission
import com.android.fogapp.ui.nav.NavigationItem


/**
 * Composable of the main screen, hosting all other screens
 *
 * @param viewModel the view model of the main screen
 * @param navController the nav host controller
 */
@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val context = LocalContext.current
    val tracking = viewModel.tracking.value
    val currentNavRoute by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            NavBar(navController = navController)
        },
        content = { padding ->
            Column(modifier = Modifier.fillMaxSize()) {
                Row {
                    Box(modifier = Modifier.padding(padding)) {
                        NavigationHost(navController = navController)
                    }
                }
            }
        },
        floatingActionButton = {
              
              if (currentNavRoute?.destination?.route.toString() == NavigationItem.Home.route){

              ExtendedFloatingActionButton(
                  text = { TrackingText(tracking = tracking) },
                  icon = { TrackingIcon(tracking = tracking) },
                  containerColor = trackingContainerColor(tracking = tracking),
                  onClick = {
                      viewModel.onEvent(
                          event = MainScreenEvent.OnStartTrackingClick,
                          context = context
                      )
                  })
              }

        }
    )

}

/**
 * Composable for the tracking icon
 *
 * @param tracking is the app currently tracking
 * @return the corresponding icon
 */
@Composable
private fun TrackingIcon(tracking: Boolean) {
    if(tracking){
        return Icon(
            Icons.Outlined.LocationOff,
            contentDescription = stringResource(id = R.string.tracking_icon_off_desc)
        )
    }
    return Icon(
        Icons.Outlined.LocationOn,
        contentDescription = stringResource(id = R.string.tracking_icon_on_desc)
    )
}

/**
 * Composable for the tracking container color
 *
 * @param tracking is the app currently tracking
 * @return the corresponding color
 */
@Composable
private fun trackingContainerColor(tracking: Boolean): androidx.compose.ui.graphics.Color {
    return if(tracking) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
}

/**
 * Composable for the tracking text
 *
 * @param tracking is the app currently tracking
 * @return the corresponding text
 */
@Composable
private fun TrackingText(tracking: Boolean) {

    return Text(
        text = stringResource(
            id = if(tracking) R.string.stop_tracking else R.string.start_tracking)
    )

}