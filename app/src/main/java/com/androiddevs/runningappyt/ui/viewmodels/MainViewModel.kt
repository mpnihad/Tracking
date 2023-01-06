package com.androiddevs.runningappyt.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.runningappyt.db.Run
import com.androiddevs.runningappyt.repository.DefaultMainRepository
import com.androiddevs.runningappyt.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel() {

    var isNotificationEnabled : Boolean = false


    var finalList = MediatorLiveData<List<Run>>()

    val runSortedByDate =mainRepository.getAllRunSortedByDate()
    val runSortedByDistance =mainRepository.getAllRunSortedByDistance()
    val runSortedByTime =mainRepository.getAllRunSortedByTimeMils()
    val runSortedBySpeed =mainRepository.getAllRunSortedByAvgSpeed()
    val runSortedByCaloriesBurned =mainRepository.getAllRunSortedByCaloriesBurned()

    init{
        finalList.addSource(runSortedByDate){
            finalList.value = it
        }

        finalList.addSource(runSortedByDistance){
            finalList.value = it
        }

        finalList.addSource(runSortedByTime){
            finalList.value = it
        }

        finalList.addSource(runSortedBySpeed){
            finalList.value = it
        }

        finalList.addSource(runSortedByCaloriesBurned){
            finalList.value = it
        }
    }
    var sort =SORT.DATE

    fun sortBy(sortType: SORT){
        when (sortType){
            SORT.DATE -> {
                runSortedByDate.value?.let {
                    finalList.value = it
                }
            }
            SORT.SPEED -> {runSortedBySpeed.value?.let {
                finalList.value = it
            }
            }
            SORT.TIME -> {
                runSortedByTime.value?.let {
                    finalList.value = it
                }
            }
            SORT.DISTANCE -> {
                runSortedByDistance.value?.let {
                    finalList.value = it
                }
            }
            SORT.CALORIES -> {
                runSortedByCaloriesBurned.value?.let {
                    finalList.value = it
                }
            }
        }
        sort = sortType
    }
    fun insertRun(run: Run){
        viewModelScope.launch {
            mainRepository.insertRun(run)
        }
    }
}

enum class SORT{
    DATE,SPEED, TIME, DISTANCE,CALORIES
}