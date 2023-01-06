package com.androiddevs.runningappyt.other

import android.Manifest
import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.app.ActivityCompat
import com.androiddevs.runningappyt.service.Polyline
import com.androiddevs.runningappyt.service.Polylines
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit
import kotlin.math.min


object TrackingUtility {

    fun hasLocationPermission(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(

                context,
                ACCESS_FINE_LOCATION,


            )
        } else {

            (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&

                    EasyPermissions.hasPermissions(

                        context,
                        ACCESS_FINE_LOCATION,


                    ))
        }

    var locationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(
            ACCESS_FINE_LOCATION,
            ACCESS_BACKGROUND_LOCATION
        )
    } else {
        arrayOf(ACCESS_FINE_LOCATION)
    }



    fun getFormattedStopwatchTime(ms:Long,includeMillis:Boolean = false) : String {

        var tempMilliSeconds = ms

        var hours  = TimeUnit.MILLISECONDS.toHours(tempMilliSeconds)
        tempMilliSeconds -= TimeUnit.HOURS.toMillis(hours)

        var minutes   = TimeUnit.MILLISECONDS.toMinutes(tempMilliSeconds)
        tempMilliSeconds -= TimeUnit.MINUTES.toMillis(minutes)


        var seconds   = TimeUnit.MILLISECONDS.toSeconds(tempMilliSeconds)
        tempMilliSeconds -= TimeUnit.SECONDS.toMillis(seconds)

        if(!includeMillis){
            return "${if(hours<10)"0" else ""}$hours h:"+
             "${if(minutes < 10)"0" else ""}$minutes m:" +
             "${if(seconds<10)"0" else ""}$seconds s"
        }

        tempMilliSeconds -= TimeUnit.MILLISECONDS.toMillis(seconds)
        tempMilliSeconds /= 10


            return "${if(hours<10)"0" else ""}$hours h:"+
                    "${if(minutes < 10)"0" else ""}$minutes m:" +
                    "${if(seconds<10)"0" else ""}$seconds s:"+
                    "${if(tempMilliSeconds<10)"0" else ""}$tempMilliSeconds"




    }

    fun calculatePolylineLength (polylines: Polyline) :Float {
        var distance = 0f
        for (i in 0..polylines.size - 2) {

            val pos1 = polylines[i]
            val pos2 = polylines[i+1]

            val result= FloatArray(1)
            Location.distanceBetween(
                pos1.latitude,
                pos1.longitude,
                pos2.latitude,
                pos2.longitude,
                result
            )

            distance +=result[0]


        }

        return  distance

    }
}