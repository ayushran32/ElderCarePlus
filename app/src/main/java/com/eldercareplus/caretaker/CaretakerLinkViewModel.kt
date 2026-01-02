package com.eldercareplus.caretaker

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CaretakerLinkViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status

    private val _linkedElders = MutableStateFlow<List<com.eldercareplus.model.LinkedElder>>(emptyList())
    val linkedElders: StateFlow<List<com.eldercareplus.model.LinkedElder>> = _linkedElders

    init {
        loadLinkedElders()
    }

    private fun loadLinkedElders() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("links")
            .whereEqualTo("caretakerId", uid)
            .whereEqualTo("status", "approved")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val elders = snapshot.documents.map { 
                        com.eldercareplus.model.LinkedElder(
                            linkId = it.id,
                            elderId = it.getString("elderId") ?: "",
                            elderName = "Elder" // Default name
                        )
                    }
                    
                    // Set the list immediately with default names
                    _linkedElders.value = elders
                    
                    // Fetch display names for each elder asynchronously and update
                    elders.forEach { elder ->
                        db.collection("users").document(elder.elderId).get()
                            .addOnSuccessListener { userDoc ->
                                val name = userDoc.getString("displayName") ?: "Elder"
                                _linkedElders.value = _linkedElders.value.map {
                                    if (it.elderId == elder.elderId) {
                                        it.copy(elderName = name)
                                    } else {
                                        it
                                    }
                                }
                            }
                    }
                }
            }
    }

    fun submitInviteCode(codeInput: String) {
        val caretakerId = auth.currentUser?.uid ?: return
        val code = codeInput.trim().uppercase()

        if (code.isBlank()) {
            _status.value = "Enter invite code"
            return
        }

        // Fix collection name to match InviteCodeViewModel
        val codeRef = db.collection("invite_codes").document(code)

        codeRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                _status.value = "Invalid invite code"
                return@addOnSuccessListener
            }

            // Removed 'used' check since codes are reusable
            val expiresAt = doc.getLong("expiresAt") ?: 0L
            val elderId = doc.getString("elderId")

            if (System.currentTimeMillis() > expiresAt) {
                _status.value = "Invite code expired"
                return@addOnSuccessListener
            }

            if (elderId == null) {
                _status.value = "Invalid invite data (No Elder ID)"
                return@addOnSuccessListener
            }

            // Check if link already exists
            db.collection("links")
                .whereEqualTo("elderId", elderId)
                .whereEqualTo("caretakerId", caretakerId)
                .get()
                .addOnSuccessListener { existing ->
                    if (!existing.isEmpty) {
                        _status.value = "You are already linked to this Elder"
                    } else {
                        val linkData = hashMapOf(
                            "elderId" to elderId,
                            "caretakerId" to caretakerId,
                            "status" to "pending",
                            "createdAt" to System.currentTimeMillis()
                        )

                        db.collection("links")
                            .add(linkData)
                            .addOnSuccessListener {
                                // Do NOT mark as used
                                _status.value = "Request sent. Waiting for elder approval."
                            }
                    }
                }
        }.addOnFailureListener {
            _status.value = "Network error. Try again."
        }
    }

    fun clearStatus() {
        _status.value = null
    }
}
