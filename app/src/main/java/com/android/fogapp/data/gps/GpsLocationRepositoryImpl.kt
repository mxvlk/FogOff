package com.android.fogapp.data.gps

import kotlinx.coroutines.flow.Flow

class GpsLocationRepositoryImpl(
    private val dao: GpsLocationDao
): GpsLocationRepository {

    override suspend fun insertLocation(gpsLocation: GpsLocation) {
        dao.insertLocation(gpsLocation)
    }

    override suspend fun deleteLocation(gpsLocation: GpsLocation) {
        dao.deleteLocation(gpsLocation)
    }

    override fun getLocationById(id: Long): Flow<GpsLocation> {
        return dao.getLocationById(id)
    }

    override fun getGpsLocations(): Flow<List<GpsLocation>> {
        return dao.getGpsLocations()
    }

}