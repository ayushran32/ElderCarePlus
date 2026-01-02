package com.eldercareplus.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eldercareplus.model.AlertManager
import com.eldercareplus.model.Appointment
import com.eldercareplus.model.AppointmentRepository
import com.eldercareplus.util.AppointmentScheduler
import com.eldercareplus.util.LocationHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AppointmentViewModel(context: Context) : ViewModel() {

    private val repository = AppointmentRepository()
    private val scheduler = AppointmentScheduler(context)
    private val alertManager = AlertManager()
    private val locationHelper = LocationHelper(context)
    private val auth = FirebaseAuth.getInstance()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    private var targetElderId: String? = null

    fun setTargetElderId(id: String) {
        targetElderId = id
        loadAppointments()
    }

    init {
        // Don't load data in init to avoid blocking UI thread
        // Data will be loaded when screen is shown via LaunchedEffect
    }

    fun loadAppointments() {
        val elderId = targetElderId ?: auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.getAppointments(elderId).collect {
                _appointments.value = it
            }
        }
    }

    fun addAppointment(doctorName: String, hospitalName: String, timestamp: Long, notifyCaretaker: Boolean) {
        val elderId = targetElderId ?: auth.currentUser?.uid ?: return
        val appointment = Appointment(
            id = UUID.randomUUID().toString(),
            elderId = elderId,
            doctorName = doctorName,
            hospitalName = hospitalName,
            timestamp = timestamp,
            notifyCaretaker = notifyCaretaker
        )

        viewModelScope.launch {
            repository.addAppointment(appointment)
            scheduler.schedule(appointment)

            if (notifyCaretaker) {
                try {
                     val location = locationHelper.getCurrentLocation()
                     alertManager.sendAlert(
                         elderId = elderId, 
                         elderName = "Elder", // Default name for now
                         location = location,
                         type = "APPOINTMENT_BOOKED: Dr. $doctorName"
                     )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            scheduler.cancel(appointment)
            repository.deleteAppointment(appointment.id)
        }
    }
}

class AppointmentViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppointmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppointmentViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
