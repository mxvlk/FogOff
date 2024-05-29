package com.android.fogapp.ui.settings

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.fogapp.data.gps.GpsLocation
import com.android.fogapp.data.gps.GpsLocationRepository
import com.android.fogapp.util.getBufferSizeFromBufferDistance
import com.android.fogapp.util.insertNewLocationToDB
import io.ticofab.androidgpxparser.parser.GPXParser
import io.ticofab.androidgpxparser.parser.domain.Gpx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.math.RoundingMode

/**
 * Composable that shows the settings
 *
 * @param viewModel the view model of the setting screen
 */

@Composable
fun SettingsScreen (
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val sliderVal = remember { mutableFloatStateOf(0.005f) }

    sliderVal.floatValue = viewModel.bufferDistanceSetting.value.toFloat()

    val switchVal = remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val gpsRep = viewModel.gps
    val launcher = rememberLauncherForActivityResult(contract =ActivityResultContracts.GetContent(), onResult = {importLocations(gpsRep, coroutineScope, context, it)})

    switchVal.value = viewModel.waterSurfaceSetting.value == 1.0

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {


        Column {

            ListItem(headlineContent = {
                Text(
                    text = "Settings",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            )

            Divider()

            ListItem(headlineContent = {
                Text(
                    text = "Buffer Distance"
                )
            },
                trailingContent = {
                    Text("~" +
                        getBufferSizeFromBufferDistance(
                            sliderVal.floatValue.toBigDecimal().setScale(3, RoundingMode.HALF_EVEN).toDouble()
                        ).toBigDecimal().setScale(0, RoundingMode.HALF_EVEN).toString() + " m"
                    )
                }
            )

            Slider(
                modifier = Modifier
                    .padding(
                        start = 40.dp,
                        end = 40.dp
                    ),
                value = sliderVal.floatValue,
                onValueChange = {
                    sliderVal.floatValue = it
                    viewModel.launchUpdateBufferDistance(sliderVal.floatValue.toBigDecimal().setScale(3, RoundingMode.HALF_EVEN).toDouble())
                },
                steps = 7,
                valueRange = 0.001f..0.009f
            )

            Divider()

            ListItem(headlineContent = {
                Text(
                    text = "Water Surface Toggle"
                )
            },
                trailingContent = {
                    Switch(checked = switchVal.value, onCheckedChange = {
                        switchVal.value = it
                        if(it) viewModel.launchUpdateWaterSurfaceBool(1.0) else viewModel.launchUpdateWaterSurfaceBool(0.0)
                    })
                }
            )

            Divider()

            Row(modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(8.dp)) {
                Button(onClick = {launcher.launch("*/*")}) {
                    Text(text = "Import GPX")
                }
            }

        }

    }

}


fun importLocations(repository: GpsLocationRepository, coroutineScope: CoroutineScope, context: Context, uri: Uri?) {
    Log.d("GpxProvider", "importLocations")
    val parser = GPXParser()
    val geocoder = Geocoder(context)
    try {
        val input: InputStream? = context.contentResolver.openInputStream(uri!!)

        val parsedGpx: Gpx? = parser.parse(input)

        parsedGpx?.let {

            it.tracks.forEach { track ->
                track.trackSegments.forEach { segment ->
                    segment.trackPoints.forEach { waypoint ->
                        coroutineScope.launch {
                            insertNewLocationToDB(waypoint.latitude, waypoint.longitude, geocoder, repository)
                        }
                    }
                }
            }
        }.also {
            Toast.makeText(context, "Imported GPX successfully", Toast.LENGTH_LONG).show() }
    } catch (e: Exception) {

        Toast.makeText(context, "Importing GPX failed", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}

