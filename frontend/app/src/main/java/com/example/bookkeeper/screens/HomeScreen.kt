package com.example.bookkeeper.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookkeeper.viewmodel.Auth0ViewModel
import com.example.bookkeeper.viewmodel.BookViewModel
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReadingEntry(
    val date: Date = Date(),
    val pagesRead: Int = 0,
    val currentPage: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: Auth0ViewModel,
    bookViewModel: BookViewModel,
    onLogout: () -> Unit,
    onMenuClick: () -> Unit
) {
    // State for form fields
    var bookTitle by remember { mutableStateOf("") }
    var pagesPerDay by remember { mutableStateOf("") }
    var daysLeft by remember { mutableStateOf("") }
    var currentPage by remember { mutableStateOf("") }

    // State for reading history
    var readingHistory by remember { mutableStateOf(listOf<ReadingEntry>()) }

    // State for dropdown
    var expanded by remember { mutableStateOf(false) }
    var selectedBookIndex by remember { mutableStateOf(-1) }

    // Context for toast messages
    val context = LocalContext.current

    // Date formatter
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Load books when the screen is first displayed
    val booksState by bookViewModel.booksState.collectAsState()
    val scrollState = rememberScrollState()

    // Get the books list
    val books = when (booksState) {
        is com.example.bookkeeper.viewmodel.BookState.Success -> {
            (booksState as com.example.bookkeeper.viewmodel.BookState.Success).books
        }
        else -> emptyList()
    }

    LaunchedEffect(Unit) {
        bookViewModel.loadBooks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Home",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
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
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Book title input with dropdown
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = if (selectedBookIndex >= 0 && selectedBookIndex < books.size)
                        books[selectedBookIndex].title else bookTitle,
                    onValueChange = { bookTitle = it },
                    placeholder = { Text("Book title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Book,
                            contentDescription = "Book"
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            modifier = Modifier.clickable { expanded = true }
                        )
                    },
                    singleLine = true
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    books.forEachIndexed { index, book ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    book.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            onClick = {
                                selectedBookIndex = index
                                bookTitle = book.title
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Pages per day input
            OutlinedTextField(
                value = pagesPerDay,
                onValueChange = { pagesPerDay = it },
                placeholder = { Text("pages per day / actual pages per day") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = "Pages per day"
                    )
                },
                singleLine = true
            )

            // Advised days left input
            OutlinedTextField(
                value = daysLeft,
                onValueChange = { daysLeft = it },
                placeholder = { Text("Advised days left / Actual days left") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = "Days left"
                    )
                },
                singleLine = true
            )

            // Current Page and Done buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = currentPage,
                    onValueChange = { currentPage = it },
                    placeholder = { Text("Current Page") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Numbers,
                            contentDescription = "Current Page"
                        )
                    },
                    singleLine = true
                )

                // Done button implementation as a proper button
                Button(
                    onClick = {
                        if (currentPage.isNotEmpty()) {
                            // Add current reading entry to history
                            val currentPageInt = currentPage.toIntOrNull() ?: 0
                            if (currentPageInt > 0) {
                                val newEntry = ReadingEntry(
                                    date = Date(),
                                    pagesRead = currentPageInt,
                                    currentPage = currentPageInt
                                )
                                readingHistory = readingHistory + newEntry
                                Toast.makeText(context, "Reading progress updated!", Toast.LENGTH_SHORT).show()
                                currentPage = "" // Clear current page after adding
                            }
                        } else {
                            Toast.makeText(context, "Please enter current page", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp), // Match height with TextField
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Done"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Done")
                    }
                }
            }

            // Daily Read History card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.LibraryBooks,
                            contentDescription = "History"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily Read History",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // Display reading history entries
                    if (readingHistory.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No reading history yet. Click 'Done' to add entries.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn {
                            items(readingHistory) { entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = dateFormatter.format(entry.date),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Page ${entry.currentPage}",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }

            // Finished Book button (centered)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = { /* Mark book as finished */ },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.width(200.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.LibraryBooks,
                            contentDescription = "Finished Book"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Finished Book")
                    }
                }
            }

            // Add spacing at the bottom for better scrolling experience
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}