package com.eldercareplus.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eldercareplus.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RoleSelectionScreen(navController: NavController) {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        // 🔴 LOGOUT BUTTON (TOP RIGHT)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    auth.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            ) {
                Text("Logout")
            }
        }

        Spacer(Modifier.height(40.dp))

        // CENTER CONTENT
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Select Role", style = MaterialTheme.typography.headlineSmall)

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    uid?.let {
                        val updates = mapOf(
                            "role" to "caretaker",
                            // Auto-approve per user request
                            "verificationStatus" to "approved"
                        )
                        db.collection("users").document(it)
                            .update(updates)
                            .addOnSuccessListener {
                                navController.navigate(Screen.Caretaker.route) {
                                    popUpTo(Screen.Role.route) { inclusive = true }
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Caretaker")
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    uid?.let {
                        val updates = mapOf(
                            "role" to "elder",
                            "verificationStatus" to "approved"
                        )
                        db.collection("users").document(it)
                            .update(updates)
                            .addOnSuccessListener {
                                navController.navigate(Screen.Elder.route) {
                                    popUpTo(Screen.Role.route) { inclusive = true }
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Elder")
            }
        }
    }
}

