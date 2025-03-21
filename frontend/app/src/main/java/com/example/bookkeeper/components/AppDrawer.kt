package com.example.bookkeeper.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bookkeeper.viewmodel.Auth0ViewModel

// Remote logo URL
private const val LOGO_URL = "https://i.postimg.cc/YCpVhDBQ/Bookkeeper.png"

// Data class for drawer items
data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val route: String? = null,
    val onClick: () -> Unit
)

@Composable
fun AppDrawer(
    navController: NavController,
    authViewModel: Auth0ViewModel,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val currentUser = authViewModel.getCurrentUser()

    // Create list of drawer items
    val drawerItems = listOf(
        DrawerItem("My Books", Icons.Default.Book, "home") {
            navController.navigate("home") {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }
            onCloseDrawer()
        },
        DrawerItem("Reading History", Icons.AutoMirrored.Filled.LibraryBooks, "history") {
            navController.navigate("history") { launchSingleTop = true }
            onCloseDrawer()
        },
        DrawerItem("Profile", Icons.Default.AccountCircle, "profile") {
            navController.navigate("profile") { launchSingleTop = true }
            onCloseDrawer()
        },
        DrawerItem("Settings", Icons.Default.Settings, "settings") {
            navController.navigate("settings") { launchSingleTop = true }
            onCloseDrawer()
        },
        DrawerItem("About", Icons.Default.Info, "about") {
            navController.navigate("about") { launchSingleTop = true }
            onCloseDrawer()
        },
        DrawerItem("Logout", Icons.AutoMirrored.Filled.ExitToApp, null) {
            onLogout()
            onCloseDrawer()
        }
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Drawer header with user info including sub ID and updated logo
        DrawerHeader(
            userName = currentUser?.name ?: "Book Keeper User",
            userEmail = currentUser?.email ?: "No Email",
            userSubId = currentUser?.id ?: "No Sub ID"
        )

        HorizontalDivider()

        // Drawer items
        drawerItems.forEach { item ->
            DrawerItemRow(item)
        }
    }
}

@Composable
fun DrawerHeader(userName: String, userEmail: String, userSubId: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            val context = LocalContext.current

            // Updated logo (Remote Image)
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(LOGO_URL)
                    .crossfade(true)
                    .build(),
                contentDescription = "Book Keeper Logo",
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White, CircleShape) // Circular background for better appearance
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User name
            Text(
                text = userName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            // User email
            Text(
                text = userEmail,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )

            // User sub ID (Displayed for testing purposes)
            Text(
                text = "ID: $userSubId",
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun DrawerItemRow(item: DrawerItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = item.title,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
