package com.eldercareplus.caretaker

import androidx.lifecycle.ViewModel
import com.eldercareplus.model.Elder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CaretakerViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    private val _elders = MutableStateFlow<List<Elder>>(emptyList())
    val elders: StateFlow<List<Elder>> = _elders

    private val _caretakerName = MutableStateFlow<String>("Caretaker")
    val caretakerName: StateFlow<String> = _caretakerName

    init {
        loadCaretakerName()
        loadElders()
    }

    private fun loadCaretakerName() {
        uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener {
                val name = it.getString("displayName") ?: "Caretaker"
                _caretakerName.value = name
            }
    }

    private fun loadElders() {
        uid ?: return

        db.collection("users")
            .document(uid)
            .collection("elders")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _elders.value = snapshot.documents.map {
                        Elder(
                            id = it.id,
                            name = it.getString("name") ?: "",
                            age = it.getString("age") ?: "",
                            phone = it.getString("phone") ?: "",
                            condition = it.getString("condition") ?: ""
                        )
                    }
                }
            }
    }

    fun addElder(elder: Elder) {
        uid ?: return

        db.collection("users")
            .document(uid)
            .collection("elders")
            .add(
                mapOf(
                    "name" to elder.name,
                    "age" to elder.age,
                    "phone" to elder.phone,
                    "condition" to elder.condition,
                    "createdAt" to System.currentTimeMillis()
                )
            )
    }

    fun deleteElder(elderId: String) {
        uid ?: return

        db.collection("users")
            .document(uid)
            .collection("elders")
            .document(elderId)
            .delete()
    }
    fun updateElder(elder: Elder) {
        uid ?: return

        db.collection("users")
            .document(uid)
            .collection("elders")
            .document(elder.id)
            .update(
                mapOf(
                    "name" to elder.name,
                    "age" to elder.age,
                    "phone" to elder.phone,
                    "condition" to elder.condition
                )
            )
    }

}
