package com.example.bookkeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bookkeeper.components.AppDrawer
import com.example.bookkeeper.components.BottomNavBar
import com.example.bookkeeper.screens.*
import com.example.bookkeeper.ui.theme.BookKeeperTheme
import com.example.bookkeeper.viewmodel.Auth0ViewModel
import com.example.bookkeeper.viewmodel.BookViewModel
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookKeeperApp() {
    val navController = rememberNavController()
    val authViewModel: Auth0ViewModel = viewModel()
    val bookViewModel: BookViewModel = viewModel()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Track whether the drawer should be enabled
    var isDrawerEnabled by remember { mutableStateOf(false) }

    // Initialize Auth0 client
    LaunchedEffect(Unit) {
        authViewModel.initialize(navController.context)
    }

    // Check if user is already logged in and navigate accordingly
    LaunchedEffect(Unit) {
        if (authViewModel.isLoggedIn()) {
            navController.navigate("home") {
                popUpTo("launch") { inclusive = true }
            }
        }
    }

    // Update drawer enabled status based on current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    isDrawerEnabled = currentRoute in listOf("home", "add_book", "books", "history", "profile", "settings", "about")

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (isDrawerEnabled) {
                ModalDrawerSheet {
                    AppDrawer(
                        navController = navController,
                        authViewModel = authViewModel,
                        onLogout = {
                            authViewModel.logout(navController.context)
                            navController.navigate("launch") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onCloseDrawer = {
                            scope.launch {
                                drawerState.close()
                            }
                        }
                    )
                }
            }
        },
        gesturesEnabled = isDrawerEnabled
    ) {
        Scaffold(
            bottomBar = {
                // Only show bottom navigation bar on main screens after authentication
                if (isDrawerEnabled) {
                    BottomNavBar(navController)
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "launch",
                modifier = Modifier.padding(paddingValues)
            ) {
                // Authentication flow screens
                composable("launch") {
                    LaunchScreen(
                        onGetStartedClick = {
                            navController.navigate("signup")
                        }
                    )
                }

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

                // Main app screens (with navigation)
                composable("home") {
                    HomeScreen(
                        authViewModel = authViewModel,
                        bookViewModel = bookViewModel, // Added bookViewModel parameter
                        onLogout = {
                            authViewModel.logout(navController.context)
                            navController.navigate("launch") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onMenuClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }

                // Books list screen
                composable("books") {
                    BooksListScreen(
                        bookViewModel = bookViewModel,
                        onMenuClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        onAddBookClick = {
                            navController.navigate("add_book")
                        }
                    )
                }

                // Add book screen
                composable("add_book") {
                    AddBookScreen(
                        bookViewModel = bookViewModel,
                        onMenuClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                // History screen
                composable("history") {
                    BooksListScreen(  // Changed from PlaceholderScreen to BooksListScreen
                        bookViewModel = bookViewModel,
                        onMenuClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        onAddBookClick = {
                            navController.navigate("add_book")
                        }
                    )
                }

                // Placeholder screens for other sections
                composable("profile") {
                    PlaceholderScreen(
                        screenName = "Profile",
                        onMenuClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }

                composable("settings") {
                    PlaceholderScreen(
                        screenName = "Settings",
                        onMenuClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }

                composable("about") {
                    PlaceholderScreen(
                        screenName = "About Book Keeper",
                        onMenuClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }
            }
        }
    }
}

// Temporary placeholder screen with hamburger menu functionality
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(screenName: String, onMenuClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenName) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Text(
                text = screenName,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}