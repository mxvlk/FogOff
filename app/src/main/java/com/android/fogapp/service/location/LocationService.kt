package com.android.fogapp.service.location

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.databinding.ObservableBoolean
import com.android.fogapp.R
import com.android.fogapp.data.gps.GpsLocation
import com.android.fogapp.data.gps.GpsLocationRepository
import com.android.fogapp.util.hasNotificationPermission
import com.android.fogapp.util.insertNewLocationToDB
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


@AndroidEntryPoint
@Singleton
class LocationService: Service() {

    @Inject
    lateinit var repository: GpsLocationRepository

    private val serviceScope = CoroutineScope(
        context = SupervisorJob() + Dispatchers.IO
    )

    private lateinit var geocoder: Geocoder

    private lateinit var locationClient: LocationClient

    /**
     * Needed for a service, not needed for this implementation
     *
     * @param intent the intent
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Run when the service is create, sets a observable boolean if the service is running and instantiates the location client
     */
    override fun onCreate() {

        Log.d("gaming", "on create called")
        super.onCreate()
        IS_ACTIVITY_RUNNING.set(true)
        locationClient = LocationClientImpl(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )

        geocoder = Geocoder(this)
    }

    /**
     * Start and stop service on commands by intent
     *
     * @param intent the received intent
     * @param flags the received flags
     * @param startId the start id
     *
     * @return the service state as int
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("gaming", "on start command")
        when(intent?.action){
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Starts the service and subscribes to location updates from the location client
     * Also stores all locations in latlngList
     */
    private fun start() {

        val intent = Intent(
            this,
            StopGPSNotificationReceiver::class.java
        )
        val penInt = PendingIntent.getBroadcast(
            applicationContext,
            2,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat
            .Builder(this, getString(R.string.notification_channel_id))
            .setContentTitle("FogOff")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.icon_notification)
            .setOngoing(true)
            .addAction(
                NotificationCompat.Action(
                2,
                "Stop",
                penInt
            ))


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(!hasNotificationPermission()) {
                throw LocationClient.LocationException("Missing notification permission")
            }
        }


        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        locationClient
            .getLocationUpdates(gpsInterval)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                Log.d("gaming", "new location")

                serviceScope.launch {
                    var isNewLocation = true

                    val locationsList = repository.getGpsLocations().first()



                    // if list is empty, just add location
                    if(locationsList.isEmpty()){
                        insertNewLocationToDB(location.latitude, location.longitude, geocoder, repository)
                    }

                    // loop trough list to check if new location is duplicate
                    // or less then the buffer value from the old location
                    locationsList.forEach {
                        if(location.latitude == it.lat && location.longitude == it.long){
                            isNewLocation = false
                            return@forEach
                        }
                        else {
                            if(
                                distanceBetweenTwoPointsInM(
                                    it.lat, it.long,
                                    location.latitude, location.longitude
                                ) <= locationBufferInM
                            ){
                                Log.d("gaming", "not new location")
                                isNewLocation = false
                            }
                        }

                    }
                    // add the location if it passed the checks
                    if(isNewLocation) {
                        Log.d("gaming", "inserted location")
                        insertNewLocationToDB(location.latitude, location.longitude, geocoder, repository)
                    }
                }

                // update location state
                LOCATION_STATE.set(LOCATION_STATE.get().not())

                // update the notification to reflect current location
                val updatedNotification = notification.setContentText(
                    "Location: ${location.latitude}, ${location.longitude}"
                )
                notificationManager.notify(
                    1,
                    updatedNotification.build()
                )
            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
    }


    /**
     * Calculates the distance between two points in meter with the haversine formula
     *
     * @param lat1 the first lat
     * @param lon1 the first long
     * @param lat2 the second lat
     * @param lon2 the second long
     *
     * @return the distance between the two points in meter
     */
    private fun distanceBetweenTwoPointsInM(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {

        val diameter = 2 * 6371.0
        val toMeters = 1000

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val lat1Radian = Math.toRadians(lat1)
        val lat2Radian = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(lat1Radian) * cos(lat2Radian)
        val c = asin(sqrt(a))

        return diameter * c * toMeters
    }

    /**
     * Stops the service and saves the new track if conditions are met
     */
    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Runs on destroy, sets running of service to false and cancels the service scope
     */
    override fun onDestroy() {
        super.onDestroy()
        IS_ACTIVITY_RUNNING.set(false)
        serviceScope.cancel()
    }

    /**
     * Companion object defining the start/stop action and a boolean to check if the service is running
     *
     */
    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        var IS_ACTIVITY_RUNNING= ObservableBoolean(false)
        var LOCATION_STATE = ObservableBoolean(false)
        const val gpsInterval = 2000L
        const val locationBufferInM = 25.0
    }
}