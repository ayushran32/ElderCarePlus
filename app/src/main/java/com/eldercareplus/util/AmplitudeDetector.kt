package com.eldercareplus.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlin.math.sqrt

class AmplitudeDetector(
    private val context: Context,
    private val onLoudSoundDetected: () -> Unit
) {
    private var audioRecord: AudioRecord? = null
    private var isMonitoring = false
    private var monitoringThread: Thread? = null
    
    private var lastAlertTime = 0L
    private val DEBOUNCE_INTERVAL_MS = 30000L // 30 seconds between alerts
    private val AMPLITUDE_THRESHOLD = 20000 // Higher threshold to avoid false alerts
    
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    fun startMonitoring() {
        if (isMonitoring) return
        
        // Check permission
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("AmplitudeDetector", "RECORD_AUDIO permission not granted")
            return
        }

        try {
            val bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("AmplitudeDetector", "AudioRecord not initialized")
                return
            }

            isMonitoring = true
            audioRecord?.startRecording()

            monitoringThread = Thread {
                monitorAudioLevels(bufferSize)
            }
            monitoringThread?.start()
            
            Log.d("AmplitudeDetector", "Started monitoring audio amplitude")
        } catch (e: Exception) {
            Log.e("AmplitudeDetector", "Failed to start monitoring", e)
            isMonitoring = false
        }
    }

    private fun monitorAudioLevels(bufferSize: Int) {
        val buffer = ShortArray(bufferSize)

        while (isMonitoring) {
            try {
                val readSize = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                
                if (readSize > 0) {
                    val amplitude = calculateRMS(buffer, readSize)
                    
                    // Check if amplitude exceeds threshold and debounce interval has passed
                    val currentTime = System.currentTimeMillis()
                    if (amplitude > AMPLITUDE_THRESHOLD && 
                        currentTime - lastAlertTime > DEBOUNCE_INTERVAL_MS) {
                        
                        Log.d("AmplitudeDetector", "Loud sound detected! Amplitude: $amplitude")
                        lastAlertTime = currentTime
                        onLoudSoundDetected()
                    }
                }
            } catch (e: Exception) {
                Log.e("AmplitudeDetector", "Error reading audio", e)
                break
            }
        }
    }

    private fun calculateRMS(buffer: ShortArray, readSize: Int): Int {
        var sum = 0.0
        for (i in 0 until readSize) {
            sum += (buffer[i] * buffer[i]).toDouble()
        }
        val mean = sum / readSize
        return sqrt(mean).toInt()
    }

    fun stopMonitoring() {
        isMonitoring = false
        monitoringThread?.interrupt()
        monitoringThread = null
        
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            Log.d("AmplitudeDetector", "Stopped monitoring")
        } catch (e: Exception) {
            Log.e("AmplitudeDetector", "Error stopping monitoring", e)
        }
    }
}
