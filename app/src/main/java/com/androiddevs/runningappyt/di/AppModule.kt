package com.androiddevs.runningappyt.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import androidx.room.Room
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.db.RunDao
import com.androiddevs.runningappyt.db.RunningDataBase
import com.androiddevs.runningappyt.other.Constant.KEY_FIRST_TIME_TOGGLED
import com.androiddevs.runningappyt.other.Constant.KEY_NAME
import com.androiddevs.runningappyt.other.Constant.KEY_WEIGHT
import com.androiddevs.runningappyt.other.Constant.RUNNING_DATABASE_NAME
import com.androiddevs.runningappyt.other.Constant.SHARED_PREF_NAME
import com.androiddevs.runningappyt.repository.DefaultMainRepository
import com.androiddevs.runningappyt.repository.MainRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.RemoteConfigConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideRunningDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app, RunningDataBase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideDao(
        database: RunningDataBase
    ) = database.getRunDao()




    @Provides
    @Singleton
    fun provideMainRepository(
        runDao: RunDao
    ) : MainRepository = DefaultMainRepository(
        runDao
    )

    @Provides
    @Singleton
    fun provideSharePrefObject(
        @ApplicationContext app:Context
    ): SharedPreferences = app.getSharedPreferences(
        SHARED_PREF_NAME,MODE_PRIVATE
    )


    @Provides
    @Singleton
    fun provideName(
        sharedPref:SharedPreferences
    )  = sharedPref.getString(KEY_NAME,"") ?: ""

    @Provides
    @Singleton
    fun provideWeight(
        sharedPref:SharedPreferences
    )  = sharedPref.getFloat(KEY_WEIGHT,80.0f)

    @Provides
    @Singleton
    fun provideFirstTimeToggle(
        sharedPref:SharedPreferences
    )  = sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLED,true)


    @Provides
    @Singleton
    fun provideFireBaseInstance(
        @ApplicationContext app: Context
    ) :FirebaseAnalytics = FirebaseAnalytics.getInstance(app)


    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(
         config: FirebaseRemoteConfigSettings
    ): FirebaseRemoteConfig {
        var remote = FirebaseRemoteConfig.getInstance()
        remote.setConfigSettingsAsync(config)
        remote.setDefaultsAsync(R.xml.remote_config_defaults)
        remote.fetchAndActivate().addOnCompleteListener {
            Log.d("TAG", "provideFirebaseRemoteConfig: ")
        }


         return remote



    }

    @Provides
    @Singleton
    fun provideFireBaseRemoteConfigSettings():FirebaseRemoteConfigSettings{


        return FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()

    }
}




