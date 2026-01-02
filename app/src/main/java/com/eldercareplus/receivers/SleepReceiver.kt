package com.eldercareplus.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.eldercareplus.util.SleepManager
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SleepReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            result?.let {
                val mostProbableActivity = it.mostProbableActivity
                
                // Only process SIGNIFICANT transitions or confidence > 75
                if (mostProbableActivity.confidence >= 75) {
                    CoroutineScope(Dispatchers.IO).launch {
                        SleepManager(context).processActivityUpdate(mostProbableActivity)
                    }
                }
            }
        }
    }
}
