package com.eldercareplus.elder

import androidx.lifecycle.ViewModel
import com.eldercareplus.model.ApprovedCaretaker
import com.eldercareplus.model.PendingRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ElderApprovedViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _approvedCaretakers =
        MutableStateFlow<List<ApprovedCaretaker>>(emptyList())
    val approvedCaretakers: StateFlow<List<ApprovedCaretaker>> = _approvedCaretakers

    private val _currentUserName = MutableStateFlow<String>("Elder")
    val currentUserName: StateFlow<String> = _currentUserName
    
    private val _linkCode = MutableStateFlow<String>("")
    val linkCode: StateFlow<String> = _linkCode
    
    private val _pendingRequests = MutableStateFlow<List<PendingRequest>>(emptyList())
    val pendingRequests: StateFlow<List<PendingRequest>> = _pendingRequests

    init {
        loadUserName()
        loadApprovedCaretakers()
        loadPendingRequests()
        loadLinkCode()
    }

    private fun loadUserName() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener {
                val name = it.getString("displayName") ?: "Elder"
                _currentUserName.value = name
            }
    }

    fun loadApprovedCaretakers() {
        val elderId = auth.currentUser?.uid ?: return

        db.collection("links")
            .whereEqualTo("elderId", elderId)
            .whereEqualTo("status", "approved")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                val caretakers = snapshot.documents.map {
                    ApprovedCaretaker(
                        caretakerId = it.getString("caretakerId") ?: "",
                        linkId = it.id,
                        caretakerName = "Caretaker", // Default name
                        phoneNumber = "" // Will be fetched
                    )
                }
                
                // Set the list immediately with defaults
                _approvedCaretakers.value = caretakers
                
                // Fetch display names and phone numbers for each caretaker
                caretakers.forEach { caretaker ->
                    db.collection("users").document(caretaker.caretakerId).get()
                        .addOnSuccessListener { userDoc ->
                            val name = userDoc.getString("displayName") ?: "Caretaker"
                            val phone = userDoc.getString("phoneNumber") ?: ""
                            
                            _approvedCaretakers.value = _approvedCaretakers.value.map {
                                if (it.caretakerId == caretaker.caretakerId) {
                                    it.copy(caretakerName = name, phoneNumber = phone)
                                } else {
                                    it
                                }
                            }
                        }
                }
            }
    }


    fun loadPendingRequests() {
        val elderId = auth.currentUser?.uid ?: return

        db.collection("links")
            .whereEqualTo("elderId", elderId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                val requests = snapshot.documents.map {
                    PendingRequest(
                        caretakerId = it.getString("caretakerId") ?: "Unknown",
                        linkId = it.id,
                        caretakerName = ""
                    )
                }
                _pendingRequests.value = requests
                
                // Fetch caretaker names
                requests.forEach { request ->
                    db.collection("users").document(request.caretakerId).get()
                        .addOnSuccessListener { userDoc ->
                            val name = userDoc.getString("displayName") ?: "Unknown"
                            val updated = _pendingRequests.value.map {
                                if (it.linkId == request.linkId) it.copy(caretakerName = name) else it
                            }
                            _pendingRequests.value = updated
                        }
                }
            }
    }

    fun approveRequest(linkId: String) {
        db.collection("links").document(linkId).update("status", "approved")
    }

    fun rejectRequest(linkId: String) {
        db.collection("links").document(linkId).delete()
    }

    fun revoke(linkId: String) {
        db.collection("links").document(linkId).delete()
    }
    
    // Link code management
    private fun loadLinkCode() {
        val elderId = auth.currentUser?.uid ?: return
        db.collection("users").document(elderId).get()
            .addOnSuccessListener {
                val code = it.getString("linkCode") ?: generateRandomCode()
                _linkCode.value = code
                if (!it.contains("linkCode")) {
                    it.reference.update("linkCode", code)
                }
            }
    }
    
    fun generateNewLinkCode() {
        val elderId = auth.currentUser?.uid ?: return
        val newCode = generateRandomCode()
        db.collection("users").document(elderId)
            .update("linkCode", newCode)
            .addOnSuccessListener {
                _linkCode.value = newCode
            }
    }
    
    private fun generateRandomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
    
    // Caretaker management functions
    fun approveLinking(linkId: String) {
        db.collection("links").document(linkId).update("status", "approved")
    }
    
    fun denyLinking(linkId: String) {
        db.collection("links").document(linkId).delete()
    }
    
    fun unlinkCaretaker(linkId: String) {
        db.collection("links").document(linkId).delete()
    }
}
