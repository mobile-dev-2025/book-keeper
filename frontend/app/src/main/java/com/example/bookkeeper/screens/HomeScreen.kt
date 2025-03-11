package com.example.bookkeeper.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookkeeper.viewmodel.LocalAuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: LocalAuthViewModel,
    onLogout: () -> Unit
) {
    // Get current authenticated user
    val currentUser = authViewModel.getCurrentUser()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Keeper") },
                navigationIcon = {
                    IconButton(onClick = { /* Open drawer in the future */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                actions = {
                    // Logout button
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Welcome card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome, ${currentUser?.name ?: "Reader"}!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "You are logged in with: ${currentUser?.email ?: "Unknown email"}",
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your books will appear here soon.",
                        fontSize = 18.sp
                    )
                }
            }

            // Placeholder content - to be replaced with actual book list
            Spacer(modifier = Modifier.height(32.dp))

            // Stats summary - placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard("Books", "0")
                StatCard("Reading", "0")
                StatCard("Finished", "0")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Placeholder for book list
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Book Collection",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "No books yet. Add your first book to get started!",
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { /* Add book functionality will be added later */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("Add Book")
                    }
                }
            }
        }
    }
}

/**
 * Card displaying a statistic with title and value.
 */
@Composable
fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                fontSize = 14.sp
            )
        }
    }
}