package com.android.fogapp.util

import android.location.Geocoder
import com.android.fogapp.data.gps.GpsLocation
import com.android.fogapp.data.gps.GpsLocationRepository
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Polygon
import java.io.IOException
import kotlin.math.PI
import kotlin.math.sqrt

/**
 * Function to convert the path to a line string (needed for jts)
 *
 * @param path a list of GpsLocations
 *
 * @return LineString if it is constructable, else null
 */
fun convertPathToLineString(path: List<GpsLocation>): LineString? {
    val coordinates = arrayOfNulls<Coordinate>(path.size)
    for (i in path.indices) {
        coordinates[i] = Coordinate(path[i].long, path[i].lat)
    }
    val factory = GeometryFactory()
    return factory.createLineString(coordinates)
}

/**
 * Function to create a perimeter around the path
 *
 * @param path a LineString
 * @param bufferDistance the distance of the perimeter
 *
 * @return Polygon if it is constructable, else null
 */
fun createPerimeter(path: LineString, bufferDistance: Double): Polygon? {
    val buffer = path.buffer(bufferDistance, 10)

    if (buffer is Polygon) {
        return buffer
    }
    return null
}

fun getBufferSizeFromBufferDistance(bufferDistance: Double): Double {
    val path =  convertPathToLineString(listOf(GpsLocation(48.5000967, 10.1181567, null, null), GpsLocation(48.5000967, 10.1181567, null, null)))
    val buffer = path?.buffer(bufferDistance, 10)

    if (buffer is Polygon) {
        val area = SphericalUtil.computeArea(convertPolygonToLatLngList(buffer)) // in m^2
        return sqrt(area / PI) // return radius in m
    }
    return 0.0
}

/**
 * Function to convert the polygon to a list of LatLng (needed for maps compose)
 *
 * @param polygon a Polygon
 *
 * @return List of LatLng
 */
fun convertPolygonToLatLngList(polygon: Polygon): List<LatLng> {
    val latLngList = ArrayList<LatLng>()
    val coordinates = polygon.coordinates
    for (coord in coordinates) {
        latLngList.add(LatLng(coord.y, coord.x)) // Convert y (latitude) first
    }
    return latLngList
}

suspend fun insertNewLocationToDB(latitude: Double, longitude: Double, geocoder: Geocoder, repository: GpsLocationRepository) {
    var city: String? = null
    var country: String? = null

    try {
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses != null) {
            if (addresses.isNotEmpty()) {
                city = addresses[0].locality
                country = addresses[0].countryName
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    finally {
        repository.insertLocation(
            GpsLocation(latitude, longitude, city, country)
        )
    }
}