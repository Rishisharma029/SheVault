package com.shevault.service.SensorService

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.shevault.service.IncidentService.IncidentService
import kotlin.math.sqrt

/**
 * SensorService: Monitors device accelerometer for emergency triggers
 * (violent shake detection or fall impact).
 */
class SensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastShakeTime = 0L

    companion object {
        const val CHANNEL_ID = "shevault_sensor_channel"
        const val NOTIFICATION_ID = 9003
        const val ACTION_START_SENSORS = "com.shevault.action.START_SENSORS"
        const val ACTION_STOP_SENSORS = "com.shevault.action.STOP_SENSORS"
        private const val SHAKE_THRESHOLD_G_FORCE = 3.2f
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SENSORS -> {
                startForegroundSensor()
                accelerometer?.let {
                    sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                }
            }
            ACTION_STOP_SENSORS -> {
                sensorManager.unregisterListener(this)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundSensor() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SheVault Sensor Shield")
            .setContentText("Emergency shake detection active.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
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

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val x = event.values[0] / SensorManager.GRAVITY_EARTH
        val y = event.values[1] / SensorManager.GRAVITY_EARTH
        val z = event.values[2] / SensorManager.GRAVITY_EARTH

        val gForce = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        if (gForce > SHAKE_THRESHOLD_G_FORCE) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > 3000) {
                lastShakeTime = now
                // Trigger Emergency Incident
                IncidentService.startEmergency(this, "Emergency Shake Detected")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hardware Sensor Trigger",
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
