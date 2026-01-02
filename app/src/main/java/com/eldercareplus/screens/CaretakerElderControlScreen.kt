package com.eldercareplus.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun CaretakerElderControlScreen(navController: NavController, elderId: String) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Manage Elder", style = MaterialTheme.typography.headlineMedium)
            Text("ID: $elderId", style = MaterialTheme.typography.bodySmall)
            
            Spacer(Modifier.height(24.dp))

            ControlCard(title = "Manage Medicines", subtitle = "View & Add Reminders") {
                navController.navigate("medicine/$elderId") // Parameterized route
            }

            Spacer(Modifier.height(16.dp))

            ControlCard(title = "Doctor Appointments", subtitle = "Book & Track Visits") {
                navController.navigate("appointment/$elderId")
            }

            Spacer(Modifier.height(16.dp))

            ControlCard(title = "Sleep Stats", subtitle = "View Sleep Patterns") {
                navController.navigate("sleep/$elderId")
            }
        }
    }
}

@Composable
fun ControlCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
