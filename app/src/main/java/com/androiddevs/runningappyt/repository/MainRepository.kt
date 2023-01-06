package com.androiddevs.runningappyt.repository

import androidx.lifecycle.LiveData
import com.androiddevs.runningappyt.db.Run
import com.androiddevs.runningappyt.db.RunDao
import javax.inject.Inject


interface MainRepository {

    suspend fun insertRun(run: Run)
    suspend fun deleteRun(run: Run)
    fun getAllRunSortedByDate() : LiveData<List<Run>>
    fun getAllRunSortedByDistance(): LiveData<List<Run>>
    fun getAllRunSortedByAvgSpeed(): LiveData<List<Run>>
    fun getAllRunSortedByTimeMils(): LiveData<List<Run>>
    fun getAllRunSortedByCaloriesBurned(): LiveData<List<Run>>
    fun getTotalAvgSpeed(): LiveData<Long>
    fun getTotalDistance(): LiveData<Long>
    fun getTotalCaloriesBurned(): LiveData<Long>
    fun getTotalTimesInMillis(): LiveData<Long>

}