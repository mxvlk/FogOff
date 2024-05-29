package com.android.fogapp.widget

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.android.fogapp.service.location.LocationService
import com.android.fogapp.util.hasLocationPermission

/**
 * WidgetReceiver to receive widget events
 */
class WidgetReceiver: GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = Widget()

    override fun onReceive(context: Context, intent: Intent) {

        // check for location permission
        if(context.hasLocationPermission()){

            // click on the widget triggers the "ACTION_TRIGGER_LAMBDA" action
            if(intent.action == "ACTION_TRIGGER_LAMBDA"){

                val serviceAction = if(LocationService.IS_ACTIVITY_RUNNING.get()) LocationService.ACTION_STOP else LocationService.ACTION_START

                Intent(context, LocationService::class.java).apply {
                    action = serviceAction
                    context.startService(this)
                }
            }
        }



        super.onReceive(context, intent)
    }
}