package com.eldercareplus.model

data class SleepSession(
    val id: String = "",
    val elderId: String = "",
    val date: Long = 0L,         // Midnight timestamp of the sleep night
    val startTime: Long = 0L,    // When sleep started
    val endTime: Long = 0L,      // When wake up happened
    val durationMinutes: Int = 0 
)
