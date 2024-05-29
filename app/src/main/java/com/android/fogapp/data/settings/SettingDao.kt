package com.android.fogapp.data.settings

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: Setting)

    @Delete
    suspend fun deleteSetting(setting: Setting)

    @Query("SELECT * FROM Setting WHERE id= :id")
    suspend fun getSettingById(id: Long): Setting

    @Query("SELECT * FROM Setting WHERE name= :name")
    suspend fun getSettingByName(name: String): Setting?

    @Query("SELECT * FROM Setting")
    fun getAllSettings(): Flow<List<Setting>>

}