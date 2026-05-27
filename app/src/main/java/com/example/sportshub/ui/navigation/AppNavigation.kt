package com.example.sportshub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sportshub.data.local.SportsDatabase
import com.example.sportshub.data.remote.RetrofitClient
import com.example.sportshub.data.repository.MatchRepository
import com.example.sportshub.ui.screens.home.DetailScreen
import com.example.sportshub.ui.screens.home.HomeScreen
import com.example.sportshub.ui.screens.home.HomeViewModel
import com.example.sportshub.ui.screens.login.LoginScreen
import com.example.sportshub.ui.screens.login.LoginState
import com.example.sportshub.ui.screens.login.LoginViewModel
import com.example.sportshub.ui.screens.splash.SplashScreen
import androidx.navigation.navArgument
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
            LaunchedEffect(loginState) {
                if (loginState is LoginState.Success) {
                    navController.navigate("home") {
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
            val context = LocalContext.current
            val db = SportsDatabase.getDatabase(context)
            val api = RetrofitClient.api
            val viewModel = HomeViewModel(MatchRepository(api, db.sportsDao()))

            HomeScreen(
                viewModel = viewModel,
                onMatchClick = { id ->
                    navController.navigate("detail/$id")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }


        composable(
            "detail/{matchId}",
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val db = SportsDatabase.getDatabase(context)
            val matchId = backStackEntry.arguments?.getString("matchId")
            DetailScreen(matchId = matchId, dao = db.sportsDao())
        }
    }
}

