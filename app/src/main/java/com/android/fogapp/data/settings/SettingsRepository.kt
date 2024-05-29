package com.android.fogapp.data.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    suspend fun insertSetting(setting: Setting)

    suspend fun deleteSetting(setting: Setting)

    suspend fun getSettingById(id: Long): Setting

    suspend fun getSettingByName(name: String): Setting?

    fun getAllSettings(): Flow<List<Setting>>

}