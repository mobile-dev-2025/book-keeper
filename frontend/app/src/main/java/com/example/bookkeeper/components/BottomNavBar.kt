package com.example.bookkeeper.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

// Data class to represent navigation items
data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun BottomNavBar(navController: NavController) {
    // Define navigation items
    val navItems = listOf(
        BottomNavItem(
            name = "Home",
            route = "home",
            icon = Icons.Filled.Dashboard
        ),
        BottomNavItem(
            name = "Add Book",
            route = "add_book",
            icon = Icons.Filled.AddCircle
        ),
        BottomNavItem(
            name = "History",
            route = "books",
            icon = Icons.AutoMirrored.Filled.MenuBook
        ),
        BottomNavItem(
            name = "Stats",
            route = "statistics",
            icon = Icons.Filled.QueryStats
        )
    )

    // Get current route to highlight the correct tab
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    // Navigate only if we're not already on that route
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Use a simpler navigation pattern to avoid transition issues
                            popUpTo(navController.graph.startDestinationId)
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Don't restore state to prevent buggy transitions
                            restoreState = false
                            // Disable animations to speed up transitions
                            anim {
                                enter = 0
                                exit = 0
                                popEnter = 0
                                popExit = 0
                            }
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.name) },
                label = { Text(text = item.name) }
            )
        }
    }
}