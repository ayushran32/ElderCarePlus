package com.eldercareplus.screens

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
import com.eldercareplus.model.Medicine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun MedicineScreen(navController: NavController, elderId: String? = null) {
    val context = LocalContext.current
    val vm: MedicineViewModel = viewModel(factory = MedicineViewModelFactory(context))
    
    LaunchedEffect(elderId) {
        if (elderId != null) {
            vm.setTargetElderId(elderId)
        }
    }
    
    val medicines by vm.medicines.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Medicine")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("My Medicines", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            if (medicines.isEmpty()) {
                Text("No medicines added yet.")
            } else {
                LazyColumn {
                    items(medicines) { medicine ->
                        MedicineItem(medicine, onDelete = { vm.deleteMedicine(medicine) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMedicineDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, dosage, time ->
                vm.addMedicine(name, dosage, time)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MedicineItem(medicine: Medicine, onDelete: () -> Unit) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(medicine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(medicine.dosage, style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(timeFormat.format(medicine.timeInMillis), style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddMedicineDialog(onDismiss: () -> Unit, onAdd: (String, String, Long) -> Unit) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var timeInMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medicine") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medicine Name") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage (e.g., 1 tablet)") }
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    val cal = Calendar.getInstance()
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            cal.set(Calendar.HOUR_OF_DAY, hour)
                            cal.set(Calendar.MINUTE, minute)
                            timeInMillis = cal.timeInMillis
                        },
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),
                        false
                    ).show()
                }) {
                    Text("Time: ${timeFormat.format(timeInMillis)}")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onAdd(name, dosage, timeInMillis)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
