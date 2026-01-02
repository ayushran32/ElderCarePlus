package com.eldercareplus.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // REGISTER (Caretaker)
    fun register(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email & password required")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user ?: return@addOnSuccessListener

                // Save user in Firestore
                val data = mapOf(
                    "email" to email,
                    "createdAt" to System.currentTimeMillis()
                )
                db.collection("users").document(user.uid).set(data)

                user.sendEmailVerification()
                _authState.value = AuthState.Success
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Registration failed")
            }
    }

    // LOGIN (Caretaker)
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email & password required")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _authState.value = AuthState.Success
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Login failed")
            }
    }

    // FORGOT PASSWORD
    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Enter registered email")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _authState.value =
                    AuthState.Error("Password reset link sent to email")
            }
            .addOnFailureListener {
                _authState.value =
                    AuthState.Error(it.message ?: "Failed to send reset link")
            }
    }

    // LOGOUT
    fun logout() {
        auth.signOut()
    }

    fun reset() {
        _authState.value = AuthState.Idle
    }
}
