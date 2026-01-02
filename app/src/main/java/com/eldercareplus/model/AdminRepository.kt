package com.eldercareplus.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class PendingUser(
    val uid: String = "",
    val email: String = "",
    val role: String = "",
    val verificationStatus: String = "pending"
)

class AdminRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getPendingCaretakers(): Flow<List<PendingUser>> = callbackFlow {
        val listener = db.collection("users")
            .whereEqualTo("role", "caretaker")
            .whereEqualTo("verificationStatus", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.map { 
                    it.toObject(PendingUser::class.java)!!.copy(uid = it.id)
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    suspend fun approveUser(uid: String) {
        db.collection("users").document(uid)
            .update("verificationStatus", "approved")
            .await()
    }

    suspend fun rejectUser(uid: String) {
        db.collection("users").document(uid)
            .update("verificationStatus", "rejected")
            .await()
    }
}
