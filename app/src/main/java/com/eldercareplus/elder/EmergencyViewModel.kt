package com.eldercareplus.elder

import androidx.lifecycle.ViewModel
import com.eldercareplus.model.EmergencyContact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log

class EmergencyViewModel : ViewModel() {
    
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _emergencyContacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val emergencyContacts: StateFlow<List<EmergencyContact>> = _emergencyContacts
    
    init {
        loadEmergencyContacts()
    }
    
    private fun loadEmergencyContacts() {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("users").document(userId)
            .collection("emergency_contacts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("EmergencyViewModel", "Error loading contacts", error)
                    return@addSnapshotListener
                }
                
                val contacts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(EmergencyContact::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                _emergencyContacts.value = contacts
            }
    }
    
    fun addEmergencyContact(
        name: String,
        phoneNumber: String,
        relationship: String,
        userRole: String // "ELDER" or "CARETAKER"
    ) {
        val userId = auth.currentUser?.uid ?: return
        val currentUserId = auth.currentUser?.uid ?: return
        
        val contact = hashMapOf(
            "name" to name,
            "phoneNumber" to phoneNumber,
            "relationship" to relationship,
            "addedBy" to currentUserId,
            "addedByRole" to userRole
        )
        
        db.collection("users").document(userId)
            .collection("emergency_contacts")
            .add(contact)
            .addOnSuccessListener {
                Log.d("EmergencyViewModel", "Contact added successfully")
            }
            .addOnFailureListener { e ->
                Log.e("EmergencyViewModel", "Error adding contact", e)
            }
    }
    
    fun deleteEmergencyContact(contactId: String, userRole: String) {
        val userId = auth.currentUser?.uid ?: return
        
        // Only elders can delete contacts
        if (userRole != "ELDER") {
            Log.w("EmergencyViewModel", "Only elders can delete contacts")
            return
        }
        
        db.collection("users").document(userId)
            .collection("emergency_contacts")
            .document(contactId)
            .delete()
            .addOnSuccessListener {
                Log.d("EmergencyViewModel", "Contact deleted successfully")
            }
            .addOnFailureListener { e ->
                Log.e("EmergencyViewModel", "Error deleting contact", e)
            }
    }
}
