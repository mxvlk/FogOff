package com.android.fogapp.ui.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.fogapp.data.gps.GpsLocation
import com.android.fogapp.data.gps.GpsLocationRepository
import com.android.fogapp.data.settings.Setting
import com.android.fogapp.data.settings.SettingsRepository
import com.android.fogapp.util.convertPathToLineString
import com.android.fogapp.util.convertPolygonToLatLngList
import com.android.fogapp.util.createPerimeter
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class StatsViewModel @Inject constructor(private val repository: GpsLocationRepository,
                                         private val repositorySettings: SettingsRepository) : ViewModel() {

    private var bufferDistance by mutableDoubleStateOf(0.005)
    private var waterSurfaceBool by mutableDoubleStateOf(1.0)

    private val worldSurfaceArea = 510100000.0 // in km²
    private val worldSurfaceAreaNoWat = 147929000.0 // in km²

    val settings = repositorySettings.getAllSettings()

    private var discoveredPercentageRaw by mutableDoubleStateOf(0.0)
    var discoveredPercentageRounded by mutableDoubleStateOf(0.0)
    var discoveredPercentagePiechart by mutableDoubleStateOf(0.0)
    var discoveredSurfaceAreaRounded by mutableDoubleStateOf(0.0)
    var totalDistanceTravelledRounded by mutableDoubleStateOf(0.0)
    var cityMap by mutableStateOf<Map<String, Double>>(mutableMapOf())
    var countryMap by mutableStateOf<Map<String, Double>>(mutableMapOf())
    private var totalCityCount by mutableIntStateOf(0)
    private var totalCountryCount by mutableIntStateOf(0)

    init {
        viewModelScope.launch {
            bufferDistance = getBufferDistance().value
            waterSurfaceBool = getWaterSurfaceBool().value

            val locList = getLocationList()
            val discoveredSurfaceArea = getDiscoveredSurfaceArea(locList)

            discoveredSurfaceAreaRounded = discoveredSurfaceArea.toBigDecimal().setScale(2, RoundingMode.UP).toDouble()

            discoveredPercentageRaw = if (waterSurfaceBool == 1.0){
                (discoveredSurfaceArea / worldSurfaceArea) * 100
            } else {
                (discoveredSurfaceArea / worldSurfaceAreaNoWat) * 100
            }

            discoveredPercentageRounded = discoveredPercentageRaw.toBigDecimal().setScale(10, RoundingMode.UP).toDouble()
            discoveredPercentagePiechart = discoveredPercentageRaw.toBigDecimal().setScale(0, RoundingMode.UP).toDouble()

            totalDistanceTravelledRounded = calculateTotalDistance(locList).toBigDecimal().setScale(2, RoundingMode.UP).toDouble()

            val cityList = getLocationDetailsWithCount(locList).first
            val countryList = getLocationDetailsWithCount(locList).second

            totalCityCount = getTotalCount(cityList)
            totalCountryCount = getTotalCount(countryList)

            cityMap = convertToMap(cityList, totalCityCount)
            countryMap = convertToMap(countryList, totalCountryCount)
        }
    }

    private suspend fun getLocationList() = repository.getGpsLocations().first()

    private suspend fun getBufferDistance() = repositorySettings.getSettingByName("bufferDistance") ?: Setting("bufferDistance", 0.005)

    private suspend fun getWaterSurfaceBool() = repositorySettings.getSettingByName("waterSurfaceBool") ?: Setting("waterSurfaceBool", 1.0)

    private fun getDiscoveredSurfaceArea(locationsList: List<GpsLocation>): Double {
        if(locationsList.size > 1){
            val lineString = convertPathToLineString(locationsList)

            if (lineString != null) {
                val polygon = createPerimeter(lineString, bufferDistance)

                if(polygon != null){
                    val list = convertPolygonToLatLngList(polygon)
                    return SphericalUtil.computeArea(list) * 0.000001 // convert m^2 to km^2
                }
            }
        }

        return 0.0
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Earth radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(lat1Rad) * cos(lat2Rad)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    private fun calculateTotalDistance(locations: List<GpsLocation>): Double {
        var totalDistance = 0.0
        for (i in 0 until locations.size - 1) {
            val location1 = locations[i]
            val location2 = locations[i + 1]
            totalDistance += calculateDistance(
                location1.lat, location1.long,
                location2.lat, location2.long
            )
        }
        return totalDistance
    }

    private fun getLocationDetailsWithCount(locations: List<GpsLocation>): Pair<List<Pair<String, Int>>, List<Pair<String, Int>>> {
        val cityDetailsMap = mutableMapOf<String, Int>()
        val countryDetailsMap = mutableMapOf<String, Int>()

        for (location in locations) {
            val city = location.city
            val country = location.country

            if (city != null) {
                val cityCount = cityDetailsMap[city] ?: 0
                cityDetailsMap[city] = cityCount + 1
            }

            if (country != null) {
                val countryCount = countryDetailsMap[country] ?: 0
                countryDetailsMap[country] = countryCount + 1
            }
        }

        val cityList = cityDetailsMap.filter { true }.map { it.key to it.value }.toList()
        val countryList = countryDetailsMap.filter { true }.map { it.key to it.value }.toList()

        return Pair(cityList, countryList)
    }

    private fun getTotalCount(pairList: List<Pair<String, Int>>): Int {
        var totalCount = 0
        for ((_, count) in pairList) {
            totalCount += count
        }
        return totalCount
    }

    private fun convertToMap(locList: List<Pair<String, Int>>, totalLocCount: Int): Map<String, Double> {
        if (locList.isEmpty()) {
            return emptyMap()
        }

        val sortedLocList = locList.sortedByDescending { it.second }

        val result = mutableMapOf<String, Double>()
        var restPercentage = 0.0
        val top4 = sortedLocList.take(4)

        for ((country, count) in top4) {
            val percentage = (count.toDouble() / totalLocCount) * 100
            result[country] = percentage
            restPercentage += percentage
        }

        val restCount = totalLocCount - top4.sumOf { it.second }
        if (restCount > 0) {
            result["Other"] = 100 - restPercentage
        }

        return result
    }
}
