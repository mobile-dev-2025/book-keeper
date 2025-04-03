package com.example.bookkeeper.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookkeeper.data.Book
import com.example.bookkeeper.viewmodel.Auth0ViewModel
import com.example.bookkeeper.viewmodel.BookState
import com.example.bookkeeper.viewmodel.BookViewModel
import com.example.bookkeeper.viewmodel.ReadingPlansState
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: Auth0ViewModel,
    bookViewModel: BookViewModel,
    onLogout: () -> Unit,
    onMenuClick: () -> Unit
) {
    // Load books and reading plans when the screen is first displayed
    val booksState by bookViewModel.booksState.collectAsState()
    val readingPlansState by bookViewModel.readingPlansState.collectAsState()
    val scrollState = rememberScrollState()

    // Context for toast messages
    val context = LocalContext.current

    // Date formatter
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // State for form fields and calculations
    var selectedBookIndex by remember { mutableStateOf(-1) }
    var expanded by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf("") }

    // Reading plan and progress information
    var pagesPerDay by remember { mutableStateOf("") }
    var daysLeft by remember { mutableStateOf("") }
    var finishDate by remember { mutableStateOf("") }

    // Get the books list and reading plans
    val books = when (booksState) {
        is BookState.Success -> (booksState as BookState.Success).books
        else -> emptyList()
    }

    val readingPlans = when (readingPlansState) {
        is ReadingPlansState.Success -> (readingPlansState as ReadingPlansState.Success).readingPlans
        else -> emptyList()
    }

    // State to track when updates occur so we can reload data
    var updateCounter by remember { mutableStateOf(0) }

    // Selected book and its reading plan - with key tracking changes
    val selectedBook = if (selectedBookIndex >= 0 && selectedBookIndex < books.size) {
        books[selectedBookIndex]
    } else null

    // Find reading plan for selected book
    val selectedBookReadingPlan = selectedBook?.let { book ->
        readingPlans.find { it.bookTitle == book.title }
    }

    // Function to update the reading plan UI values
    fun updateReadingPlanUI(newPagesPerDay: String, newDaysLeft: String, newFinishDate: String) {
        pagesPerDay = newPagesPerDay
        daysLeft = newDaysLeft
        finishDate = newFinishDate
    }

    // Calculate reading plan details when a book is selected or data is updated
    LaunchedEffect(selectedBook, selectedBookReadingPlan, updateCounter, books) {
        selectedBook?.let { book ->
            // Get pages read and total pages
            val pagesRemaining = (book.totalPages ?: 0) - (book.pagesRead ?: 0)

            // Set current page from book's pages read (making sure to update correctly)
            currentPage = (book.pagesRead ?: 0).toString()
            Log.d("HomeScreen", "Book pages read: ${book.pagesRead}, set current page to: $currentPage")

            // Default values
            var calculatedPagesPerDay = "0"
            var calculatedDaysLeft = "N/A"
            var calculatedFinishDate = "Not set"

            // Get PPD from reading plan if available
            if (selectedBookReadingPlan != null) {
                try {
                    // Get pages per day from reading plan
                    calculatedPagesPerDay = selectedBookReadingPlan.pagesPerDay.toString()

                    // Parse end date
                    val endDateObj = if (selectedBookReadingPlan.endDate.contains("T")) {
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(selectedBookReadingPlan.endDate)
                    } else {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedBookReadingPlan.endDate)
                    }

                    // Calculate days left and format finish date
                    if (endDateObj != null) {
                        val today = Date()
                        val daysLeftValue = TimeUnit.MILLISECONDS.toDays(endDateObj.time - today.time)
                        calculatedDaysLeft = daysLeftValue.toString()
                        calculatedFinishDate = dateFormatter.format(endDateObj)
                    }
                } catch (e: Exception) {
                    Log.e("HomeScreen", "Error parsing reading plan data", e)
                }
            } else {
                // Calculate if no reading plan exists
                try {
                    // Parse dates
                    val startDate = if (book.startDate.contains("T")) {
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(book.startDate)
                    } else {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(book.startDate)
                    }

                    val endDate = if (!book.endDate.isNullOrBlank() && book.endDate.contains("T")) {
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(book.endDate)
                    } else if (!book.endDate.isNullOrBlank()) {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(book.endDate)
                    } else null

                    if (startDate != null && endDate != null) {
                        // Calculate time difference in days
                        val diffInMillis = endDate.time - startDate.time
                        val diffDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

                        // Calculate days left
                        val today = Date()
                        val daysFromStart = TimeUnit.MILLISECONDS.toDays(today.time - startDate.time)
                        val remainingDays = diffDays - daysFromStart

                        // Make sure days left is not negative
                        val adjustedRemainingDays = if (remainingDays < 0) 0 else remainingDays

                        // Calculate pages per day
                        val pagesPerDayValue = if (diffDays > 0)
                            Math.ceil(pagesRemaining.toDouble() / diffDays).toInt()
                        else 0

                        // Update calculated values
                        calculatedPagesPerDay = pagesPerDayValue.toString()
                        calculatedDaysLeft = adjustedRemainingDays.toString()
                        calculatedFinishDate = dateFormatter.format(endDate)
                    }
                } catch (e: Exception) {
                    Log.e("HomeScreen", "Error parsing book dates", e)
                }
            }

            // Update the UI values
            updateReadingPlanUI(calculatedPagesPerDay, calculatedDaysLeft, calculatedFinishDate)
        }
    }

    // Load books and reading plans on first launch and when needed
    LaunchedEffect(Unit, updateCounter) {
        bookViewModel.loadBooks()
        bookViewModel.loadReadingPlans()
        Log.d("HomeScreen", "Loaded books and reading plans (update count: $updateCounter)")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Reading Dashboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        when (booksState) {
            is BookState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is BookState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Could not load your books",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = (booksState as BookState.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = { bookViewModel.loadBooks() }) {
                            Text("Try Again")
                        }
                    }
                }
            }

            else -> {
                // Empty state if no books
                if (books.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "No Books Yet",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Add your first book to start tracking your reading progress",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { /* Navigate to add book screen */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Add Your First Book")
                            }
                        }
                    }
                } else {
                    // Main content with books
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // FIXED: Book selection dropdown using ExposedDropdownMenuBox
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedBook?.title ?: "",
                                onValueChange = { },
                                readOnly = true,
                                placeholder = { Text("Select a book to track") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(), // Key for proper anchoring
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Book,
                                        contentDescription = "Book"
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                label = { Text("Current Book") },
                                singleLine = true
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.exposedDropdownSize()
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
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (selectedBook != null) {
                            // Reading Progress Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Reading Progress Header
                                    Text(
                                        text = "Reading Progress",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )

                                    val totalPages = selectedBook.totalPages ?: 0
                                    val pagesRead = selectedBook.pagesRead ?: 0
                                    val remainingPages = totalPages - pagesRead
                                    val progress = if (totalPages > 0) {
                                        (pagesRead.toFloat() / totalPages) * 100
                                    } else 0f

                                    // Progress bar
                                    LinearProgressIndicator(
                                        progress = { progress / 100 },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Progress stats in grid layout
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Row 1: Total Pages and Pages Read
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            // Total Pages
                                            Column {
                                                Text(
                                                    text = "Total Pages",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                                Text(
                                                    text = "$totalPages",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            // Pages Read
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "Pages Read",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                                Text(
                                                    text = "$pagesRead",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        // Row 2: Remaining Pages and Completion Percentage
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            // Remaining Pages
                                            Column {
                                                Text(
                                                    text = "Remaining Pages",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                                Text(
                                                    text = "$remainingPages",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            // Completion Percentage
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "Completed",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                                Text(
                                                    text = "${progress.toInt()}%",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Reading Plan Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Reading Plan Header
                                    Text(
                                        text = "Reading Plan",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )

                                    // Plan stats in grid layout
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Row 1: Days Left and Pages per Day
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            // Days Left
                                            Column {
                                                Text(
                                                    text = "Days Remaining",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                                )
                                                Text(
                                                    text = daysLeft,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }

                                            // Pages per Day
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "Pages per Day",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                                )
                                                Text(
                                                    text = pagesPerDay,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }

                                        // Row 2: Target Finish Date
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CalendarToday,
                                                contentDescription = "Finish date",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Target Finish Date",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                                )
                                                Text(
                                                    text = finishDate,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Current Page Input
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = currentPage,
                                    onValueChange = { currentPage = it },
                                    label = { Text("Current Page") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Bookmark,
                                            contentDescription = "Current Page"
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )

                                // Update Progress Button
                                Button(
                                    onClick = {
                                        val currentPageInt = currentPage.toIntOrNull() ?: 0
                                        val totalPages = selectedBook.totalPages ?: 0

                                        if (currentPageInt <= 0) {
                                            Toast.makeText(context, "Please enter a valid page number", Toast.LENGTH_SHORT).show()
                                        } else if (currentPageInt > totalPages) {
                                            Toast.makeText(context, "Page number cannot exceed total pages", Toast.LENGTH_SHORT).show()
                                        } else {
                                            // Update the book in the database via BookViewModel
                                            bookViewModel.updateBookProgress(
                                                bookId = selectedBook.id ?: -1,
                                                title = selectedBook.title,
                                                currentPage = currentPageInt,
                                                totalPages = selectedBook.totalPages ?: 0
                                            )

                                            // Explicitly reload books to get the latest data
                                            bookViewModel.loadBooks()

                                            // For immediate UI update, forcibly update the progress indicators
                                            val totalPages = selectedBook.totalPages ?: 0
                                            val remainingPages = totalPages - currentPageInt

                                            // Update counter to trigger LaunchedEffect
                                            updateCounter++

                                            Toast.makeText(context, "Reading progress updated to page $currentPageInt!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp), // Match height with TextField
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Update Progress"
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Update")
                                    }
                                }
                            }

                            // Finished Book button
                            Button(
                                onClick = {
                                    selectedBook?.let { book ->
                                        // Mark the book as finished by setting pagesRead to totalPages
                                        bookViewModel.markBookAsFinished(
                                            bookId = book.id ?: -1,
                                            title = book.title,
                                            totalPages = book.totalPages ?: 0
                                        )

                                        // Increment counter to refresh UI
                                        updateCounter++

                                        Toast.makeText(context, "Book marked as finished!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Finished Book"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mark Book as Finished")
                                }
                            }
                        } else {
                            // No book selected state
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Book,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Select a book to track",
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Choose a book from the dropdown to view and update your reading progress",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        // Add spacing at the bottom
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
