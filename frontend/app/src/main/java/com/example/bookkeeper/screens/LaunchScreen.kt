package com.example.bookkeeper.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

/**
 * Launch/Splash screen that displays the app name, logo image and a "Get Started" button.
 * This is the first screen users see when opening the app.
 * Added animations for a more engaging user experience.
 */
@Composable
fun LaunchScreen(onGetStartedClick: () -> Unit) {
    // Animation states
    val imageAlpha = remember { Animatable(0f) }
    val titleScale = remember { Animatable(0.8f) }
    val buttonAlpha = remember { Animatable(0f) }

    // Start animations when the screen is composed
    LaunchedEffect(key1 = true) {
        // Image fade in
        imageAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )

        // Title scale up animation
        titleScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        // Button fade in after a delay
        delay(600)
        buttonAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo image with fade-in animation
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data("https://raw.githubusercontent.com/mobile-dev-2025/book-keeper/refs/heads/main/assets/Bookkeeper.png")
                        .build()
                ),
                contentDescription = "Book Keeper Logo",
                modifier = Modifier
                    .size(200.dp)
                    .alpha(imageAlpha.value)
                    .padding(bottom = 24.dp),
                contentScale = ContentScale.Fit
            )

            // Title with scale animation
            Text(
                text = "Book Keeper",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .scale(titleScale.value)
                    .padding(bottom = 16.dp)
            )
        }

        // Get Started button with fade-in animation
        Button(
            onClick = onGetStartedClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.BottomCenter)
                .alpha(buttonAlpha.value),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Text("Get Started")
        }
    }
}