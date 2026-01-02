package com.eldercareplus.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.eldercareplus.model.SleepSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SleepScreen(navController: NavController, elderId: String? = null) {
    val context = LocalContext.current
    val vm: SleepViewModel = viewModel(factory = SleepViewModelFactory(context))

    LaunchedEffect(Unit) {
        // Load data after UI is rendered
        vm.loadData()
    }
    
    LaunchedEffect(elderId) {
        if (elderId != null) {
            vm.setTargetElderId(elderId)
        }
    }

    val sessions by vm.sleepSessions.collectAsState()
    val lastNight by vm.lastNightSleep.collectAsState()
    val isTracking by vm.isTracking.collectAsState()

    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Sleep Tracking", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        // ===== TRACKING TOGGLE =====
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Automatic Tracking", style = MaterialTheme.typography.titleMedium)
                    Text(if (isTracking) "Active" else "Paused", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = isTracking,
                    onCheckedChange = { vm.toggleTracking(it) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ===== LAST NIGHT SUMMARY =====
        Text("Last Night", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        
        if (lastNight != null) {
            Card(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${lastNight!!.durationMinutes / 60}h ${lastNight!!.durationMinutes % 60}m",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${timeFormat.format(lastNight!!.startTime)} - ${timeFormat.format(lastNight!!.endTime)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
               Text("No sleep data for last night.", Modifier.padding(16.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        // ===== HISTORY =====
        Text("History (Last 7 Days)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyColumn {
            items(sessions) { session ->
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
                        Text(dateFormat.format(Date(session.date)), fontWeight = FontWeight.Bold)
                        Text("${session.durationMinutes / 60}h ${session.durationMinutes % 60}m")
                    }
                }
            }
        }
    }
}
