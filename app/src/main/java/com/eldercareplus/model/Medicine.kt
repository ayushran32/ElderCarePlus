package com.eldercareplus.model

data class Medicine(
    val id: String = "",
    val name: String = "",
    val dosage: String = "",
    val timeInMillis: Long = 0L,
    val frequency: String = "Daily", // Daily, Weekly
    val elderId: String = ""
)
