package com.example.bookkeeper.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bookkeeper.data.Book
import com.example.bookkeeper.data.ReadingStats
import com.example.bookkeeper.viewmodel.BookState
import com.example.bookkeeper.viewmodel.BookViewModel
import com.example.bookkeeper.viewmodel.ReadingStatsState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    bookViewModel: BookViewModel,
    onMenuClick: () -> Unit
) {
    val booksState by bookViewModel.booksState.collectAsState()
    val readingStatsState by bookViewModel.readingStatsState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // State for selected book
    var selectedBookIndex by remember { mutableStateOf(-1) }
    var expanded by remember { mutableStateOf(false) }

    // Load books when the screen is first displayed
    LaunchedEffect(Unit) {
        bookViewModel.loadBooks()
    }

    // Trigger loading reading stats when book selection changes
    LaunchedEffect(selectedBookIndex) {
        if (selectedBookIndex >= 0) {
            val books = (booksState as? BookState.Success)?.books ?: return@LaunchedEffect
            if (books.isNotEmpty() && selectedBookIndex < books.size) {
                val selectedBook = books[selectedBookIndex]
                bookViewModel.loadReadingStats(selectedBook.title)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Statistics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                            text = "Could not load your reading statistics",
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

            is BookState.Success -> {
                val books = (booksState as BookState.Success).books

                if (books.isEmpty()) {
                    // Empty state
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
                                imageVector = Icons.Default.QueryStats,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "No Statistics Yet",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Add books and track your reading progress to see statistics",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Display statistics with book selection
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Book selection dropdown - fixed implementation
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = if (selectedBookIndex >= 0) books[selectedBookIndex].title else "",
                                onValueChange = { },
                                readOnly = true,
                                placeholder = { Text("Select a book to view statistics") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Book,
                                        contentDescription = "Book"
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                label = { Text("Book name") },
                                singleLine = true
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                books.forEachIndexed { index, book ->
                                    DropdownMenuItem(
                                        text = { Text(book.title) },
                                        onClick = {
                                            selectedBookIndex = index
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Show content only if a book is selected
                        if (selectedBookIndex >= 0) {
                            val selectedBook = books[selectedBookIndex]

                            // Stats content depends on reading stats state
                            when (readingStatsState) {
                                is ReadingStatsState.Loading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }

                                is ReadingStatsState.Error -> {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Error Loading Statistics",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = (readingStatsState as ReadingStatsState.Error).message,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Button(
                                                onClick = {
                                                    bookViewModel.loadReadingStats(selectedBook.title)
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.onErrorContainer,
                                                    contentColor = MaterialTheme.colorScheme.errorContainer
                                                )
                                            ) {
                                                Icon(
                                                    Icons.Outlined.Refresh,
                                                    contentDescription = "Retry"
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Retry")
                                            }
                                        }
                                    }
                                }

                                is ReadingStatsState.Success -> {
                                    val readingStats = (readingStatsState as ReadingStatsState.Success).stats

                                    if (readingStats.isEmpty()) {
                                        // No stats available yet
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "No Reading Data Available",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                    text = "Update your reading progress to see statistics",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    } else {
                                        // Format dates for better display in table
                                        val formattedStats = formatReadingStats(readingStats)

                                        // Reading progress table
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp)
                                            ) {
                                                // Table header
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "DATE",
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .padding(vertical = 12.dp)
                                                    )
                                                    Text(
                                                        text = "PLAN",
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .padding(vertical = 12.dp)
                                                    )
                                                    Text(
                                                        text = "ACTUAL",
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .padding(vertical = 12.dp)
                                                    )
                                                    Text(
                                                        text = "BONUS",
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .padding(vertical = 12.dp)
                                                    )
                                                }

                                                HorizontalDivider()

                                                // Table rows
                                                formattedStats.forEach { stat ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = stat.formattedDate,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .padding(vertical = 12.dp)
                                                        )
                                                        Text(
                                                            text = "${stat.plan}",
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .padding(vertical = 12.dp)
                                                        )
                                                        Text(
                                                            text = "${stat.actual}",
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .padding(vertical = 12.dp)
                                                        )
                                                        val bonusText = if (stat.bonus >= 0) "+${stat.bonus}" else "${stat.bonus}"
                                                        val bonusColor = if (stat.bonus >= 0)
                                                            MaterialTheme.colorScheme.primary
                                                        else
                                                            MaterialTheme.colorScheme.error
                                                        Text(
                                                            text = bonusText,
                                                            color = bonusColor,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .padding(vertical = 12.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Reading progress legend
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Reading Plan legend
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFF8A055))
                                            )
                                            Text(
                                                text = " Reading Plan",
                                                style = MaterialTheme.typography.bodyMedium
                                            )

                                            Spacer(modifier = Modifier.width(24.dp))

                                            // Actual Pages legend
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF4ECDC4))
                                            )
                                            Text(
                                                text = " Actual Pages",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }

                                        // Progress chart
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(16.dp)
                                            ) {
                                                ReadingProgressChart(
                                                    readingStats = readingStats,
                                                    planColor = Color(0xFFF8A055),
                                                    actualColor = Color(0xFF4ECDC4)
                                                )
                                            }
                                        }
                                    }
                                }

                                else -> {
                                    // Idle state - empty content
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Select a book to view statistics",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        } else {
                            // No book selected message
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
                                        imageVector = Icons.Default.QueryStats,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Select a book to view statistics",
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Choose a book from the dropdown to see detailed reading progress statistics",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            else -> {
                // Idle state, do nothing
            }
        }
    }
}

