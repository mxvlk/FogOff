package com.android.fogapp.data.settings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Setting(
    val name: String,
    var value: Double,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)