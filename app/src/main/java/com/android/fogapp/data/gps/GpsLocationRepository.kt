package com.android.fogapp.data.gps

import kotlinx.coroutines.flow.Flow

interface GpsLocationRepository {

    suspend fun insertLocation(gpsLocation: GpsLocation)

    suspend fun deleteLocation(gpsLocation: GpsLocation)

    fun getLocationById(id: Long): Flow<GpsLocation>

    fun getGpsLocations(): Flow<List<GpsLocation>>
}