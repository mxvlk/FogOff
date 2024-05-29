package com.android.fogapp.data.gps

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GpsLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(gpsLocation: GpsLocation)

    @Delete
    suspend fun deleteLocation(gpsLocation: GpsLocation)

    @Query("SELECT * FROM GpsLocation WHERE id= :id")
    fun getLocationById(id: Long): Flow<GpsLocation>

    @Query("SELECT * FROM GpsLocation")
    fun getGpsLocations(): Flow<List<GpsLocation>>

}