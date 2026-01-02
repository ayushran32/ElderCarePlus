package com.eldercareplus.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eldercareplus.model.Alert
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlertViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _activeAlerts = MutableStateFlow<List<Alert>>(emptyList())
    val activeAlerts: StateFlow<List<Alert>> = _activeAlerts

    private var linkedElderIds: List<String> = emptyList()
    private var listenerRegistered = false

    // Called when the Dashboard loads and we know our linked elders
    fun setLinkedElders(ids: List<String>) {
        android.util.Log.d("AlertViewModel", "setLinkedElders called with: $ids")
        
        if (ids.isEmpty()) {
            android.util.Log.d("AlertViewModel", "No linked elders, skipping alert listener")
            return
        }
        
        // Only skip if the list is exactly the same AND listener is already registered
        if (ids.toSet() == linkedElderIds.toSet() && listenerRegistered) {
            android.util.Log.d("AlertViewModel", "Elder IDs unchanged and listener active, skipping")
            return
        }
        
        linkedElderIds = ids
        listenForAlerts()
    }

    private fun listenForAlerts() {
        if (linkedElderIds.isEmpty()) {
            android.util.Log.d("AlertViewModel", "Cannot listen for alerts: no linked elders")
            return
        }

        android.util.Log.d("AlertViewModel", "Setting up alert listener for elders: $linkedElderIds")
        
        // Firestore 'in' queries are limited to 10 items, but for a prototype this is fine.
        // For production, use Cloud Functions + FCM for push notifications
        
        db.collection("alerts")
            .whereIn("elderId", linkedElderIds)
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("AlertViewModel", "Error listening for alerts", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val alerts = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Alert::class.java)?.copy(id = doc.id)
                    }
                    android.util.Log.d("AlertViewModel", "Received ${alerts.size} pending alerts")
                    _activeAlerts.value = alerts
                } else {
                    android.util.Log.d("AlertViewModel", "Snapshot is null")
                }
            }
        
        listenerRegistered = true
        android.util.Log.d("AlertViewModel", "Alert listener registered successfully")
    }

    fun acknowledgeAlert(alertId: String) {
        android.util.Log.d("AlertViewModel", "Acknowledging alert: $alertId")
        db.collection("alerts").document(alertId)
            .update("status", "ACKNOWLEDGED")
            .addOnSuccessListener {
                android.util.Log.d("AlertViewModel", "Alert acknowledged successfully")
            }
            .addOnFailureListener { error ->
                android.util.Log.e("AlertViewModel", "Failed to acknowledge alert", error)
            }
    }
}
