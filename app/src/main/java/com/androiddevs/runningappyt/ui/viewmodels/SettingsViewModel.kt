package com.androiddevs.runningappyt.ui.viewmodels

import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.androiddevs.runningappyt.other.Constant
import com.androiddevs.runningappyt.other.Constant.FIREBASE_ANALYTICS_NAME
import com.androiddevs.runningappyt.other.Constant.FIREBASE_ANALYTICS_WEIGHT
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    val firebaseAnalytics: FirebaseAnalytics,
    var sharedPref: SharedPreferences

) : ViewModel() {


    fun writePersonalDataToSharePref(name:String,weight:String): Boolean {

        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPref.edit().putString(Constant.KEY_NAME, name)
            .putFloat(Constant.KEY_WEIGHT, weight.toFloat())
            .putBoolean(Constant.KEY_FIRST_TIME_TOGGLED, false)
            .apply()


        val bundle = Bundle()
        bundle.apply {
            putString(FIREBASE_ANALYTICS_NAME,name)
            putString(FIREBASE_ANALYTICS_WEIGHT,weight)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN,bundle)


        return true
    }


}