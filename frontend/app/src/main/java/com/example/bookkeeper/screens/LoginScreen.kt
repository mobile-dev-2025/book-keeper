package com.example.bookkeeper.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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

    // Animation visibility flags
    var logoVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    var buttonsVisible by remember { mutableStateOf(false) }

    // Trigger animations in sequence
    LaunchedEffect(Unit) {
        logoVisible = true
        kotlinx.coroutines.delay(300)
        contentVisible = true
        kotlinx.coroutines.delay(200)
        buttonsVisible = true
    }

    LaunchedEffect(loginState) {
        when (loginState) {
            is AuthState.Success -> onLoginSuccess()
            is AuthState.Error -> {
                snackbarHostState.showSnackbar((loginState as AuthState.Error).message)
            }
            else -> {}
        }
    }

    // Modern gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(48.dp))

                    // Animated logo - using AsyncImage to load the remote logo
                    AnimatedVisibility(
                        visible = logoVisible,
                        enter = fadeIn(animationSpec = tween(1000)) +
                                slideInVertically(animationSpec = tween(1000)) { it / 2 }
                    ) {
                        AsyncImage(
                            model = "https://raw.githubusercontent.com/mobile-dev-2025/book-keeper/refs/heads/main/assets/Bookkeeper.png",
                            contentDescription = "BookKeeper Logo",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RoundedCornerShape(24.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Animated welcome text
                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = fadeIn(animationSpec = tween(800)) +
                                slideInVertically(animationSpec = tween(800)) { it / 2 }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Welcome to BookKeeper",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Track your reading journey with our intelligent reading companion",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Animated buttons
                    AnimatedVisibility(
                        visible = buttonsVisible,
                        enter = fadeIn(animationSpec = tween(600)) +
                                slideInVertically(animationSpec = tween(800)) { it / 2 }
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Email/Password Login Button
                            Button(
                                onClick = { authViewModel.login(context) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                enabled = loginState !is AuthState.Loading
                            ) {
                                if (loginState is AuthState.Loading) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Email,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Sign in with Email",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Google Login Button with modern design
                            OutlinedButton(
                                onClick = { authViewModel.loginWithGoogle(context) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onBackground
                                ),
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
                                        // Google logo - using PNG instead of SVG for better compatibility
                                        AsyncImage(
                                            model = "https://www.google.com/images/branding/googleg/1x/googleg_standard_color_128dp.png",
                                            contentDescription = "Google logo",
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Continue with Google",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Privacy notes with 2025-style transparency
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val annotatedText = buildAnnotatedString {
                            append("By continuing, you agree to our ")
                            pushStringAnnotation("terms", "https://bookkeeperfi.pages.dev/terms")
                            withStyle(style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )) {
                                append("Terms")
                            }
                            pop()
                            append(" and ")
                            pushStringAnnotation("privacy", "https://bookkeeperfi.pages.dev/privacy")
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
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
    }
}