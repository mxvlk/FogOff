package com.android.fogapp.ui.settings

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.fogapp.data.gps.GpsLocationRepository
import com.android.fogapp.data.settings.Setting
import com.android.fogapp.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel of the settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRep: SettingsRepository,
    private val gpsRep: GpsLocationRepository
): ViewModel() {

    var bufferDistanceSetting by mutableStateOf(Setting("bufferDistance", 0.005))
    var waterSurfaceSetting by mutableStateOf(Setting("waterSurfaceBool", 1.0))

    val gps = gpsRep

    init {
        viewModelScope.launch {
            bufferDistanceSetting = getBufferDistanceSetting()
            waterSurfaceSetting = getWaterSurfaceSetting()
        }
    }

    private suspend fun changeBufferDistance(newBufferDistance: Double) {
        bufferDistanceSetting.value = newBufferDistance
        return settingsRep.insertSetting(bufferDistanceSetting)
    }

    private suspend fun changeWaterSurfaceBool(newWaterSurfaceBool: Double) {
        waterSurfaceSetting.value = newWaterSurfaceBool
        return settingsRep.insertSetting(waterSurfaceSetting)
    }

    suspend fun getBufferDistanceSetting() = settingsRep.getSettingByName("bufferDistance") ?: Setting("bufferDistance", 0.005)

    suspend fun getWaterSurfaceSetting() = settingsRep.getSettingByName("waterSurfaceBool") ?: Setting("waterSurfaceBool", 1.0)

    fun launchUpdateBufferDistance(newValue: Double) {
        viewModelScope.launch {
            changeBufferDistance(newValue)
        }
    }

    fun launchUpdateWaterSurfaceBool(newValue: Double) {
        viewModelScope.launch {
            changeWaterSurfaceBool(newValue)
        }
    }
}