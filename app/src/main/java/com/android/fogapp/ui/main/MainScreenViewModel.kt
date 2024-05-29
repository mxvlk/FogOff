package com.android.fogapp.ui.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.databinding.Observable
import androidx.lifecycle.ViewModel
import com.android.fogapp.service.location.LocationService
import com.android.fogapp.util.PermissionUtil
import com.android.fogapp.util.hasLocationPermission
import com.android.fogapp.util.hasNotificationPermission
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel of the main screen
 */
@HiltViewModel
class MainScreenViewModel @Inject constructor(): ViewModel() {

    var tracking = mutableStateOf(false)

    /**
     * Adds a callback to the location service to track if it is running
     */
    init {
        LocationService.IS_ACTIVITY_RUNNING.addOnPropertyChangedCallback(
            object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    tracking.value = LocationService.IS_ACTIVITY_RUNNING.get()
                }
            })
    }

    /**
     * Called when a event from the main screen is received
     *
     * @param event the event from the main screen
     * @param context the context of the received event
     */
    fun onEvent(event: MainScreenEvent, context: Context) {
        when(event) {
            is MainScreenEvent.OnStartTrackingClick -> {
                trackingActionUpdate(
                    tracking = tracking.value,
                    context = context
                )
            }
        }
    }

    /**
     * Starts or stops the tracking service depending of if it is running
     *
     * @param tracking is the service running
     * @param context the context of the service
     */
    private fun trackingActionUpdate(
        tracking: Boolean,
        context: Context
    ) {
        var notification = true

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notification = context.hasNotificationPermission()
        }

        if(context.hasLocationPermission() && notification) {
            val action = if(tracking) LocationService.ACTION_STOP else LocationService.ACTION_START

            Intent(context, LocationService::class.java).apply {
                this.action = action
                context.startService(this)
            }
        }
        else {
            if(context.hasLocationPermission() && !notification){
                Toast.makeText(context, "Please grant notification permission", Toast.LENGTH_LONG).show()
            }
            else {
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                    Toast.makeText(context, "Please grant permissions", Toast.LENGTH_LONG).show()
                }
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if(context.hasNotificationPermission() && !context.hasLocationPermission()){
                    Toast.makeText(context, "Please grant location permission", Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(context, "Please grant permissions", Toast.LENGTH_LONG).show()
                }
            }

        }


    }

}