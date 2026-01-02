package com.eldercareplus.model

data class Appointment(
    val id: String = "",
    val elderId: String = "",
    val doctorName: String = "",
    val hospitalName: String = "",
    val timestamp: Long = 0L,
    val notifyCaretaker: Boolean = false
)
