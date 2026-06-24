package com.example.sportshub.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sportshub.ui.screens.home.DetailScreen
import com.example.sportshub.ui.screens.home.HomeViewModel
import com.example.sportshub.ui.screens.login.LoginScreen
import com.example.sportshub.ui.screens.login.LoginState
import com.example.sportshub.ui.screens.login.LoginViewModel
import com.example.sportshub.ui.screens.splash.SplashScreen
import com.example.sportshub.ui.screens.onboarding.OnboardingScreen
import com.example.sportshub.ui.screens.onboarding.OnboardingViewModel
import com.example.sportshub.ui.screens.container.MainContainerScreen
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(navController = navController)
        }

        composable("login") {
            val loginViewModel: LoginViewModel = koinViewModel()
            val loginState by loginViewModel.loginState.collectAsStateWithLifecycle()
            val context = LocalContext.current

            LaunchedEffect(loginState) {
                if (loginState is LoginState.Success) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val sharedPrefs = context.getSharedPreferences("sportshub_prefs", Context.MODE_PRIVATE)
                    val isCompleted = sharedPrefs.getBoolean("onboarding_completed_$uid", false)
                    val target = if (isCompleted) "main_container" else "onboarding"
                    navController.navigate(target) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }

            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToHome = {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val sharedPrefs = context.getSharedPreferences("sportshub_prefs", Context.MODE_PRIVATE)
                    val isCompleted = sharedPrefs.getBoolean("onboarding_completed_$uid", false)
                    val target = if (isCompleted) "main_container" else "onboarding"
                    navController.navigate(target) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding") {
            val onboardingViewModel: OnboardingViewModel = koinViewModel()
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onNavigateToHome = {
                    navController.navigate("main_container") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("main_container") {
            val homeViewModel: HomeViewModel = koinViewModel()

            MainContainerScreen(
                homeViewModel = homeViewModel,
                onMatchClick = { id ->
                    navController.navigate("detail/$id")
                },
                onNewsClick = { id ->
                    navController.navigate("news_detail/$id")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main_container") { inclusive = true }
                    }
                }
            )
        }

        composable(
            "detail/{matchId}",
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId")
            DetailScreen(
                matchId = matchId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            "news_detail/{newsId}",
            arguments = listOf(navArgument("newsId") { type = NavType.StringType })
        ) { backStackEntry ->
            val newsId = backStackEntry.arguments?.getString("newsId")
            com.example.sportshub.ui.screens.news.NewsDetailScreen(newsId = newsId, navController = navController)
        }
    }
}

