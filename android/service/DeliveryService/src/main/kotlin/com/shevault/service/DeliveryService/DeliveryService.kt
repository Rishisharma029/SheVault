package com.shevault.service.DeliveryService

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * DeliveryService: Reliable encrypted sync of offline breadcrumbs,
 * voice recordings, and incident evidence to backend servers.
 */
class DeliveryService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val CHANNEL_ID = "shevault_delivery_channel"
        const val NOTIFICATION_ID = 9004
        const val ACTION_SYNC_PENDING = "com.shevault.action.SYNC_PENDING"

        fun triggerSync(context: Context) {
            val intent = Intent(context, DeliveryService::class.java).apply {
                action = ACTION_SYNC_PENDING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_SYNC_PENDING) {
            startForegroundDelivery()
            syncOfflineVaultData()
        }
        return START_NOT_STICKY
    }

    private fun startForegroundDelivery() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SheVault Sync")
            .setContentText("Uploading encrypted safety logs...")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun syncOfflineVaultData() {
        serviceScope.launch {
            try {
                // Batch upload queued offline reports
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Evidence Delivery Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
