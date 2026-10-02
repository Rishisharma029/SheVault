package com.shevault.service.IncidentService

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
 * IncidentService: Coordinates active SOS events.
 * Runs as a foreground service with types `location`, `microphone`, and `dataSync` (Android 14+).
 */
class IncidentService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val CHANNEL_ID = "shevault_incident_channel"
        const val NOTIFICATION_ID = 9001
        const val ACTION_START_SOS = "com.shevault.action.START_SOS"
        const val ACTION_STOP_SOS = "com.shevault.action.STOP_SOS"
        const val EXTRA_TRIGGER_REASON = "extra_trigger_reason"

        fun startEmergency(context: Context, reason: String) {
            val intent = Intent(context, IncidentService::class.java).apply {
                action = ACTION_START_SOS
                putExtra(EXTRA_TRIGGER_REASON, reason)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopEmergency(context: Context) {
            val intent = Intent(context, IncidentService::class.java).apply {
                action = ACTION_STOP_SOS
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SOS -> {
                val reason = intent.getStringExtra(EXTRA_TRIGGER_REASON) ?: "SOS Triggered"
                startForegroundWithTypes(reason)
                executeSosProtocol(reason)
            }
            ACTION_STOP_SOS -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundWithTypes(reason: String) {
        val notification = buildEmergencyNotification(reason)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val foregroundServiceTypes =
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION or
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                foregroundServiceTypes
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun executeSosProtocol(reason: String) {
        serviceScope.launch {
            // 1. Gather GPS Fix
            // 2. Dispatch alert to trusted contacts via backend API & SMS fallback
            // 3. Initiate encrypted audio recording buffer
            // 4. Open high-frequency location streaming websocket
        }
    }

    private fun buildEmergencyNotification(reason: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SheVault Emergency SOS Active")
            .setContentText("Transmitting live location and evidence: $reason")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Emergency SOS Incidents",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts when emergency SOS is active"
                enableVibration(true)
            }
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
