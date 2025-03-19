package com.example.bookkeeper.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.bookkeeper.viewmodel.Auth0ViewModel
import com.example.bookkeeper.viewmodel.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: Auth0ViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val loginState by authViewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(loginState) {
        when (loginState) {
            is AuthState.Success -> onLoginSuccess()
            is AuthState.Error -> {
                snackbarHostState.showSnackbar((loginState as AuthState.Error).message)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Book Keeper",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Icon
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(8.dp, shape = CircleShape)
                        .clip(CircleShape),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Welcome text
                Text(
                    text = "Welcome Back!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Track your reading journey\nand never lose a book again",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 40.dp)
                )

                // Email/Password Login Button
                ElevatedButton(
                    onClick = { authViewModel.login(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = loginState !is AuthState.Loading
                ) {
                    if (loginState is AuthState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Sign in with Email",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Divider with "or" text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Text(
                        text = "  OR  ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Login Button
                OutlinedButton(
                    onClick = {
                        // Use direct Google login with account selection
                        authViewModel.loginWithGoogle(context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    shape = MaterialTheme.shapes.large,
                    enabled = loginState !is AuthState.Loading
                ) {
                    if (loginState is AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // You can add a Google logo here if needed
                            Text(
                                text = "Continue with Google",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Secure authentication powered by Auth0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }

            // Terms Section
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .width(120.dp)
                )

                val annotatedText = buildAnnotatedString {
                    append("By continuing, you agree to our ")
                    pushStringAnnotation("terms", "https://bookkeeperonline.netlify.app")
                    withStyle(style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )) {
                        append("Terms of Use")
                    }
                    pop()
                    append(" and ")
                    pushStringAnnotation("privacy", "https://bookkeeperonline.netlify.app")
                    withStyle(style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )) {
                        append("Privacy Policy")
                    }
                }

                ClickableText(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        textAlign = TextAlign.Center
                    ),
                    onClick = { offset ->
                        annotatedText.getStringAnnotations(offset, offset)
                            .firstOrNull()?.let { annotation ->
                                when (annotation.tag) {
                                    "terms", "privacy" -> {
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(annotation.item)
                                            )
                                        )
                                    }
                                }
                            }
                    }
                )
            }
        }
    }
}