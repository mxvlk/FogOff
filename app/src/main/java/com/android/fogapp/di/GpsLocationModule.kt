package com.android.fogapp.di

import android.app.Application
import androidx.room.Room
import com.android.fogapp.R
import com.android.fogapp.data.gps.GpsDatabase
import com.android.fogapp.data.gps.GpsLocationRepository
import com.android.fogapp.data.gps.GpsLocationRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module for dagger hilt to inject into constructors
 *
 * Provides the location database
 *
 */
@Module
@InstallIn(SingletonComponent::class)
object GpsLocationModule {

    /**
     * Provides the location database
     * @param app the app
     *
     * @return the location database
     */
    @Provides
    @Singleton
    fun provideGpsDatabase(app: Application): GpsDatabase {
        return Room.databaseBuilder(
            context = app,
            klass = GpsDatabase::class.java,
            name = app.getString(R.string.gps_database_name)
        ).build()
    }

    /**
     * Provides the location repository
     * @param db the location database
     *
     * @return the location repository implementation
     */
    @Provides
    @Singleton
    fun provideGpsLocationRepository(db: GpsDatabase): GpsLocationRepository {
        return GpsLocationRepositoryImpl(db.dao)
    }

}