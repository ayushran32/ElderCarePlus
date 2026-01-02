package com.eldercareplus.model

data class Alert(
    val id: String = "",
    val elderId: String = "",
    val elderName: String = "",
    val type: String = "",
    val timestamp: Long = 0,
    val status: String = "PENDING",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val mapsUrl: String = ""
)
