package com.example.sportshub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sportshub.ui.screens.login.LoginScreen
import com.example.sportshub.ui.screens.login.LoginState
import com.example.sportshub.ui.screens.login.LoginViewModel
import com.example.sportshub.ui.screens.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(navController = navController)
        }

        composable("login") {
            val loginViewModel: LoginViewModel = viewModel()
            val loginState by loginViewModel.loginState.collectAsState()

            // Si el estado cambia a Success, navegamos al Home
            LaunchedEffect(loginState) {
                if (loginState is LoginState.Success) {
                    navController.navigate("home") {
                        // Borramos el login del historial para que el usuario no pueda volver atrás
                        popUpTo("login") { inclusive = true }
                    }
                }
            }

            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            androidx.compose.material3.Text(text = "¡Bienvenido al Home!")
        }
    }
}

