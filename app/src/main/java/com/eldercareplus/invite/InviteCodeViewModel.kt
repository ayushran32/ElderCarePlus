package com.eldercareplus.invite

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class InviteCodeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _inviteCode = MutableStateFlow<String?>(null)
    val inviteCode: StateFlow<String?> = _inviteCode

    fun generateInviteCode() {
        val user = auth.currentUser ?: return
        val code = UUID.randomUUID()
            .toString()
            .substring(0, 6)
            .uppercase()

        val data = mapOf(
            "code" to code,
            "elderId" to user.uid, // Correct field
            "role" to "elder",     // Correct role
            "expiresAt" to (System.currentTimeMillis() + 48 * 60 * 60 * 1000), // 48 hrs Validity
            "isReusable" to true
        )

        db.collection("invite_codes") // Consistent collection name
            .document(code)
            .set(data)
            .addOnSuccessListener {
                _inviteCode.value = code
            }
    }
}
