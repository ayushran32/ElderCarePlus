package com.eldercareplus.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AppointmentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("appointments")

    suspend fun addAppointment(appointment: Appointment) {
        collection.document(appointment.id).set(appointment).await()
    }

    suspend fun deleteAppointment(appointmentId: String) {
        collection.document(appointmentId).delete().await()
    }

    fun getAppointments(elderId: String): Flow<List<Appointment>> = callbackFlow {
        val listener = collection.whereEqualTo("elderId", elderId)
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val appointments = snapshot?.toObjects(Appointment::class.java) ?: emptyList()
                trySend(appointments)
            }
        awaitClose { listener.remove() }
    }
}
