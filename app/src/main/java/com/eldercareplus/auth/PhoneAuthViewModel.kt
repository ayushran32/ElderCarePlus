package com.eldercareplus.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

sealed class PhoneAuthState {
    object Idle : PhoneAuthState()
    object CodeSent : PhoneAuthState()
    object Loading : PhoneAuthState()
    object Verified : PhoneAuthState()
    data class Error(val message: String) : PhoneAuthState()
}

class PhoneAuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var verificationId: String

    private val _state = MutableStateFlow<PhoneAuthState>(PhoneAuthState.Idle)
    val state: StateFlow<PhoneAuthState> = _state

    fun sendOtp(
        phoneNumber: String,
        activity: Activity
    ) {
        _state.value = PhoneAuthState.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _state.value = PhoneAuthState.Error(e.localizedMessage ?: "Verification failed")
            }

            override fun onCodeSent(
                verifId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = verifId
                _state.value = PhoneAuthState.CodeSent
            }
        }

        PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
            .let { PhoneAuthProvider.verifyPhoneNumber(it) }
    }

    fun verifyOtp(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                _state.value = PhoneAuthState.Verified
            }
            .addOnFailureListener {
                _state.value = PhoneAuthState.Error("Invalid OTP")
            }
    }
}
