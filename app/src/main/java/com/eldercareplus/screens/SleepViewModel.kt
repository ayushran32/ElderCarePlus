package com.eldercareplus.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eldercareplus.model.SleepSession
import com.eldercareplus.util.SleepManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SleepViewModel(private val context: Context) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val sleepManager = SleepManager(context)
    
    // SharedPreferences for persisting tracking state
    private val prefs = context.getSharedPreferences("sleep_tracking_prefs", Context.MODE_PRIVATE)
    private val PREF_KEY_TRACKING = "is_sleep_tracking_enabled"

    private val _sleepSessions = MutableStateFlow<List<SleepSession>>(emptyList())
    val sleepSessions: StateFlow<List<SleepSession>> = _sleepSessions

    private val _lastNightSleep = MutableStateFlow<SleepSession?>(null)
    val lastNightSleep: StateFlow<SleepSession?> = _lastNightSleep

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking

    private var targetElderId: String? = null

    fun setTargetElderId(id: String) {
        targetElderId = id
        loadData()
    }

    init {
        // Restore tracking state from SharedPreferences
        checkTrackingStatus()
    }

    private fun checkTrackingStatus() {
        // Restore tracking state from persistent storage
        val savedState = prefs.getBoolean(PREF_KEY_TRACKING, false)
        _isTracking.value = savedState
        
        // If it was enabled, ensure tracking is actually running
        if (savedState) {
            sleepManager.startTracking()
        }
    }

    fun toggleTracking(enable: Boolean) {
        if (enable) {
            sleepManager.startTracking()
        } else {
            sleepManager.stopTracking()
        }
        
        // Update UI state
        _isTracking.value = enable
        
        // Save to persistent storage
        prefs.edit().putBoolean(PREF_KEY_TRACKING, enable).apply()
    }

    fun loadData() {
        val elderId = targetElderId ?: auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            // Load Last Night
            try {
                val lastNight = sleepManager.analyzeLastNight(elderId)
                _lastNightSleep.value = lastNight
            } catch (e: Exception) {
                android.util.Log.e("SleepViewModel", "Failed to analyze last night (likely missing Firestore index)", e)
                _lastNightSleep.value = null
            }
        }
        
        viewModelScope.launch {
            // Load History
            try {
                val snapshot = db.collection("sleep_sessions")
                    .whereEqualTo("elderId", elderId)
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(7) // Last 7 days
                    .get()
                    .await()

                _sleepSessions.value = snapshot.toObjects(SleepSession::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class SleepViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SleepViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SleepViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
