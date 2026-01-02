package com.eldercareplus.model

data class EmergencyContact(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val relationship: String = "", // e.g., "Son", "Daughter", "Friend", "Doctor"
    val addedBy: String = "", // userId of who added this contact
    val addedByRole: String = "" // "ELDER" or "CARETAKER"
)
