package com.eldercareplus.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.eldercareplus.model.Appointment
import com.eldercareplus.receivers.AppointmentReceiver

class AppointmentScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(appointment: Appointment) {
        // Schedule reminder 2 hours before
        val reminderTime = appointment.timestamp - (2 * 60 * 60 * 1000)

        // If reminder time is in the past, don't schedule (or schedule immediately if desired, but skip for now)
        if (reminderTime <= System.currentTimeMillis()) return

        val intent = Intent(context, AppointmentReceiver::class.java).apply {
            putExtra("DOCTOR_NAME", appointment.doctorName)
            putExtra("HOSPITAL_NAME", appointment.hospitalName)
        }

        val requestCode = appointment.id.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancel(appointment: Appointment) {
        val intent = Intent(context, AppointmentReceiver::class.java)
        val requestCode = appointment.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