// Reading progress chart component
@Composable
fun ReadingProgressChart(
    readingStats: List<ReadingStats>,
    planColor: Color,
    actualColor: Color
) {
    // Calculate the maximum value for scaling
    val maxPlanned = readingStats.maxOfOrNull { it.plan } ?: 1
    val maxActual = readingStats.maxOfOrNull { it.actual } ?: 1
    val maxValue = maxOf(maxPlanned, maxActual)
    val dataPoints = readingStats.size

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val padding = 40f

        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding

        // Draw axes
        drawLine(
            color = Color.Black,
            start = Offset(padding, height - padding),
            end = Offset(width - padding, height - padding),
            strokeWidth = 2f
        )

        drawLine(
            color = Color.Black,
            start = Offset(padding, height - padding),
            end = Offset(padding, padding),
            strokeWidth = 2f
        )

        // Draw data points and lines if we have more than one data point
        if (dataPoints > 1) {
            // Calculate point positions for planned progress
            val plannedPoints = readingStats.mapIndexed { index, data ->
                val x = padding + index * (chartWidth / (dataPoints - 1))
                val y = height - padding - (data.plan.toFloat() / maxValue) * chartHeight
                Offset(x, y)
            }

            // Calculate point positions for actual progress
            val actualPoints = readingStats.mapIndexed { index, data ->
                val x = padding + index * (chartWidth / (dataPoints - 1))
                val y = height - padding - (data.actual.toFloat() / maxValue) * chartHeight
                Offset(x, y)
            }

            // Draw lines connecting planned points
            for (i in 0 until plannedPoints.size - 1) {
                drawLine(
                    color = planColor,
                    start = plannedPoints[i],
                    end = plannedPoints[i + 1],
                    strokeWidth = 3f
                )
            }

            // Draw lines connecting actual points
            for (i in 0 until actualPoints.size - 1) {
                drawLine(
                    color = actualColor,
                    start = actualPoints[i],
                    end = actualPoints[i + 1],
                    strokeWidth = 3f
                )
            }

            // Draw planned data points
            plannedPoints.forEach { point ->
                drawCircle(
                    color = planColor,
                    radius = 5f,
                    center = point
                )
            }

            // Draw actual data points
            actualPoints.forEach { point ->
                drawCircle(
                    color = actualColor,
                    radius = 5f,
                    center = point
                )
            }
        }
    }
}

// Data class for formatted reading stats
data class FormattedReadingStats(
    val formattedDate: String,
    val plan: Int,
    val actual: Int,
    val bonus: Int
)

// Function to format reading stats dates
fun formatReadingStats(stats: List<ReadingStats>): List<FormattedReadingStats> {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

    return stats.map { stat ->
        try {
            val date = inputFormat.parse(stat.date)
            val formattedDate = if (date != null) outputFormat.format(date) else stat.date
            FormattedReadingStats(
                formattedDate = formattedDate,
                plan = stat.plan,
                actual = stat.actual,
                bonus = stat.bonus
            )
        } catch (e: Exception) {
            // If date parsing fails, keep the original date
            FormattedReadingStats(
                formattedDate = stat.date,
                plan = stat.plan,
                actual = stat.actual,
                bonus = stat.bonus
            )
        }
    }
}