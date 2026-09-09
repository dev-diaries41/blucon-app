package com.fpf.blucon

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.fpf.blucon.notifications.NotificationChannels

class App : Application() {

    companion object {
        private const val TAG = "App"
    }

    override fun onCreate() {
        super.onCreate()

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