package com.eldercareplus.model

data class ApprovedCaretaker(
    val caretakerId: String,
    val linkId: String,
    val caretakerName: String = "",
    val phoneNumber: String = ""
)
