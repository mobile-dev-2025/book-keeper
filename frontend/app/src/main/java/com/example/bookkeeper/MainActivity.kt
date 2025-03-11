package com.example.bookkeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bookkeeper.screens.HomeScreen
import com.example.bookkeeper.screens.LaunchScreen
import com.example.bookkeeper.screens.LoginScreen
import com.example.bookkeeper.screens.SignUpScreen
import com.example.bookkeeper.ui.theme.BookKeeperTheme
import com.example.bookkeeper.viewmodel.LocalAuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookKeeperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BookKeeperApp()
                }
            }
        }
    }
}

@Composable
fun BookKeeperApp() {
    val navController = rememberNavController()
    val authViewModel: LocalAuthViewModel = viewModel()

    // Check if user is already logged in and navigate accordingly
    LaunchedEffect(key1 = authViewModel.isLoggedIn()) {
        if (authViewModel.isLoggedIn()) {
            navController.navigate("home") {
                popUpTo("launch") { inclusive = true }
            }
        }
    }

    BookKeeperNavHost(navController, authViewModel)
}

@Composable
fun BookKeeperNavHost(
    navController: NavHostController,
    authViewModel: LocalAuthViewModel
) {
    NavHost(navController = navController, startDestination = "launch") {
        // Launch/Splash Screen
        composable("launch") {
            LaunchScreen(
                onGetStartedClick = {
                    navController.navigate("signup")
                }
            )
        }

        // Sign Up Screen
        composable("signup") {
            SignUpScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate("login")
                },
                onSignUpSuccess = {
                    navController.navigate("home") {
                        popUpTo("launch") { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("launch") { inclusive = true }
                    }
                }
            )
        }

        // Home Screen (shown after authentication)
        composable("home") {
            HomeScreen(
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("launch") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}