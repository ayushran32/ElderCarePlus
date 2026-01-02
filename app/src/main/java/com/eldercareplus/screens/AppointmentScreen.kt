package com.eldercareplus.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.eldercareplus.model.Appointment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AppointmentScreen(navController: NavController, elderId: String? = null) {
    val context = LocalContext.current
    val vm: AppointmentViewModel = viewModel(factory = AppointmentViewModelFactory(context))
    
    LaunchedEffect(Unit) {
        // Load data after UI is rendered
        vm.loadAppointments()
    }
    
    LaunchedEffect(elderId) {
        if (elderId != null) {
            vm.setTargetElderId(elderId)
        }
    }
    
    val appointments by vm.appointments.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Book Appointment")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Doctor Appointments", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            if (appointments.isEmpty()) {
                Text("No upcoming appointments.")
            } else {
                LazyColumn {
                    items(appointments) { appointment ->
                        AppointmentItem(appointment, onDelete = { vm.deleteAppointment(appointment) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAppointmentDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { doctor, hospital, time, notify ->
                vm.addAppointment(doctor, hospital, time, notify)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AppointmentItem(appointment: Appointment, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("EEE, MMM d, hh:mm a", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Dr. ${appointment.doctorName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(appointment.hospitalName, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Text(dateFormat.format(appointment.timestamp), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddAppointmentDialog(onDismiss: () -> Unit, onAdd: (String, String, Long, Boolean) -> Unit) {
    var doctorName by remember { mutableStateOf("") }
    var hospitalName by remember { mutableStateOf("") }
    var timestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var notifyCaretaker by remember { mutableStateOf(true) }
    
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("EEE, MMM d, hh:mm a", Locale.getDefault())

    val calendar = Calendar.getInstance()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Book Appointment") },
        text = {
            Column {
                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text("Doctor's Name") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = hospitalName,
                    onValueChange = { hospitalName = it },
                    label = { Text("Hospital / Clinic") }
                )
                Spacer(Modifier.height(16.dp))
                
                Button(onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            calendar.set(Calendar.YEAR, year)
                            calendar.set(Calendar.MONTH, month)
                            calendar.set(Calendar.DAY_OF_MONTH, day)
                            
                            TimePickerDialog(
                                context, 
                                { _, hour, minute ->
                                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                                    calendar.set(Calendar.MINUTE, minute)
                                    timestamp = calendar.timeInMillis
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text("Date & Time")
                }
                Text(dateFormat.format(timestamp), style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = notifyCaretaker, onCheckedChange = { notifyCaretaker = it })
                    Text("Notify Caretaker Now")
                }
                if (notifyCaretaker) {
                    Text("Also sends alert to linked caretakers.", style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (doctorName.isNotBlank() && hospitalName.isNotBlank()) {
                    onAdd(doctorName, hospitalName, timestamp, notifyCaretaker)
                }
            }) {
                Text("Book")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
