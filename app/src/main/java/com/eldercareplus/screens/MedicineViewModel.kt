package com.eldercareplus.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eldercareplus.model.Medicine
import com.eldercareplus.model.MedicineRepository
import com.eldercareplus.util.MedicineScheduler
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MedicineViewModel(context: Context) : ViewModel() {

    private val repository = MedicineRepository()
    private val scheduler = MedicineScheduler(context)
    private val auth = FirebaseAuth.getInstance()

    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines

    private var targetElderId: String? = null

    fun setTargetElderId(id: String) {
        targetElderId = id
        loadMedicines()
    }

    init {
        loadMedicines()
    }

    private fun loadMedicines() {
        val elderId = targetElderId ?: auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.getMedicines(elderId).collect {
                _medicines.value = it
            }
        }
    }

    fun addMedicine(name: String, dosage: String, timeInMillis: Long) {
        val elderId = targetElderId ?: auth.currentUser?.uid ?: return
        val medicine = Medicine(
            id = UUID.randomUUID().toString(),
            name = name,
            dosage = dosage,
            timeInMillis = timeInMillis,
            elderId = elderId
        )

        viewModelScope.launch {
            repository.addMedicine(medicine)
            scheduler.schedule(medicine)
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            scheduler.cancel(medicine)
            repository.deleteMedicine(medicine.id)
        }
    }
}

class MedicineViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicineViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
