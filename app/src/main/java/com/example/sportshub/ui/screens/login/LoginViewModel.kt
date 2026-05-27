package com.example.sportshub.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user
                if (user != null) {
                    try {
                        val userDocRef = db.collection("users").document(user.uid)
                        if (!userDocRef.get().await().exists()) {
                            userDocRef.set(hashMapOf("email" to user.email)).await()
                        }
                    } catch (firestoreException: Exception) {
                        Log.w("LoginViewModel", "No se pudo sincronizar el usuario en Firestore (offline): ${firestoreException.message}")
                    }
                    _loginState.value = LoginState.Success
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error: ${e.message}")
                _loginState.value = LoginState.Error(e.localizedMessage ?: "Error")
            }
        }
    }
}