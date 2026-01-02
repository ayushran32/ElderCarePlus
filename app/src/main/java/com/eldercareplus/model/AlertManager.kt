package com.eldercareplus.model

import android.location.Location
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AlertManager {
    private val db = FirebaseFirestore.getInstance()

    suspend fun sendAlert(elderId: String, elderName: String, location: Location?, type: String = "FALL_DETECTED") {
        val alert = hashMapOf(
            "elderId" to elderId,
            "elderName" to elderName,
            "type" to type,
            "timestamp" to System.currentTimeMillis(),
            "status" to "PENDING",
            "latitude" to (location?.latitude ?: 0.0),
            "longitude" to (location?.longitude ?: 0.0),
            "mapsUrl" to "https://www.google.com/maps/search/?api=1&query=${location?.latitude},${location?.longitude}"
        )

        try {
            android.util.Log.d("AlertManager", "Sending alert: elderId=$elderId, type=$type, location=${location?.latitude},${location?.longitude}")
            db.collection("alerts").add(alert).await()
            android.util.Log.d("AlertManager", "Alert successfully written to Firestore")
        } catch (e: Exception) {
            android.util.Log.e("AlertManager", "Failed to send alert", e)
            e.printStackTrace()
        }
    }
}
