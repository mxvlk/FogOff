package com.android.fogapp.data.settings

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The setting room database with the setting entity
 */
@Database(
    entities = [Setting::class],
    version = 1,
    exportSchema = false
)

abstract class SettingsDatabase: RoomDatabase() {
    abstract val dao: SettingDao
}