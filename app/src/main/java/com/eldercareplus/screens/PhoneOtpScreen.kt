package com.eldercareplus.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.eldercareplus.auth.PhoneAuthState
import com.eldercareplus.auth.PhoneAuthViewModel
import com.eldercareplus.navigation.Screen

@Composable
fun PhoneOtpScreen(navController: NavController) {

    val vm: PhoneAuthViewModel = viewModel()
    val state by vm.state.collectAsState()
    val activity = LocalContext.current as Activity

    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is PhoneAuthState.Verified) {
            navController.navigate(Screen.Role.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Phone OTP Login", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))

        if (state is PhoneAuthState.CodeSent) {

            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it },
                label = { Text("Enter OTP") }
            )

            Spacer(Modifier.height(8.dp))

            Button(onClick = { vm.verifyOtp(otp) }) {
                Text("Verify OTP")
            }

        } else {

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number (+91XXXXXXXXXX)") }
            )

            Spacer(Modifier.height(8.dp))

            Button(onClick = {
                vm.sendOtp(phone, activity)
            }) {
                Text("Send OTP")
            }
        }

        when (state) {
            is PhoneAuthState.Loading -> CircularProgressIndicator()
            is PhoneAuthState.Error -> Text(
                (state as PhoneAuthState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
            else -> {}
        }
    }
}
