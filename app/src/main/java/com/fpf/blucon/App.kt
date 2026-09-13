package com.fpf.blucon

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.fpf.blucon.di.dbModule
import com.fpf.blucon.di.modelModule
import com.fpf.blucon.di.storageModule
import com.fpf.blucon.di.viewModelModule
import com.fpf.blucon.notifications.NotificationChannels
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class App : Application() {

    companion object {
        private const val TAG = "App"

        fun resetKoin(app: Application){
            stopKoin()
            startKoin {
                androidContext(app)
                modules(
                    dbModule,
                    viewModelModule,
                    storageModule,
                    modelModule
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
                dbModule,
                viewModelModule,
                storageModule,
                modelModule
            )
        }

        createNotificationChannels()
    }
    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        val scanChannel = NotificationChannel(
            NotificationChannels.SCAN,
            getString(R.string.scan_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.scan_channel_description)
        }

        notificationManager.createNotificationChannels(
            listOf(scanChannel)
        )
    }
}