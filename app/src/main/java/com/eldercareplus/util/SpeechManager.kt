package com.eldercareplus.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SpeechManager(private val context: Context, private val onKeywordDetected: (String) -> Unit) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private val keywords = listOf("help", "pain", "emergency", "save me")

    fun startListening() {
        if (isListening) return
        
        // Check if speech recognition is available
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e("SpeechManager", "Speech recognition is not available on this device")
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                // Restart listening when speech ends to support continuous listening
                restartListening()
            }

            override fun onError(error: Int) {
                // Restart if error occurs (common in continuous listening)
                // Add tiny delay to prevent rapid-fire restart loops on permanent errors
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    restartListening()
                }, 1000)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.forEach { text ->
                    val lowerText = text.lowercase()
                    Log.d("SpeechManager", "Heard: $lowerText")
                    keywords.forEach { keyword ->
                        if (lowerText.contains(keyword)) {
                            onKeywordDetected(keyword)
                        }
                    }
                }
                restartListening()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Optional: Check partial results for faster detection
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            Log.e("SpeechManager", "Failed to start speech recognition", e)
            isListening = false
        }
    }

    private fun restartListening() {
        if (isListening) {
            speechRecognizer?.destroy()
            isListening = false // Reset state so startListening can proceed
            startListening()
        }
    }

    fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
