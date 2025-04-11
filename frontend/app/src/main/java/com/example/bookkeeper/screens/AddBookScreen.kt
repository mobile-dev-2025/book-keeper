package com.example.bookkeeper.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookkeeper.viewmodel.AddBookState
import com.example.bookkeeper.viewmodel.BookViewModel
import com.example.bookkeeper.viewmodel.ReadingPlanState
import kotlinx.coroutines.launch
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
    val readingPlanState by bookViewModel.readingPlanState.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // State variables
    var title by remember { mutableStateOf("") }
    var totalPages by remember { mutableStateOf("") }
    var pagesRead by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember { mutableStateOf(Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(14))) }
    var calculationResult by remember { mutableStateOf<String?>(null) }
    var showCalculationCard by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var createReadingPlan by remember { mutableStateOf(true) }

    // Date formatters
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val serverDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Function to clear all input fields
    fun clearFields() {
        title = ""
        totalPages = ""
        pagesRead = ""
        notes = ""
        calculationResult = null
        showCalculationCard = false
        startDate = Date()
        endDate = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(14))
    }

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
                showCalculationCard = true
                return
            }

            val diffDays = TimeUnit.MILLISECONDS.toDays(endDate.time - startDate.time)
            if (diffDays <= 0) {
                calculationResult = "End date must be after start date"
                showCalculationCard = true
                return
            }

            val pagesPerDay = Math.ceil(remainingPages.toDouble() / diffDays).toInt()
            calculationResult = "To finish \"$title\" in $diffDays days, you need to read $pagesPerDay pages per day."
            showCalculationCard = true
        } catch (e: NumberFormatException) {
            calculationResult = "Please enter valid numbers"
            showCalculationCard = true
        }
    }

    // Handle success or error for book addition
    LaunchedEffect(addBookState) {
        when (addBookState) {
            is AddBookState.Success -> {
                if (readingPlanState !is ReadingPlanState.Loading) {
                    val message = if (readingPlanState is ReadingPlanState.Success) {
                        "Book added with reading plan!"
                    } else {
                        "Book added successfully!"
                    }

                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    clearFields()

                    // Reset states
                    bookViewModel.resetAddBookState()
                    bookViewModel.resetReadingPlanState()

                    // Go back to previous screen
                    onNavigateBack()
                }
            }
            is AddBookState.Error -> {
                Toast.makeText(context, (addBookState as AddBookState.Error).message, Toast.LENGTH_LONG).show()
                bookViewModel.resetAddBookState()
                bookViewModel.resetReadingPlanState()
            }
            else -> {}
        }
    }

    // Handle reading plan specific messages
    LaunchedEffect(readingPlanState) {
        when (readingPlanState) {
            is ReadingPlanState.Error -> {
                // Only show reading plan error if book was added successfully
                if (addBookState is AddBookState.Success) {
                    val message = "Book added but reading plan failed: ${(readingPlanState as ReadingPlanState.Error).message}"
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    clearFields()
                    bookViewModel.resetAddBookState()
                    bookViewModel.resetReadingPlanState()
                    onNavigateBack()
                }
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
                        calculateReadingPlan()
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
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
                        calculateReadingPlan()
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Main Content
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add New Book", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            // Book Details Section
            SectionHeader(
                icon = Icons.Default.Book,
                title = "Book Details",
                color = MaterialTheme.colorScheme.primary
            )

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    calculateReadingPlan()
                },
                label = { Text("Book Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Outlined.Book, contentDescription = "Book") },
                singleLine = true
            )

            // Pages inputs in Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = totalPages,
                    onValueChange = {
                        totalPages = it
                        calculateReadingPlan()
                    },
                    label = { Text("Total Pages") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Outlined.Numbers, contentDescription = "Total Pages") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = pagesRead,
                    onValueChange = {
                        pagesRead = it
                        calculateReadingPlan()
                    },
                    label = { Text("Pages Read") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Outlined.BookmarkAdded, contentDescription = "Pages Read") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // Reading Timeline Section
            SectionHeader(
                icon = Icons.Default.DateRange,
                title = "Reading Timeline",
                color = MaterialTheme.colorScheme.primary
            )

            // Date fields in row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = dateFormatter.format(startDate),
                    onValueChange = { },
                    label = { Text("Start Date") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = "Start Date") },
                    trailingIcon = {
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(Icons.Default.Event, contentDescription = "Select Date")
                        }
                    }
                )

                OutlinedTextField(
                    value = dateFormatter.format(endDate),
                    onValueChange = { },
                    label = { Text("End Date") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Outlined.EventAvailable, contentDescription = "End Date") },
                    trailingIcon = {
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(Icons.Default.Event, contentDescription = "Select Date")
                        }
                    }
                )
            }

            // Reading Plan Option and Calculator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = createReadingPlan,
                    onCheckedChange = { createReadingPlan = it }
                )
                Text(
                    text = "Create reading plan automatically",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            ElevatedButton(
                onClick = { calculateReadingPlan() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    Icons.Default.Calculate,
                    contentDescription = "Calculate",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Calculate Reading Plan")
            }

            // Enhanced Reading Plan Result Card
            AnimatedVisibility(visible = showCalculationCard) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                                contentDescription = "Reading Plan",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Your Reading Plan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            thickness = 1.dp
                        )

                        calculationResult?.let { result ->
                            if (result.contains("already completed")) {
                                // Completed book message
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = "Completed",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "You've already completed this book!",
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            } else if (result.contains("End date must be after")) {
                                // Error message
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Warning,
                                            contentDescription = "Error",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "End date must be after start date",
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            } else if (result.contains("valid numbers")) {
                                // Error message for invalid numbers
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Error,
                                            contentDescription = "Error",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Please enter valid numbers",
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            } else {
                                // Regular reading plan
                                val parts = result.split(",")
                                val titlePart = parts[0]
                                val pagesPart = parts.getOrNull(1)?.trim() ?: ""

                                // Format the book title part
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = titlePart,
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                // Pages per day highlight
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val pagesPerDay = pagesPart.replace("you need to read ", "").replace(" pages per day.", "")
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = pagesPerDay,
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "pages per day",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }

                                // Calculate progress percentage
                                val totalPagesNum = totalPages.toIntOrNull() ?: 1
                                val pagesReadNum = pagesRead.toIntOrNull() ?: 0
                                val progressPercentage = (pagesReadNum.toFloat() / totalPagesNum) * 100

                                Spacer(modifier = Modifier.height(12.dp))

                                // Progress indicator
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Current Progress: ${progressPercentage.toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { progressPercentage / 100 },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$pagesReadNum of $totalPagesNum pages",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Notes Section
            SectionHeader(
                icon = Icons.AutoMirrored.Filled.Notes,
                title = "Additional Notes",
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )

            // Add Book Button with WHITE background and BLACK text
            ElevatedButton(
                onClick = {
                    try {
                        // Validate title
                        if (title.isBlank()) {
                            Toast.makeText(context, "Please enter a book title", Toast.LENGTH_SHORT).show()
                            return@ElevatedButton
                        }

                        // Parse and validate total pages
                        val totalPagesInt: Int
                        try {
                            totalPagesInt = totalPages.trim().toInt()
                            if (totalPagesInt <= 0) {
                                Toast.makeText(context, "Total pages must be positive", Toast.LENGTH_SHORT).show()
                                return@ElevatedButton
                            }
                        } catch (e: NumberFormatException) {
                            Toast.makeText(context, "Please enter a valid number for total pages", Toast.LENGTH_SHORT).show()
                            return@ElevatedButton
                        }

                        // Parse and validate pages read
                        val pagesReadInt: Int
                        try {
                            pagesReadInt = pagesRead.trim().toInt()
                            if (pagesReadInt < 0) {
                                Toast.makeText(context, "Pages read cannot be negative", Toast.LENGTH_SHORT).show()
                                return@ElevatedButton
                            }

                            if (pagesReadInt > totalPagesInt) {
                                Toast.makeText(context, "Pages read cannot exceed total pages", Toast.LENGTH_SHORT).show()
                                return@ElevatedButton
                            }
                        } catch (e: NumberFormatException) {
                            Toast.makeText(context, "Please enter a valid number for pages read", Toast.LENGTH_SHORT).show()
                            return@ElevatedButton
                        }

                        // Format dates for the server
                        val formattedStartDate = serverDateFormatter.format(startDate)
                        val formattedEndDate = serverDateFormatter.format(endDate)

                        // Calculate pages per day
                        val pagesPerDay = try {
                            val diffDays = TimeUnit.MILLISECONDS.toDays(endDate.time - startDate.time)
                            val remainingPages = totalPagesInt - pagesReadInt
                            if (diffDays > 0) Math.ceil(remainingPages.toDouble() / diffDays).toInt() else 0
                        } catch (e: Exception) {
                            Log.e("AddBookScreen", "Error calculating pages per day", e)
                            0
                        }

                        // Submit the data
                        if (createReadingPlan && pagesPerDay > 0) {
                            bookViewModel.addBookWithReadingPlan(
                                title = title.trim(),
                                pagesRead = pagesReadInt,
                                totalPages = totalPagesInt,
                                startDate = formattedStartDate,
                                endDate = formattedEndDate,
                                notes = notes.ifBlank { null },
                                pagesPerDay = pagesPerDay
                            )
                        } else {
                            bookViewModel.addBook(
                                title = title.trim(),
                                pagesRead = pagesReadInt,
                                totalPages = totalPagesInt,
                                startDate = formattedStartDate,
                                endDate = formattedEndDate,
                                notes = notes.ifBlank { null }
                            )
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                enabled = addBookState !is AddBookState.Loading
            ) {
                if (addBookState is AddBookState.Loading) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        Icons.Rounded.AddCircle,
                        contentDescription = "Add",
                        modifier = Modifier.padding(end = 8.dp),
                        tint = Color.Black
                    )
                    Text("Add to Library", fontSize = 16.sp, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}