package com.android.fogapp.ui.map

import android.util.Log
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.databinding.Observable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.fogapp.data.gps.GpsLocationRepository
import com.android.fogapp.data.settings.Setting
import com.android.fogapp.data.settings.SettingsRepository
import com.android.fogapp.service.location.LocationService.Companion.LOCATION_STATE
import com.android.fogapp.util.convertPathToLineString
import com.android.fogapp.util.convertPolygonToLatLngList
import com.android.fogapp.util.createPerimeter
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel of the map screen
 */
@HiltViewModel
class MapScreenViewModel @Inject constructor(private val repository: GpsLocationRepository,
                                             private val repositorySettings: SettingsRepository) :
    ViewModel() {

    var polygons = mutableStateOf<List<List<LatLng>>>(emptyList())
    private var bufferDistance by mutableDoubleStateOf(0.005)

    init {
        viewModelScope.launch {
            bufferDistance = getBufferDistance().value
            updatePolygons()
        }

        // observe location state to update polygons on change
        LOCATION_STATE.addOnPropertyChangedCallback(
            object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    Log.d("gaming", "property changed")
                    viewModelScope.launch {
                        bufferDistance = getBufferDistance().value

                        updatePolygons()
                    }
                }
            })
    }

    private suspend fun getBufferDistance() = repositorySettings.getSettingByName("bufferDistance") ?: Setting("bufferDistance", 0.005)

    /**
     * Function that updates the polygons
     */
    private suspend fun updatePolygons(){
        Log.d("gaming", "updatePolygons called")

        val locationsList = repository.getGpsLocations().first()

        // we can only construct a polygon if more then one point is in the list
        if(locationsList.size > 1){
            val lineString = convertPathToLineString(locationsList)

            // check that line string is not null
            if (lineString != null) {
                val polygon = createPerimeter(lineString, bufferDistance)
                // check that polygon string is not null
                if(polygon != null){
                    val list = convertPolygonToLatLngList(polygon)
                    polygons.value = listOf(list)
                }
            }
        }

    }

}