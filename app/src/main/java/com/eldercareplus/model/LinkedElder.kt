package com.eldercareplus.model

data class LinkedElder(
    val linkId: String = "",
    val elderId: String = "",
    val elderName: String = "Elder" // In a real app, fetch name from 'users' collection
)
