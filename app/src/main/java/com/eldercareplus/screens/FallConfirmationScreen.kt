package com.eldercareplus.screens

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eldercareplus.model.AlertManager
import com.eldercareplus.util.LocationHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FallConfirmationScreen(navController: NavController) {
    var timeLeft by remember { mutableIntStateOf(15) }
    var isAlertSent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val alertManager = remember { AlertManager() }
    val locationHelper = remember { LocationHelper(context) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0 && !isAlertSent) {
            delay(1000L)
            timeLeft--
        }
        if (timeLeft == 0 && !isAlertSent) {
            // Timer expired, send alert
            isAlertSent = true
            val currentUser = FirebaseAuth.getInstance().currentUser
            val elderId = currentUser?.uid ?: "unknown"
            val elderName = currentUser?.displayName ?: "Elder" // Assuming name is available or fetched

            val location = locationHelper.getCurrentLocation()
            alertManager.sendAlert(
                elderId = elderId,
                elderName = elderName,
                location = location,
                type = "FALL_DETECTED"
            )
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isAlertSent) Color.Red else Color.White)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isAlertSent) {
                Text(
                    "ALERT SENT!",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Caretakers have been notified with your location.",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Red)
                ) {
                    Text("Return to Dashboard")
                }
            } else {
                Text(
                    "FALL DETECTED",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Are you okay?",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "$timeLeft",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("I AM OKAY")
                }
            }
        }
    }
}
