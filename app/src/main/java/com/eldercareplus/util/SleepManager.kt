package com.eldercareplus.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.eldercareplus.model.SleepSession
import com.eldercareplus.receivers.SleepReceiver
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.DetectedActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID

class SleepManager(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Detection interval: 5 minutes
    private val DETECTION_Interval_MS: Long = 5 * 60 * 1000 

    @SuppressLint("MissingPermission")
    fun startTracking() {
        try {
            val intent = Intent(context, SleepReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            ActivityRecognition.getClient(context)
                .requestActivityUpdates(DETECTION_Interval_MS, pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopTracking() {
        try {
            val intent = Intent(context, SleepReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            ActivityRecognition.getClient(context)
                .removeActivityUpdates(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun processActivityUpdate(activity: DetectedActivity) {
        val user = auth.currentUser ?: return
        val timestamp = System.currentTimeMillis()
        
        // Log raw activity to Firestore (for analysis, can be optimized to local DB later)
        // Optimization: Only log STILL or SIGNIFICANT movement
        val activityType = when(activity.type) {
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.IN_VEHICLE -> "VEHICLE"
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.RUNNING -> "RUNNING"
            DetectedActivity.ON_BICYCLE -> "BICYCLE"
            else -> "UNKNOWN"
        }

        val log = hashMapOf(
            "userId" to user.uid,
            "timestamp" to timestamp,
            "activity" to activityType,
            "confidence" to activity.confidence
        )

        try {
            // Store daily logs in subcollection or root collection partitioned by date
            // For MVP: Root collection 'activity_logs'
            db.collection("activity_logs").add(log).await()
            
            // Trigger sleep analysis for "last night" if it's morning
            checkAndAnalyzeSleep(user.uid)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun checkAndAnalyzeSleep(userId: String) {
        // Run analysis only if current time is between 8 AM and 10 AM (once/day heuristic check)
        // Or simplified: run on every update but debounce.
        // Better for MVP: Analysis Button or Dashboard load triggers it.
        // Let's implement a 'analyzeLastNight()' function that can be called from UI/ViewModel.
    }
    
    suspend fun analyzeLastNight(userId: String): SleepSession? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        // 1. Get logs from yesterday 8 PM to today 10 AM
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis
        
        // End: Today 10:00 AM
        cal.set(Calendar.HOUR_OF_DAY, 10)
        cal.set(Calendar.MINUTE, 0)
        val windowEnd = cal.timeInMillis
        
        // Start: Yesterday 8:00 PM
        cal.add(Calendar.DAY_OF_YEAR, -1)
        cal.set(Calendar.HOUR_OF_DAY, 20)
        val windowStart = cal.timeInMillis

        // If we are not past the window end yet, maybe skip or analyze partial
        if (now < windowEnd) {
             // Analyzing partially or returning null
        }


        val snapshot = db.collection("activity_logs")
            .whereEqualTo("userId", userId)
            .whereGreaterThan("timestamp", windowStart)
            .get()
            .await()

        val logs = snapshot.documents
            .filter { 
                val timestamp = it.getLong("timestamp") ?: 0L
                timestamp <= windowEnd
            }
            .sortedBy { it.getLong("timestamp") ?: 0L }
        
        if (logs.isEmpty()) return@withContext null
        
        // Simple Heuristic: Longest consecutive 'STILL' block
        var longestSleepStart = 0L
        var longestSleepEnd = 0L
        var currentBlockStart = 0L
        var isSleeping = false

        for (doc in logs) {
            val type = doc.getString("activity") ?: "UNKNOWN"
            val time = doc.getLong("timestamp") ?: 0L
            
            if (type == "STILL") {
                if (!isSleeping) {
                    isSleeping = true
                    currentBlockStart = time
                }
            } else {
                if (isSleeping) {
                    isSleeping = false
                    val duration = time - currentBlockStart
                    if (duration > (longestSleepEnd - longestSleepStart)) {
                        longestSleepStart = currentBlockStart
                        longestSleepEnd = time
                    }
                }
            }
        }
        
        // Handle case where sleep continues until end of logs
        if (isSleeping) {
             val lastTime = logs.last().getLong("timestamp") ?: 0L
             val duration = lastTime - currentBlockStart
             if (duration > (longestSleepEnd - longestSleepStart)) {
                longestSleepStart = currentBlockStart
                longestSleepEnd = lastTime
             }
        }

        val durationMillis = longestSleepEnd - longestSleepStart
        val durationMins = (durationMillis / 1000 / 60).toInt()
        
        // Only count if > 3 hours
        if (durationMins < 180) return@withContext null
        
        val session = SleepSession(
            id = UUID.randomUUID().toString(),
            elderId = userId,
            date = windowEnd, // Associated with the 'morning' date
            startTime = longestSleepStart,
            endTime = longestSleepEnd,
            durationMinutes = durationMins
        )
        
        // Save to avoid re-calculation
        val check = db.collection("sleep_sessions")
            .whereEqualTo("elderId", userId)
            .whereEqualTo("date", windowEnd)
            .get()
            .await()
            
        if (check.isEmpty) {
            db.collection("sleep_sessions").add(session).await()
        }
            
        return@withContext session
    }
}
