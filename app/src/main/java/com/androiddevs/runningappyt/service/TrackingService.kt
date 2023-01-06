package com.androiddevs.runningappyt.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.other.Constant
import com.androiddevs.runningappyt.other.Constant.ACTION_PAUSE_SERVICE
import com.androiddevs.runningappyt.other.Constant.ACTION_START_OR_RESUME_SERVICE
import com.androiddevs.runningappyt.other.Constant.FASTEST_LOCATION_INTERVAL
import com.androiddevs.runningappyt.other.Constant.LOCATION_UPDATE_INTERVAL
import com.androiddevs.runningappyt.other.Constant.NOTIFICATION_ID
import com.androiddevs.runningappyt.other.Constant.TIMER_UPDATE_INTERVAL
import com.androiddevs.runningappyt.other.TrackingUtility
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>


@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstOne = true


    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var currentNotificationBuilder: NotificationCompat.Builder

    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object {

        val isTracking = MutableLiveData<Boolean>()


        val timRunInMillis = MutableLiveData<Long>()


        val pathPoints = MutableLiveData<Polylines>()
    }


    private var isTimerEnabled = false
    private var lapTime = 0L

    private var timeRun = 0L

    private var timeStarted = 0L
    private var lastSecondTimerStamp = 0L

    private fun startTimer() {

        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {

            while (isTracking.value!!) {
                //time Diffrence between started time and current time
                lapTime = System.currentTimeMillis() - timeStarted

                // new lap time
                timRunInMillis.postValue(timeRun + lapTime)

                if (timRunInMillis.value!! >= lastSecondTimerStamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }


    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timRunInMillis.postValue(0L)
        timeRunInSeconds.postValue(0L)
    }

    private fun killService(){
        serviceKilled = true
        isFirstOne= true
        pauseService()
        postInitialValues()
        stopSelf()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }


    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder
        postInitialValues()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        isTracking.observe(this) {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        }

    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermission(this)) {

                val request = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL
                ).setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(FASTEST_LOCATION_INTERVAL).build()


                fusedLocationProviderClient.requestLocationUpdates(
                    request, locationCallbacks, Looper.getMainLooper()
                )

            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallbacks)
        }


    }

    private val locationCallbacks = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Log.d("Location", "Location ${location.latitude}")

                    }


                }
            }
        }
    }



    fun updateNotificationTrackingState(isTracking: Boolean) {

        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT or FLAG_MUTABLE)


        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT or FLAG_MUTABLE)

        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, java.util.ArrayList<NotificationCompat.Action>())

           if(!serviceKilled){
               currentNotificationBuilder = baseNotificationBuilder
                   .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
               notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
           }
        }


    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {

                last().add(pos)
                pathPoints.postValue(this)
            }
        }

    }


    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        intent?.let {
            when (it.action) {

                Constant.ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstOne) {

                        startForegroundService()
                        isTracking.postValue(true)
                        isFirstOne = false
                    } else {

                        startTimer()
                    }


                }
                Constant.ACTION_PAUSE_SERVICE -> {
                    pauseService()


                }
                Constant.ACTION_STOP_SERVICE -> {
                    killService()
                }

            }
        }

        return super.onStartCommand(intent, flags, startId)
    }


    private fun pauseService() {
        isTracking.value = false
        isTimerEnabled = false
    }

    private fun startForegroundService() {
        startTimer()
        isTracking.value = true
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }


        val notificationBuilder = baseNotificationBuilder


        startForeground(Constant.NOTIFICATION_ID, notificationBuilder.build())
        timeRunInSeconds.observe(this) {

            if(!serviceKilled) {
                val notification = currentNotificationBuilder.setContentText(
                    TrackingUtility.getFormattedStopwatchTime(it * 1000L)
                )
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        }


    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            Constant.NOTIFICATION_CHANNEL_ID, Constant.NOTIFICATION_CHANNEL_NAME, IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)
    }


}