package com.eldercareplus.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.eldercareplus.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun StartScreen(navController: NavController) {

    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Start.route) { inclusive = true }
            }
            return@LaunchedEffect
        }

        val uid = user.uid
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role")

                when (role) {
                    "caretaker" -> {
                        navController.navigate(Screen.Caretaker.route) {
                            popUpTo(Screen.Start.route) { inclusive = true }
                        }
                    }
                    "elder" -> {
                        navController.navigate(Screen.Elder.route) {
                            popUpTo(Screen.Start.route) { inclusive = true }
                        }
                    }
                    else -> {
                        navController.navigate(Screen.Role.route) {
                            popUpTo(Screen.Start.route) { inclusive = true }
                        }
                    }
                }
            }
            .addOnFailureListener {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Start.route) { inclusive = true }
                }
            }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
