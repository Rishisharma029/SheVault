package com.shevault

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class SheVaultApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannels()
    }

    private fun setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val emergencyChannel = NotificationChannel(
                "shevault_incident_channel",
                "SheVault Emergency SOS",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts during emergency SOS dispatches"
                enableVibration(true)
            }

            val routeChannel = NotificationChannel(
                "shevault_location_channel",
                "SafeRoute Journey Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background location tracking updates"
            }

            notificationManager?.createNotificationChannel(emergencyChannel)
            notificationManager?.createNotificationChannel(routeChannel)
        }
    }
}
