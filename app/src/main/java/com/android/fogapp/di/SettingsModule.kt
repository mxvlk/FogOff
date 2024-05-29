package com.android.fogapp.di

import android.app.Application
import androidx.room.Room
import com.android.fogapp.R
import com.android.fogapp.data.settings.SettingsDatabase
import com.android.fogapp.data.settings.SettingsRepository
import com.android.fogapp.data.settings.SettingsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module for dagger hilt to inject into constructors
 *
 * Provides the settings database
 *
 */
@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

    /**
     * Provides the settings database
     * @param app the app
     *
     * @return the settings database
     */
    @Provides
    @Singleton
    fun provideSettingsDatabase(app: Application): SettingsDatabase {
        return Room.databaseBuilder(
            context = app,
            klass = SettingsDatabase::class.java,
            name = app.getString(R.string.settings_database_name)
        ).build()
    }

    /**
     * Provides the setting repository
     * @param db the settings database
     *
     * @return the settings repository implementation
     */
    @Provides
    @Singleton
    fun provideSettingsRepository(db: SettingsDatabase): SettingsRepository {
        return SettingsRepositoryImpl(db.dao)
    }

}