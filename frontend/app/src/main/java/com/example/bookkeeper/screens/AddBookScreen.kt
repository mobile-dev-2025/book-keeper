package com.example.bookkeeper.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bookkeeper.viewmodel.AddBookState
import com.example.bookkeeper.viewmodel.BookViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookScreen(
    bookViewModel: BookViewModel,
    onMenuClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val addBookState by bookViewModel.addBookState.collectAsState()

    // State for book form fields
    var title by remember { mutableStateOf("") }
    var totalPages by remember { mutableStateOf("") }
    var pagesRead by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // State for dates
    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember { mutableStateOf(Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(14))) } // Default to 14 days later

    // State for calculation results
    var calculationResult by remember { mutableStateOf<String?>(null) }

    // State for date pickers
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Date formatter
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Calculate reading plan
    fun calculateReadingPlan() {
        if (totalPages.isBlank() || pagesRead.isBlank()) {
            Toast.makeText(context, "Please enter total pages and pages read", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val totalPagesNum = totalPages.toInt()
            val pagesReadNum = pagesRead.toInt()
            val remainingPages = totalPagesNum - pagesReadNum

            if (remainingPages <= 0) {
                calculationResult = "You've already completed this book!"
                return
            }

            val startMillis = startDate.time
            val endMillis = endDate.time
            val diffMillis = endMillis - startMillis
            val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

            if (diffDays <= 0) {
                calculationResult = "End date must be after start date"
                return
            }

            val pagesPerDay = Math.ceil(remainingPages.toDouble() / diffDays).toInt()
            calculationResult = "To finish \"$title\" ($totalPagesNum pages) in $diffDays days,\nyou need to read $pagesPerDay pages per day."

        } catch (e: NumberFormatException) {
            calculationResult = "Please enter valid numbers"
        }
    }

    // Handle success or error after adding a book
    LaunchedEffect(addBookState) {
        when (addBookState) {
            is AddBookState.Success -> {
                Toast.makeText(context, "Book added successfully!", Toast.LENGTH_LONG).show()
                // Navigate back after successful addition
                onNavigateBack()
                bookViewModel.resetAddBookState()
            }
            is AddBookState.Error -> {
                val message = (addBookState as AddBookState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                bookViewModel.resetAddBookState()
            }
            else -> {}
        }
    }

    // Date picker dialogs
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate.time)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        startDate = Date(it)
                        calculateReadingPlan() // Recalculate when date changes
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate.time)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        endDate = Date(it)
                        calculateReadingPlan() // Recalculate when date changes
                    }
                    showEndDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Book") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    calculateReadingPlan()
                },
                label = { Text("Book Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Book,
                        contentDescription = "Book"
                    )
                },
                singleLine = true
            )

            // Total Pages Input
            OutlinedTextField(
                value = totalPages,
                onValueChange = {
                    totalPages = it
                    calculateReadingPlan()
                },
                label = { Text("Total Pages") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Numbers,
                        contentDescription = "Total Pages"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Pages Read Input
            OutlinedTextField(
                value = pagesRead,
                onValueChange = {
                    pagesRead = it
                    calculateReadingPlan()
                },
                label = { Text("Pages Read") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Numbers,
                        contentDescription = "Pages Read"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Start Date Picker
            OutlinedTextField(
                value = dateFormatter.format(startDate),
                onValueChange = { },
                label = { Text("Start Date") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                readOnly = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = "Start Date"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showStartDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                }
            )

            // End Date Picker
            OutlinedTextField(
                value = dateFormatter.format(endDate),
                onValueChange = { },
                label = { Text("End Date") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                readOnly = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = "End Date"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showEndDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                }
            )

            // Reading Plan Calculator Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Calculator"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reading Plan Calculator",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (calculationResult != null) {
                        Text(
                            text = calculationResult!!,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Text(
                            text = "Enter book details to calculate your reading plan",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = { calculateReadingPlan() },
                        modifier = Modifier.padding(top = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Calculate Reading Plan")
                    }
                }
            }

            // Notes Input
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(16.dp),
                maxLines = 5
            )

            // Submit Button - UPDATED
            Button(
                onClick = {
                    // Validate input
                    if (title.isBlank()) {
                        Toast.makeText(context, "Please enter a book title", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (totalPages.isBlank() || totalPages.toIntOrNull() == null) {
                        Toast.makeText(context, "Please enter total pages", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (pagesRead.isBlank() || pagesRead.toIntOrNull() == null) {
                        Toast.makeText(context, "Please enter a valid number of pages read", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Submit form with totalPages parameter
                    bookViewModel.addBook(
                        title = title,
                        pagesRead = pagesRead.toInt(),
                        totalPages = totalPages.toInt(), // Add the total pages
                        startDate = dateFormatter.format(startDate),
                        endDate = dateFormatter.format(endDate),
                        notes = notes.ifBlank { null }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                enabled = addBookState !is AddBookState.Loading
            ) {
                if (addBookState is AddBookState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Add Book")
                }
            }

            // Add spacing at the bottom for better scrolling experience
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}