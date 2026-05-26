package com.example.sportshub.ui.screens.login

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

//estados de nuestra pantalla
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    // autenticacion
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                // auth de credenciales google
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user

                if (user != null) {
                    val userDocRef = db.collection("users").document(user.uid)
                    val document = userDocRef.get().await()

                    if (!document.exists()) {
                        val newUserMap = hashMapOf(
                            "uid" to user.uid,
                            "displayName" to (user.displayName ?: "Usuario"),
                            "email" to (user.email ?: ""),
                            "photoUrl" to (user.photoUrl?.toString() ?: ""),
                            "createdAt" to com.google.firebase.Timestamp.now(),
                            "onboardingComplete" to false
                        )
                        userDocRef.set(newUserMap).await()
                    }

                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error("Error: Usuario nulo")
                }

            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }
}
