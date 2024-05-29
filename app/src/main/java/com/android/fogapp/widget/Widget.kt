package com.android.fogapp.widget

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import androidx.databinding.Observable
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.android.fogapp.service.location.LocationService
import com.android.fogapp.util.hasLocationPermission

/**
 * Widget for the app to start and stop tracking
 */
class Widget: GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        var tracking = mutableStateOf(false)

        provideContent {
            GlanceTheme {
                WidgetLayout(tracking)
            }
        }
    }

    @Composable
    private fun WidgetLayout(
        tracking: MutableState<Boolean>
    ) {
        LocationService.IS_ACTIVITY_RUNNING.addOnPropertyChangedCallback(
            object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    tracking.value = LocationService.IS_ACTIVITY_RUNNING.get()
                }
            })

        var colors = GlanceTheme.colors

        Column(
            modifier = GlanceModifier
                .background(colors.background)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Row {
                Text(
                    style = TextStyle(color = colors.onBackground),
                    modifier = GlanceModifier.padding(8.dp),
                    text = "FogOff" // string resources do not work with glance
                )
            }
            
            Row {

                if(LocalContext.current.hasLocationPermission()){

                    // normal composable functions don't work with glance
                    if(tracking.value) {
                        Button(
                            text = "Stop tracking",
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = ColorProvider(MaterialTheme.colorScheme.errorContainer),
                                contentColor = ColorProvider(MaterialTheme.colorScheme.onErrorContainer)
                            ),
                            onClick = {
                                actionSendBroadcast<WidgetReceiver>()
                            }
                        )
                    }
                    else {
                        Button(
                            text = "Start tracking",
                            onClick = {
                                actionSendBroadcast<WidgetReceiver>()
                            }
                        )
                    }
                }
                else {
                    Text(
                        style = TextStyle(color = colors.onBackground),
                        modifier = GlanceModifier.padding(8.dp),
                        text = "No location permission"
                    )
                }


            }
        }
    }

}