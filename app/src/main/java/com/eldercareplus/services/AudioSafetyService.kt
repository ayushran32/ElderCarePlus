package com.eldercareplus.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.eldercareplus.R
import com.eldercareplus.model.AlertManager
import com.eldercareplus.util.LocationHelper
import com.eldercareplus.util.AmplitudeDetector
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Service for detecting distress keywords
class AudioSafetyService : Service() {

    private lateinit var amplitudeDetector: AmplitudeDetector
    private lateinit var alertManager: AlertManager
    private lateinit var locationHelper: LocationHelper

    override fun onCreate() {
        super.onCreate()
        alertManager = AlertManager()
        locationHelper = LocationHelper(this)
        
        // Initialize Amplitude Detector
        amplitudeDetector = AmplitudeDetector(this) {
            Log.d("AudioSafetyService", "Loud sound detected!")
            triggerAlert("LOUD_SOUND")
        }
        
        startForeground(2, createNotification())
        amplitudeDetector.startMonitoring()
    }

    private fun triggerAlert(reason: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        
        Log.d("AudioSafetyService", "Triggering alert for: $reason, user: ${user.uid}")
        
        CoroutineScope(Dispatchers.IO).launch {
            val location = locationHelper.getCurrentLocation()
            Log.d("AudioSafetyService", "Got location: lat=${location?.latitude}, long=${location?.longitude}")
            
            alertManager.sendAlert(
                elderId = user.uid,
                elderName = user.displayName ?: "Elder",
                location = location,
                type = "AUTO_DETECTION: $reason"
            )
            
            Log.d("AudioSafetyService", "Alert sent to Firestore")
        }
    }

    private fun createNotification(): Notification {
        val channelId = "AudioSafetyChannel"
        val channel = NotificationChannel(
            channelId,
            "Safety Audio Monitoring",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Audio Safety Active")
            .setContentText("Monitoring for loud sounds...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Ensure monitoring starts/restarts
        amplitudeDetector.startMonitoring()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        amplitudeDetector.stopMonitoring()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
