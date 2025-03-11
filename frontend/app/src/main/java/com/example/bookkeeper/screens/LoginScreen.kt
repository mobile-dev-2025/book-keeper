package com.example.bookkeeper.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookkeeper.viewmodel.Auth0ViewModel
import com.example.bookkeeper.viewmodel.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: Auth0ViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current

    // Collect login state from the ViewModel
    val loginState by authViewModel.loginState.collectAsState()

    // SnackBar for showing error messages
    val snackbarHostState = remember { SnackbarHostState() }

    // Check for login success or failure
    LaunchedEffect(loginState) {
        when (loginState) {
            is AuthState.Success -> onLoginSuccess()
            is AuthState.Error -> {
                val message = (loginState as AuthState.Error).message
                snackbarHostState.showSnackbar(message)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Logo/Icon
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Login header
                Text(
                    text = "Welcome to Book Keeper",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Track your reading journey",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Login with Auth0 button
                Button(
                    onClick = {
                        authViewModel.login(context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    enabled = loginState !is AuthState.Loading
                ) {
                    if (loginState is AuthState.Loading) {
                        // Show loading indicator when logging in
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Login with Auth0")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "You'll be redirected to Auth0 to securely sign in",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            // Terms and privacy policy text at the bottom with clickable links
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "By signing up or signing in, you agree to our ",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Terms of Use clickable text
                    Text(
                        text = "Terms of Use",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bookkeeperonline.netlify.app"))
                                context.startActivity(intent)
                            }
                        }
                    )
                    Text(
                        text = " and ",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    // Privacy Policy clickable text
                    Text(
                        text = "Privacy Policy",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bookkeeperonline.netlify.app"))
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }
}