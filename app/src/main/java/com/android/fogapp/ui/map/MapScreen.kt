package com.android.fogapp.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.fogapp.R
import com.android.fogapp.util.PermissionUtil
import com.android.fogapp.util.hasLocationPermission
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

/**
 * Composable that shows a map using google maps
 *
 * @param viewModel the view model of the map screen
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapScreenViewModel = hiltViewModel(),
) {

    var isMapLoaded by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()
    val locationSource by remember { mutableStateOf(null) }
    var boxHeightDp by remember { mutableStateOf(130.dp) }
    val localDensity = LocalDensity.current
    val context = LocalContext.current

    var permissionsGranted by remember { mutableStateOf(context.hasLocationPermission()) }

    val permissionState = rememberMultiplePermissionsState(
        permissions = PermissionUtil.getPermissionArray()
        .toList()
    ) {
        permissionsGranted = it.containsValue(false).not()
    }

    val mapCover = listOf(
        LatLng(85.0, 90.0),
        LatLng(85.0, 0.001),
        LatLng(85.0, -90.0),
        LatLng(85.0, -179.999),
        LatLng(0.0,-179.999),
        LatLng(-85.0,-179.999),
        LatLng(-85.0,-90.0),
        LatLng(-85.0,0.001),
        LatLng(-85.0,90.0),
        LatLng(-85.0,179.999),
        LatLng(0.0,179.999),
        LatLng(85.0,179.999),
    )



    Box(
        Modifier
            .fillMaxSize()
            .onPlaced {
                if (permissionsGranted.not()) {
                    coroutineScope.launch {
                        permissionState.launchMultiplePermissionRequest()
                    }
                }
            }
            .onGloballyPositioned { coordinates ->
                boxHeightDp = with(localDensity) { coordinates.size.height.toDp() }
            }
    ) {

        if(permissionsGranted) {

            GoogleMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                onMapLoaded = {
                    isMapLoaded = true
                },
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = true
                ),
                properties = MapProperties(
                    isMyLocationEnabled = true
                ),
                locationSource = locationSource,
                contentPadding = PaddingValues(top = (boxHeightDp - 136.dp), end = 6.dp)
            ){
                // polygon that covers the whole world
                Polygon(points = mapCover,
                    holes = viewModel.polygons.value,
                    fillColor = if(isSystemInDarkTheme()) Color(0f, 0f, 0f, 0.8f) else Color(1.0f, 1.0f, 1.0f, 0.8f),
                    strokeWidth = 0.0f
                )
            }
        }

        // only show maps controls if map is loaded
        if(isMapLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ){

                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .drawBehind { }
                        .padding(top = (boxHeightDp - 126.dp), end = 16.dp)
                ) {
                    Box(modifier = Modifier
                        .height(42.dp)
                        .width(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = stringResource(id = R.string.my_location),
                            tint = MaterialTheme.colorScheme.background
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 32.dp, start = 16.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                            }
                        }) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = stringResource(id = R.string.zoom_in),
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                            }

                        }) {
                        Icon(
                            imageVector = Icons.Outlined.Remove,
                            contentDescription = stringResource(id = R.string.zoom_out)
                        )
                    }
                }

            }
        }

        // show spinner if map is loading
        if (isMapLoaded.not()) {

            // check for location permission
            if(permissionsGranted){
                AnimatedVisibility(
                    modifier = Modifier
                        .matchParentSize(),
                    visible = isMapLoaded.not(),
                    enter = EnterTransition.None,
                    exit = fadeOut()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .wrapContentSize()
                    )
                }
            }
            else {
                // show missing permissions message
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = "No permissions granted"
                        )
                    }
                    Row {
                        Button(onClick = {
                            coroutineScope.launch {
                                permissionState.launchMultiplePermissionRequest()
                            }
                        }) {
                            Text(text = "Grant permissions")
                        }
                    }
                }
            }

        }

    }

}
