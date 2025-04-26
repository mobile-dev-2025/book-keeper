package com.example.bookkeeper.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookkeeper.viewmodel.Auth0ViewModel
import com.example.bookkeeper.viewmodel.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    authViewModel: Auth0ViewModel,
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    val context = LocalContext.current
    val signUpState by authViewModel.signUpState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(signUpState) {
        when (signUpState) {
            is AuthState.Success -> onSignUpSuccess()
            is AuthState.Error -> {
                val message = (signUpState as AuthState.Error).message
                snackbarHostState.showSnackbar(message)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sign Up") }) },
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
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                    contentDescription = "Book Keeper Logo",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Join Book Keeper",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Sign up to start your reading journey",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Button(
                    onClick = { authViewModel.signUp(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    enabled = signUpState !is AuthState.Loading
                ) {
                    if (signUpState is AuthState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Create Account with Auth0")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Already have an account? Login")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "You'll be redirected to Auth0 for secure registration",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}