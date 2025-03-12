package com.example.bookkeeper.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bookkeeper.viewmodel.Auth0ViewModel

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
        DrawerItem(
            title = "My Books",
            icon = Icons.Default.Book,
            route = "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
                onCloseDrawer()
            }
        ),
        DrawerItem(
            title = "Reading History",
            icon = Icons.AutoMirrored.Filled.LibraryBooks,
            route = "history",
            onClick = {
                navController.navigate("history") {
                    launchSingleTop = true
                }
                onCloseDrawer()
            }
        ),
        DrawerItem(
            title = "Profile",
            icon = Icons.Default.AccountCircle,
            route = "profile",
            onClick = {
                navController.navigate("profile") {
                    launchSingleTop = true
                }
                onCloseDrawer()
            }
        ),
        DrawerItem(
            title = "Settings",
            icon = Icons.Default.Settings,
            route = "settings",
            onClick = {
                navController.navigate("settings") {
                    launchSingleTop = true
                }
                onCloseDrawer()
            }
        ),
        DrawerItem(
            title = "About",
            icon = Icons.Default.Info,
            route = "about",
            onClick = {
                navController.navigate("about") {
                    launchSingleTop = true
                }
                onCloseDrawer()
            }
        ),
        DrawerItem(
            title = "Logout",
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            onClick = {
                onLogout()
                onCloseDrawer()
            }
        )
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Drawer header with user info
        DrawerHeader(
            userName = currentUser?.name ?: "Book Keeper User",
            userEmail = currentUser?.email ?: ""
        )

        HorizontalDivider()  // Updated from Divider to HorizontalDivider

        // Drawer items
        drawerItems.forEach { item ->
            DrawerItemRow(
                item = item,
                onItemClick = item.onClick
            )
        }
    }
}

@Composable
fun DrawerHeader(userName: String, userEmail: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            // App logo/icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                contentDescription = "Book Keeper Logo",
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.onPrimary
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
        }
    }
}

@Composable
fun DrawerItemRow(item: DrawerItem, onItemClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
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