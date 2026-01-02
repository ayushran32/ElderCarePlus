package com.eldercareplus.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MedicineRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("medicines")

    suspend fun addMedicine(medicine: Medicine) {
        collection.document(medicine.id).set(medicine).await()
    }

    suspend fun deleteMedicine(medicineId: String) {
        collection.document(medicineId).delete().await()
    }

    fun getMedicines(elderId: String): Flow<List<Medicine>> = callbackFlow {
        val listener = collection.whereEqualTo("elderId", elderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val medicines = snapshot?.toObjects(Medicine::class.java) ?: emptyList()
                trySend(medicines)
            }
        awaitClose { listener.remove() }
    }
}
