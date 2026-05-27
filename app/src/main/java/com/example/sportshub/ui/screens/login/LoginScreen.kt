package com.example.sportshub.ui.screens.login

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
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
        when (loginState) {
            is LoginState.Success -> {
                onNavigateToHome()
            }

            is LoginState.Error -> {
                val message = (loginState as LoginState.Error).message
                Toast.makeText(context, "Error al iniciar sesión: $message", Toast.LENGTH_LONG).show()
            }

            else -> Unit
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)

                val token = account.idToken
                if (token != null) {
                    viewModel.signInWithGoogle(token)
                } else {
                    Log.e("LoginScreen", "Google Sign-In no devolvió idToken")
                    Toast.makeText(
                        context,
                        "No se pudo obtener el token de Google.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: ApiException) {
                Log.e("LoginScreen", "Falló el inicio de sesión con Google", e)
                Toast.makeText(
                    context,
                    "Falló el inicio de sesión con Google. Revisá la configuración de Firebase/SHA-1.",
                    Toast.LENGTH_LONG
                ).show()
            }

        } else {
            Log.e("LoginScreen", "Login cancelado o fallido. Código: ${result.resultCode}")
            Toast.makeText(
                context,
                "Inicio de sesión cancelado.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.38f))

        Text(
            text = "SportsHUB",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.weight(0.15f))

        Text(
            text = "Inicia sesión para continuar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            onClick = {
                Log.d("LoginScreen", "Intento de login iniciado...")

                try {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("63181768800-akk1cel50ae6eio8baqopsg2mots4mgp.apps.googleusercontent.com")
                        .requestEmail()
                        .build()

                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    launcher.launch(googleSignInClient.signInIntent)

                } catch (e: Exception) {
                    Log.e("LoginScreen", "Error al iniciar Google Sign-In Client", e)
                    Toast.makeText(
                        context,
                        "Error al iniciar Google Sign-In.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            enabled = loginState !is LoginState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            shadowElevation = 1.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (loginState is LoginState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        coil.compose.AsyncImage(
                            model = "https://fonts.gstatic.com/s/i/productlogos/googleg/v6/web-24dp/logo_googleg_color_2x_web_24dp.png",
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Continuar con Google",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.47f))
    }
}