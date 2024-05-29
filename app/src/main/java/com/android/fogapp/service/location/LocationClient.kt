package com.android.fogapp.service.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * Location client interface which defines a method to get location updates and a exception
 */
interface LocationClient {

    fun getLocationUpdates(interval: Long): Flow<Location>

    class LocationException(message: String): Exception(message)

}