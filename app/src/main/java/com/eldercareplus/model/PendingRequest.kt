package com.eldercareplus.model

data class PendingRequest(
    val caretakerId: String,
    val linkId: String,
    val caretakerName: String = ""
)
