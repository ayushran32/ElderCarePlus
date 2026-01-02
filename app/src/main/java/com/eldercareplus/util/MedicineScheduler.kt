package com.eldercareplus.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.eldercareplus.model.Medicine
import com.eldercareplus.receivers.MedicineReceiver
import java.util.Calendar

class MedicineScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(medicine: Medicine) {
        val intent = Intent(context, MedicineReceiver::class.java).apply {
            putExtra("MEDICINE_NAME", medicine.name)
            putExtra("MEDICINE_DOSAGE", medicine.dosage)
        }

        // Use hashCode of ID for unique request code
        val requestCode = medicine.id.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate time to set
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            val timeOfDay = Calendar.getInstance().apply { timeInMillis = medicine.timeInMillis }
            set(Calendar.HOUR_OF_DAY, timeOfDay.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeOfDay.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
        }

        // If time passed, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
            // Handle permission exception
        }
    }

    fun cancel(medicine: Medicine) {
        val intent = Intent(context, MedicineReceiver::class.java)
        val requestCode = medicine.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
