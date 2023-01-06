package com.androiddevs.runningappyt.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.androiddevs.runningappyt.other.Constant.FIREBASE_REMOTE_CONFIG_SHOW_LINE_GRAPH
import com.androiddevs.runningappyt.repository.MainRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepository: MainRepository,
    val firebase: FirebaseRemoteConfig
) : ViewModel() {



    val totalTimeRun = mainRepository.getTotalTimesInMillis()
    val totalDistance = mainRepository.getTotalDistance()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()
    val totalAverageSpeed = mainRepository.getTotalAvgSpeed()

    val runSortedByDate = mainRepository.getAllRunSortedByDate()


    fun getFirebaseConfigShowLineGraph(): Boolean {
        return firebase.getBoolean(FIREBASE_REMOTE_CONFIG_SHOW_LINE_GRAPH).apply {
            Log.d("TAG", "getFirebaseConfigShowLineGraph: $this")
        }
    }

}