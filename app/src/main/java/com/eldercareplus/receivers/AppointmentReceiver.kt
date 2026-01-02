package com.eldercareplus.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.eldercareplus.R

class AppointmentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val doctorName = intent.getStringExtra("DOCTOR_NAME") ?: "Doctor"
        val hospitalName = intent.getStringExtra("HOSPITAL_NAME") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "AppointmentChannel"

        val channel = NotificationChannel(
            channelId,
            "Appointment Reminders",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Appointment Reminder")
            .setContentText("You have an appointment with $doctorName at $hospitalName in 2 hours.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
