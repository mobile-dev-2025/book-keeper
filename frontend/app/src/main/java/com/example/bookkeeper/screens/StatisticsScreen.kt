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
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bookkeeper.data.Book
import com.example.bookkeeper.viewmodel.BookState
import com.example.bookkeeper.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    bookViewModel: BookViewModel,
    onMenuClick: () -> Unit
) {
    val booksState by bookViewModel.booksState.collectAsState()
    val readingPlansState by bookViewModel.readingPlansState.collectAsState()
    val scrollState = rememberScrollState()

    // State for selected book
    var selectedBookIndex by remember { mutableStateOf(-1) }
    var expanded by remember { mutableStateOf(false) }

    // Load books and reading plans when the screen is first displayed
    LaunchedEffect(Unit) {
        bookViewModel.loadBooks()
        bookViewModel.loadReadingPlans()
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

                            // Generate sample reading progress data (in real app, this would come from actual data)
                            val readingData = generateSampleReadingData(selectedBook)

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
                                            text = "DAY",
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
                                    readingData.forEachIndexed { index, data ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(vertical = 12.dp)
                                            )
                                            Text(
                                                text = "${data.planned}",
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(vertical = 12.dp)
                                            )
                                            Text(
                                                text = "${data.actual}",
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(vertical = 12.dp)
                                            )
                                            Text(
                                                text = "+${data.actual - data.planned}",
                                                color = MaterialTheme.colorScheme.onSurface,
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
                                        readingData = readingData,
                                        planColor = Color(0xFFF8A055),
                                        actualColor = Color(0xFF4ECDC4)
                                    )
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
    readingData: List<ReadingData>,
    planColor: Color,
    actualColor: Color
) {
    // Calculate the maximum value for scaling
    val maxPlanned = readingData.maxOfOrNull { it.planned } ?: 1
    val maxActual = readingData.maxOfOrNull { it.actual } ?: 1
    val maxValue = maxOf(maxPlanned, maxActual)
    val dataPoints = readingData.size

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
            val plannedPoints = readingData.mapIndexed { index, data ->
                val x = padding + index * (chartWidth / (dataPoints - 1))
                val y = height - padding - (data.planned.toFloat() / maxValue) * chartHeight
                Offset(x, y)
            }

            // Calculate point positions for actual progress
            val actualPoints = readingData.mapIndexed { index, data ->
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

// Data class for reading progress
data class ReadingData(
    val day: Int,
    val planned: Int,
    val actual: Int
)

// Function to generate sample reading data (in a real app, this would come from actual reading progress)
fun generateSampleReadingData(book: Book): List<ReadingData> {
    // In a real app, you would fetch this data from the database
    // For now, we'll generate some sample data

    val sampleData = listOf(
        ReadingData(1, 10, 15),
        ReadingData(2, 20, 30),
        ReadingData(3, 30, 45),
        ReadingData(4, 40, 60)
    )

    return sampleData
}
