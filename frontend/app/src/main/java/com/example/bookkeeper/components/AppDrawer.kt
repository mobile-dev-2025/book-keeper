package com.example.bookkeeper.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    val scrollState = rememberScrollState()

    // Create list of drawer items - with Statistics option
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
        DrawerItem("Statistics", Icons.Default.QueryStats, "statistics") {
            navController.navigate("statistics") { launchSingleTop = true }
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
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Drawer header with user info and logo
        DrawerHeader(
            userName = currentUser?.name ?: "Book Keeper User",
            userEmail = currentUser?.email ?: "No Email",
            userSubId = currentUser?.id ?: "No Sub ID"
        )

        HorizontalDivider()

        // Scrollable drawer items list
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // Drawer items
            drawerItems.forEach { item ->
                DrawerItemRow(item)
            }
        }

        // Add a small safe area at the bottom
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun DrawerHeader(userName: String, userEmail: String, userSubId: String) {
    // State to track logo click count
    var logoClickCount by remember { mutableStateOf(0) }

    // State to control the visibility of the sub ID
    val showSubId = logoClickCount >= 20

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        val context = LocalContext.current

        // Updated logo handling with better sizing
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(4.dp)
                .clickable {
                    // Increment click counter when logo is clicked
                    logoClickCount++
                }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(LOGO_URL)
                    .crossfade(true)
                    .build(),
                contentDescription = "Book Keeper Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // User name with overflow handling
        Text(
            text = userName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // User email with overflow handling
        Text(
            text = userEmail,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // User sub ID (Only shown after 20 clicks on the logo)
        if (showSubId) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ID: $userSubId",
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
            .padding(vertical = 12.dp, horizontal = 16.dp),
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
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}