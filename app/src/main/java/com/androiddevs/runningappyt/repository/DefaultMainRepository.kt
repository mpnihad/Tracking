package com.androiddevs.runningappyt.repository

import com.androiddevs.runningappyt.db.Run
import com.androiddevs.runningappyt.db.RunDao
import javax.inject.Inject

class DefaultMainRepository @Inject constructor(
    private val runDao: RunDao
) : MainRepository {

    override suspend fun insertRun(run: Run) = runDao.insetRun(run)
    override suspend fun deleteRun(run: Run) = runDao.deleteRun(run)
    override fun getAllRunSortedByDate() = runDao.getAllRunsSortedByDate()
    override fun getAllRunSortedByDistance() = runDao.getAllRunsSortedByDistance()
    override fun getAllRunSortedByAvgSpeed() = runDao.getAllRunsSortedByAvgSpeed()
    override fun getAllRunSortedByTimeMils() = runDao.getAllRunsSortedByTimeMilliSeconds()
    override fun getAllRunSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()
    override fun getTotalAvgSpeed() = runDao.getTotalAvgSpeedInKMH()
    override fun getTotalDistance() = runDao.getTotalDistance()
    override fun getTotalCaloriesBurned()= runDao.getTotalCaloriesBurned()
    override fun getTotalTimesInMillis() = runDao.getTotalTimeInMilliSeconds()

}