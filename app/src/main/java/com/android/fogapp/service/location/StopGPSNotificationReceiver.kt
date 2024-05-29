package com.android.fogapp.service.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Implements a broadcast receiver to receive a intent to stop the location tracking service from the notification
 */
class StopGPSNotificationReceiver: BroadcastReceiver() {

    // stop the service on receive
    override fun onReceive(context: Context?, intent: Intent?) {

        Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            context?.startService(this)
        }

    }

}