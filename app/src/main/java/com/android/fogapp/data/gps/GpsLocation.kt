package com.android.fogapp.data.gps

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GpsLocation(
    val lat: Double,
    val long: Double,
    val city: String?,
    val country: String?,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)