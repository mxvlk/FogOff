package com.android.fogapp.data.settings

import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val dao: SettingDao
): SettingsRepository {
    override suspend fun insertSetting(setting: Setting) {
        dao.insertSetting(setting)
    }

    override suspend fun deleteSetting(setting: Setting) {
        dao.deleteSetting(setting)
    }

    override suspend fun getSettingById(id: Long): Setting {
        return  dao.getSettingById(id)
    }

    override suspend fun getSettingByName(name: String): Setting? {
        return dao.getSettingByName(name)
    }

    override fun getAllSettings(): Flow<List<Setting>> {
        return dao.getAllSettings()
    }
}