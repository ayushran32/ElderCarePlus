package com.eldercareplus.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eldercareplus.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileSetupScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Complete Your Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Please provide your details to continue.", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Mobile Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank() && uid != null) {
                        isLoading = true
                        val userUpdates = mapOf(
                            "displayName" to name,
                            "phoneNumber" to phone,
                            "address" to address,
                            "isProfileComplete" to true
                        )
                        
                        db.collection("users").document(uid)
                            .update(userUpdates)
                            .addOnSuccessListener {
                                isLoading = false
                                navController.navigate(Screen.Role.route) {
                                    popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                                }
                            }
                            .addOnFailureListener {
                                isLoading = false
                                // Handle error (show snackbar ideally)
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) {
                Text("Save & Continue")
            }
        }
    }
}
