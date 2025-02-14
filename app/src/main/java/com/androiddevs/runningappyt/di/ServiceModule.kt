package com.androiddevs.runningappyt.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.other.Constant
import com.androiddevs.runningappyt.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Singleton


@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideFusedLocationProvidedClient(
        @ApplicationContext app : Context
    ) = LocationServices.getFusedLocationProviderClient(app)


    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext app: Context
    ) = PendingIntent.getActivity(
        app, 0, Intent(app, MainActivity::class.java).also {
            it.action = Constant.ACTION_SHOW_TRACKING_FRAGMENT
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    )

    @Provides
    @ServiceScoped
    fun provideBaseNotificationBuilder(
        @ApplicationContext app:Context,
        pendingIntent: PendingIntent
    ) =  NotificationCompat.Builder(app, Constant.NOTIFICATION_CHANNEL_ID).setAutoCancel(false)
        .setOngoing(true).setSmallIcon(R.drawable.ic_directions_run_black_24dp)
        .setContentTitle("Running App").setContentText("00:00:00")
        .setContentIntent(pendingIntent)
}