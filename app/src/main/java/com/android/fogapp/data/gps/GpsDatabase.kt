package com.android.fogapp.data.gps

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The gps location room database with the gpslocation entity
 */
@Database(
    entities = [GpsLocation::class],
    version = 4,
    exportSchema = false
)

abstract class GpsDatabase: RoomDatabase() {
    abstract val dao: GpsLocationDao
}