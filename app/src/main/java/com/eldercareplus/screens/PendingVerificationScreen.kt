package com.eldercareplus.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eldercareplus.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PendingVerificationScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var statusMessage by remember { mutableStateOf("Checking status...") }
    var isChecking by remember { mutableStateOf(false) }

    fun checkStatus() {
        val uid = auth.currentUser?.uid ?: return
        isChecking = true
        statusMessage = "Checking..."
        
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                isChecking = false
                val status = document.getString("verificationStatus") ?: "pending"
                if (status == "approved") {
                    navController.navigate(Screen.Caretaker.route) {
                        popUpTo(Screen.PendingVerification.route) { inclusive = true }
                    }
                } else if (status == "rejected") {
                    statusMessage = "Your request was rejected. Contact admin."
                } else {
                    statusMessage = "Still pending approval."
                }
            }
            .addOnFailureListener {
                isChecking = false
                statusMessage = "Error checking status."
            }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Verification Pending",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Your account is waiting for Admin approval. You cannot access the dashboard until approved.",
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            
            Text(statusMessage, color = MaterialTheme.colorScheme.primary)
            
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { checkStatus() },
                enabled = !isChecking
            ) {
                Text("Check Status")
            }

            Spacer(Modifier.height(16.dp))
            
            TextButton(onClick = {
                auth.signOut()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0)
                }
            }) {
                Text("Logout")
            }
        }
    }
}
