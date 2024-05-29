package com.android.fogapp.util

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Permission util to request the right permissions for the app according to the android version
 */
object PermissionUtil {

    private val permArrayApi29 = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permArrayApi33 = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private val permArrayApi34 = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.FOREGROUND_SERVICE_LOCATION
    )

    /**
     * Returns the to the build version corresponding permissions
     *
     * @return the correct permission
     */
    fun getPermissionArray(): Array<String> {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
            return permArrayApi34
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return permArrayApi33
        }
        return permArrayApi29
    }

}