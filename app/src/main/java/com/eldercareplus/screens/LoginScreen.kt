package com.eldercareplus.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.eldercareplus.auth.AuthState
import com.eldercareplus.auth.AuthViewModel
import com.eldercareplus.navigation.Screen

@Composable
fun LoginScreen(navController: NavController) {

    val vm: AuthViewModel = viewModel()
    val state by vm.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            vm.reset()
            navController.navigate(Screen.Start.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Login", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { vm.login(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = { vm.forgotPassword(email) }) {
            Text("Forgot Password?")
        }

        TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
            Text("Create new account")
        }

        when (state) {
            is AuthState.Loading -> {
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator()
            }

            is AuthState.Error -> {
                Spacer(Modifier.height(12.dp))
                Text(
                    (state as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {}
        }
    }
}
