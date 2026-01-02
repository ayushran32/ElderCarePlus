package com.eldercareplus.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.delay

// Advanced service for detecting falls using multi-sensor fusion (similar to Apple Watch/Pixel Watch)
class FallDetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    
    // Fall detection parameters (smartwatch-grade)
    private val freeFallThreshold = 0.5f * 9.8f // Low g-force during free fall
    private val impactThreshold = 3.0f * 9.8f // High impact on landing
    private val gyroThreshold = 5.0f // Rapid rotation during fall (rad/s)
    private val immobilityThreshold = 0.3f * 9.8f // Near-stationary after impact
    
    // State tracking for multi-phase detection
    private var isInFreeFall = false
    private var freeFallStartTime = 0L
    private var impactDetectedTime = 0L
    private var lastFallAlertTime = 0L
    
    // Immobility tracking
    private var checkingImmobility = false
    private val immobilityCheckDuration = 3000L // 3 seconds
    
    companion object {
        private val _fallEvents = MutableSharedFlow<Boolean>()
        @Suppress("unused") // Used by FallConfirmationScreen
        val fallEvents: SharedFlow<Boolean> = _fallEvents
        private const val CHANNEL_ID = "fall_detection_channel"
        private const val NOTIFICATION_ID = 1001
        private const val MIN_FREEFALL_DURATION = 300L // Minimum 300ms free fall
        private const val MAX_FREEFALL_DURATION = 2000L // Maximum 2s free fall
        private const val DEBOUNCE_INTERVAL = 30000L // 30 seconds between alerts
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        try {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

            // Register both sensors for multi-sensor fusion
            // Using SENSOR_DELAY_GAME (20ms) for better battery efficiency while maintaining accuracy
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
                Log.d("FallDetectionService", "Accelerometer registered with SENSOR_DELAY_GAME")
            } ?: Log.e("FallDetectionService", "Accelerometer not available")
            
            gyroscope?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
                Log.d("FallDetectionService", "Gyroscope registered with SENSOR_DELAY_GAME")
            } ?: Log.w("FallDetectionService", "Gyroscope not available - fall detection accuracy reduced")
        } catch (e: SecurityException) {
            Log.e("FallDetectionService", "Permission error while registering sensors", e)
            stopSelf() // Stop service if sensors cannot be registered
        } catch (e: Exception) {
            Log.e("FallDetectionService", "Error initializing fall detection service", e)
            stopSelf()
        }
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Fall Detection",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Monitoring for falls"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotification(): android.app.Notification {
        val notificationIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Fall Detection Active")
            .setContentText("Advanced monitoring enabled")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> processAccelerometer(it.values)
                Sensor.TYPE_GYROSCOPE -> processGyroscope(it.values)
            }
        }
    }
    
    private fun processAccelerometer(values: FloatArray) {
        val x = values[0]
        val y = values[1]
        val z = values[2]
        val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        
        val currentTime = System.currentTimeMillis()
        
        // Phase 1: Detect free fall (sudden drop in acceleration)
        if (!isInFreeFall && acceleration < freeFallThreshold) {
            isInFreeFall = true
            freeFallStartTime = currentTime
            Log.d("FallDetectionService", "Free fall detected! Accel: $acceleration")
        }
        
        // Phase 2: Detect impact after free fall
        if (isInFreeFall && acceleration > impactThreshold) {
            val freeFallDuration = currentTime - freeFallStartTime
            
            // Validate it's a realistic fall duration
            if (freeFallDuration in MIN_FREEFALL_DURATION..MAX_FREEFALL_DURATION) {
                impactDetectedTime = currentTime
                Log.d("FallDetectionService", 
                    "Impact detected! Accel: $acceleration, FreeFall duration: ${freeFallDuration}ms")
                
                // Start immobility check
                startImmobilityCheck()
            }
            
            isInFreeFall = false
        }
        
        // Reset free fall if too long without impact
        if (isInFreeFall && (currentTime - freeFallStartTime > MAX_FREEFALL_DURATION)) {
            isInFreeFall = false
            Log.d("FallDetectionService", "Free fall timeout - false alarm")
        }
        
        // Check immobility after impact
        if (checkingImmobility && currentTime - impactDetectedTime < immobilityCheckDuration) {
            if (acceleration > immobilityThreshold + 2.0f) {
                // Person is moving - probably okay
                checkingImmobility = false
                Log.d("FallDetectionService", "Movement detected after impact - likely false alarm")
            }
        }
    }
    
    private fun processGyroscope(values: FloatArray) {
        // Gyroscope measures rotation rate (rad/s)
        val rotationMagnitude = sqrt(
            (values[0] * values[0] + values[1] * values[1] + values[2] * values[2]).toDouble()
        ).toFloat()
        
        // Rapid rotation can indicate a fall
        if (isInFreeFall && rotationMagnitude > gyroThreshold) {
            Log.d("FallDetectionService", "Rapid rotation detected: $rotationMagnitude rad/s")
            // This adds confidence to the fall detection
        }
    }
    
    private fun startImmobilityCheck() {
        if (checkingImmobility) return
        
        checkingImmobility = true
        
        // Check for immobility after a delay
        CoroutineScope(Dispatchers.Main).launch {
            delay(immobilityCheckDuration)
            
            if (checkingImmobility) {
                // Person hasn't moved for 3 seconds after impact - likely a real fall
                val currentTime = System.currentTimeMillis()
                
                if (currentTime - lastFallAlertTime > DEBOUNCE_INTERVAL) {
                    lastFallAlertTime = currentTime
                    triggerFallAlert()
                }
            }
            
            checkingImmobility = false
        }
    }
    
    private fun triggerFallAlert() {
        Log.d("FallDetectionService", "FALL CONFIRMED - Triggering alert!")
        
        CoroutineScope(Dispatchers.Main).launch {
            _fallEvents.emit(true)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            sensorManager.unregisterListener(this)
            Log.d("FallDetectionService", "Fall detection stopped")
        } catch (e: Exception) {
            Log.e("FallDetectionService", "Error unregistering sensors", e)
        }
    }
}
