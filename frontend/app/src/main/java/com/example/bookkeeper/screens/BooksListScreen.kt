package com.example.bookkeeper.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookkeeper.data.Book
import com.example.bookkeeper.viewmodel.BookState
import com.example.bookkeeper.viewmodel.BookViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksListScreen(
    bookViewModel: BookViewModel,
    onMenuClick: () -> Unit,
    onAddBookClick: () -> Unit
) {
    val booksState by bookViewModel.booksState.collectAsState()

    // Load books when the screen is first displayed
    LaunchedEffect(Unit) {
        bookViewModel.loadBooks()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Reading Journey", style = MaterialTheme.typography.titleLarge) },
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddBookClick,
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Book") },
                text = { Text("Add Book") }
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
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            is BookState.Success -> {
                val books = (booksState as BookState.Success).books
                if (books.isEmpty()) {
                    // Empty state with animation
                    EmptyBooksView(onAddBookClick, paddingValues)
                } else {
                    // Book list with better UI
                    BooksList(books, paddingValues)
                }
            }

            is BookState.Error -> {
                // Error state with better UI
                ErrorView(
                    message = (booksState as BookState.Error).message,
                    onRetry = { bookViewModel.loadBooks() },
                    paddingValues = paddingValues
                )
            }

            else -> {}
        }
    }
}

@Composable
fun EmptyBooksView(onAddBookClick: () -> Unit, paddingValues: PaddingValues) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(1000)) +
                    slideInVertically(animationSpec = tween(1000)) { it / 2 }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Your Bookshelf is Empty",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Track your reading progress by adding books to your collection",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onAddBookClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Add Your First Book", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun BooksList(books: List<Book>, paddingValues: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Reading Summary",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Helper function to determine if a book is truly completed
                    val completedBooks = books.count { book ->
                        // A book is considered completed if it has an end date
                        // AND the end date doesn't look like a default timestamp
                        !book.endDate.isNullOrBlank() &&
                                !book.endDate.contains("T00:00:00.000Z")
                    }

                    Text(
                        text = "${books.size} books • $completedBooks completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        items(books) { book ->
            EnhancedBookCard2025(book = book)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // For FAB space
        }
    }
}

// Modern 2025 book card component with improved UI
@Composable
fun EnhancedBookCard2025(book: Book) {
    // Safe unwrapping of nullable integers with defaults
    val pagesRead = book.pagesRead ?: 0
    val totalPages = book.totalPages ?: 0
    val hasTotal = totalPages > 0
    val readingProgress = if (hasTotal) pagesRead.toFloat() / totalPages else 0f
    val progressPercent = (readingProgress * 100).toInt()

    // State for expanded card
    var expanded by remember { mutableStateOf(false) }

    // Format dates for better display
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val startDateFormatted = try {
        book.startDate?.let {
            if (it.contains("T")) {
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(it)
                dateFormatter.format(date)
            } else {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it)
                dateFormatter.format(date)
            }
        } ?: "Not set"
    } catch (_: Exception) {
        book.startDate ?: "Not set"
    }

    val endDateFormatted = try {
        // Helper function to check if this book is truly completed
        val isCompleted = !book.endDate.isNullOrBlank() && !book.endDate.contains("T00:00:00.000Z")
        if (isCompleted) {
            book.endDate?.let {
                if (it.contains("T")) {
                    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(it)
                    dateFormatter.format(date)
                } else {
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it)
                    dateFormatter.format(date)
                }
            } ?: "Not set"
        } else {
            "In progress"
        }
    } catch (_: Exception) {
        book.endDate ?: "Not set"
    }

    // Calculate reading stats
    val pagesRemaining = if (hasTotal) totalPages - pagesRead else 0
    val isCompleted = progressPercent >= 100

    // Determine color based on progress
    val progressColor = when {
        progressPercent >= 75 -> MaterialTheme.colorScheme.primary
        progressPercent >= 50 -> MaterialTheme.colorScheme.tertiary
        progressPercent >= 25 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with title and progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Book icon with background
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Title text
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Progress circular indicator
                if (hasTotal) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { readingProgress },
                            modifier = Modifier.size(62.dp),
                            strokeWidth = 6.dp,
                            color = progressColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Text(
                            text = "${progressPercent}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress linear indicator with modern rounded edges
            if (hasTotal) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { readingProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp)),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Pages counter
                    Text(
                        text = "$pagesRead/$totalPages",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pages indicator
                Text(
                    text = if (isCompleted) "Completed!" else "$pagesRemaining pages remaining",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Dates section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start date text
                Text(
                    text = "Started: $startDateFormatted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // End date or in progress
                val endDateColor = if (endDateFormatted == "In progress")
                    MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.onSurfaceVariant

                Text(
                    text = if (endDateFormatted == "In progress") "In progress" else "Finished: $endDateFormatted",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (endDateFormatted == "In progress") FontWeight.Medium else FontWeight.Normal,
                    color = endDateColor
                )
            }

            // Expandable content - Only show if expanded
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Notes section
                if (!book.notes.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Notes: ${book.notes}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Action button - only "Mark as Read" now
                if (!isCompleted) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { /* Mark as finished */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark as Read", fontSize = 16.sp)
                        }
                    }
                }
            }

            // Expand/collapse button
            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit, paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Oops! Something went wrong",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Try Again")
            }
        }
    }
}