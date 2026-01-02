package com.eldercareplus.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.eldercareplus.model.Alert
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Service that listens for urgent alerts from linked elders
 * Provides HIGH PRIORITY notifications with vibration and sound
 */
class CaretakerAlertService : Service() {
    
    private val db = FirebaseFirestore.getInstance()
    private var alertListener: ListenerRegistration? = null
    private lateinit var vibrator: Vibrator
    private var lastAlertTime = 0L
    
    companion object {
        const val CHANNEL_ID = "urgent_elder_alerts"
        const val NOTIFICATION_ID = 2001
        private const val DEBOUNCE_MS = 5000L // 5 seconds between alerts
        
        // Vibration pattern: [delay, vibrate, sleep, vibrate, ...]
        val URGENT_VIBRATION_PATTERN = longArrayOf(0, 500, 200, 500, 200, 500)
    }
    
    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        createUrgentNotificationChannel()
        startForeground(NOTIFICATION_ID, createForegroundNotification())
        startListeningForAlerts()
        
        Log.d("CaretakerAlertService", "Service started")
    }
    
    private fun createUrgentNotificationChannel() {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
        
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Urgent Elder Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Critical alerts from elders requiring immediate attention"
            enableVibration(true)
            vibrationPattern = URGENT_VIBRATION_PATTERN
            setSound(soundUri, audioAttributes)
            enableLights(true)
            lightColor = android.graphics.Color.RED
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createForegroundNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Elder Care Monitoring")
            .setContentText("Watching for urgent alerts")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    private fun startListeningForAlerts() {
        val caretakerId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        Log.d("CaretakerAlertService", "Starting alert listener for caretaker: $caretakerId")
        
        // Listen to alerts collection for any elders linked to this caretaker
        alertListener = db.collection("links")
            .whereEqualTo("caretakerId", caretakerId)
            .whereEqualTo("status", "approved")
            .addSnapshotListener { linksSnapshot, error ->
                if (error != null) {
                    Log.e("CaretakerAlertService", "Error listening for links", error)
                    return@addSnapshotListener
                }
                
                // Get all linked elder IDs
                val elderIds = linksSnapshot?.documents?.mapNotNull { 
                    it.getString("elderId") 
                } ?: emptyList()
                
                if (elderIds.isEmpty()) {
                    Log.d("CaretakerAlertService", "No linked elders")
                    return@addSnapshotListener
                }
                
                Log.d("CaretakerAlertService", "Monitoring ${elderIds.size} elders")
                
                // Listen for alerts from these elders
                elderIds.forEach { elderId ->
                    listenForElderAlerts(elderId)
                }
            }
    }
    
    private fun listenForElderAlerts(elderId: String) {
        db.collection("alerts")
            .whereEqualTo("elderId", elderId)
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CaretakerAlertService", "Error listening for alerts", error)
                    return@addSnapshotListener
                }
                
                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val alert = change.document.toObject(Alert::class.java)
                        handleNewAlert(alert, change.document.id)
                    }
                }
            }
    }
    
    private fun handleNewAlert(alert: Alert, alertId: String) {
        val currentTime = System.currentTimeMillis()
        
        // Debounce to prevent spam
        if (currentTime - lastAlertTime < DEBOUNCE_MS) {
            return
        }
        lastAlertTime = currentTime
        
        Log.d("CaretakerAlertService", "NEW URGENT ALERT: ${alert.type} from ${alert.elderName}")
        
        // Vibrate urgently
        triggerUrgentVibration()
        
        // Show high-priority notification
        showUrgentNotification(alert, alertId)
        
        // Broadcast to open popup if app is in foreground
        broadcastAlertToApp(alert, alertId)
    }
    
    private fun triggerUrgentVibration() {
        if (vibrator.hasVibrator()) {
            val effect = VibrationEffect.createWaveform(
                URGENT_VIBRATION_PATTERN,
                intArrayOf(0, 255, 0, 255, 0, 255),
                -1 // Don't repeat
            )
            vibrator.vibrate(effect)
        }
    }
    
    private fun showUrgentNotification(alert: Alert, alertId: String) {
        val intent = Intent(this, Class.forName("com.eldercareplus.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("ALERT_ID", alertId)
            putExtra("ELDER_ID", alert.elderId)
            putExtra("ALERT_TYPE", alert.type)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            alertId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🚨 URGENT: ${alert.elderName} Needs Help!")
            .setContentText(getAlertMessage(alert.type))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setVibrate(URGENT_VIBRATION_PATTERN)
            .setFullScreenIntent(pendingIntent, true) // Show full-screen on locked screen
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColorized(true)
            .setColor(android.graphics.Color.RED)
            .addAction(
                android.R.drawable.ic_menu_call,
                "Call Now",
                pendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_mapmode,
                "View Location",
                pendingIntent
            )
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${alert.elderName} may have fallen or is in distress. " +
                        "Location: ${alert.latitude}, ${alert.longitude}\n" +
                        "Type: ${alert.type}\n" +
                        "Tap to respond immediately."))
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(alertId.hashCode(), notification)
        
        Log.d("CaretakerAlertService", "Urgent notification shown")
    }
    
    private fun getAlertMessage(type: String): String {
        return when {
            type.contains("FALL") -> "Fall detected! Check immediately"
            type.contains("LOUD_SOUND") -> "Distress sound detected"
            type.contains("SOS") -> "Emergency SOS activated"
            type.contains("MANUAL") -> "Manual emergency alert"
            else -> "Urgent assistance needed"
        }
    }
    
    private fun broadcastAlertToApp(alert: Alert, alertId: String) {
        val broadcastIntent = Intent("com.eldercareplus.URGENT_ALERT").apply {
            putExtra("ALERT_ID", alertId)
            putExtra("ELDER_ID", alert.elderId)
            putExtra("ELDER_NAME", alert.elderName)
            putExtra("ALERT_TYPE", alert.type)
            putExtra("LATITUDE", alert.latitude)
            putExtra("LONGITUDE", alert.longitude)
            putExtra("MAPS_URL", alert.mapsUrl)
        }
        sendBroadcast(broadcastIntent)
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        alertListener?.remove()
        Log.d("CaretakerAlertService", "Service stopped")
    }
}
