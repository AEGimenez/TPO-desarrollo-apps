package com.example.sportshub.ui.screens.login

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current

    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onNavigateToHome()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    viewModel.signInWithGoogle(token)
                }
            } catch (e: ApiException) {
                Log.e("LoginScreen", "Fallo el inicio de sesión con Google (ApiException)", e)
                android.widget.Toast.makeText(context, "Modo Desarrollo: Falló Google Sign-In (SHA-1). Abriendo Home igual...", android.widget.Toast.LENGTH_LONG).show()
                onNavigateToHome()
            }
        } else {
            Log.e("LoginScreen", "El inicio de sesión con Google no dio RESULT_OK. Código: ${result.resultCode}")
            android.widget.Toast.makeText(context, "Modo Desarrollo: Login omitido/cancelado. Abriendo Home...", android.widget.Toast.LENGTH_LONG).show()
            onNavigateToHome()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SportsHUB",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¡Bienvenido!",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Inicia sesión para continuar",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = {
                Log.d("LoginScreen", "Intento de login iniciado...") // DEBUG
                try {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("63181768800-akk1cel50ae6eio8baqopsg2mots4mgp.apps.googleusercontent.com")
                        .requestEmail()
                        .build()

                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    val intent = googleSignInClient.signInIntent
                    Log.d("LoginScreen", "Lanzando intent...")
                    launcher.launch(intent)
                } catch (e: Exception) {
                    Log.e("LoginScreen", "Error al iniciar Google Sign-In Client", e)
                    android.widget.Toast.makeText(context, "Modo Desarrollo: Error al iniciar cliente. Abriendo Home...", android.widget.Toast.LENGTH_LONG).show()
                    onNavigateToHome()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (loginState is LoginState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(text = "Continuar con Google")
            }
        }
    }
}