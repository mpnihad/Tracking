package com.androiddevs.runningappyt.other

import androidx.annotation.ColorInt

object Constant {

    const val RUNNING_DATABASE_NAME = "Tracking"

    const val REQUEST_CODE_LOCATION_PERMISSION = 1

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val  NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val  NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1


    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"
    const val LOCATION_UPDATE_INTERVAL =5000L
    const val FASTEST_LOCATION_INTERVAL =2000L


    @ColorInt const val  POLYLINE_COLOR : Int = 0xFF00A1C9.toInt()
    const val  POLYLINE_WIDTH = 22f
    const val  MAP_ZOOM = 15f

    const val TIMER_UPDATE_INTERVAL = 500L


    const val SHARED_PREF_NAME = "sharedPref"
    const val KEY_FIRST_TIME_TOGGLED ="KEY_FIRST_TIME_TOGGLED"
    const val KEY_NAME ="KEY_NAME"
    const val KEY_WEIGHT ="KEY_WEIGHT"

    const val CANCEL_TRACKING_DIALOG ="cancelDialog"
    const val ADD_WALLET_DIALOG ="walletDialog"


    //firebase
    const val  FIREBASE_ANALYTICS_NAME ="name"
    const val  FIREBASE_ANALYTICS_WEIGHT ="weight"
    const val FIREBASE_ANALYTICS_CANCEL_RUN = "canceled_run"


    const val FIREBASE_ANALYTICS_SPEED = "run_speed"
    const val FIREBASE_ANALYTICS_CALORIES = "run_calories"
    const val FIREBASE_ANALYTICS_DISTANCE = "run_distance"
    const val FIREBASE_ANALYTICS_DATE = "run_date"

    const val FIREBASE_REMOTE_CONFIG_SHOW_LINE_GRAPH = "show_line_graph"



}