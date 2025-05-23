package com.example.bookkeeper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookkeeper.data.Book
import com.example.bookkeeper.data.BookRepository
import com.example.bookkeeper.data.ReadingPlan
import com.example.bookkeeper.data.ReadingStats
import com.example.bookkeeper.data.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log

// States for book operations
sealed class BookState {
    object Idle : BookState()
    object Loading : BookState()
    data class Success(val books: List<Book>) : BookState()
    data class Error(val message: String) : BookState()
}

sealed class AddBookState {
    object Idle : AddBookState()
    object Loading : AddBookState()
    data class Success(val book: Book) : AddBookState()
    data class Error(val message: String) : AddBookState()
}

sealed class ReadingPlanState {
    object Idle : ReadingPlanState()
    object Loading : ReadingPlanState()
    data class Success(val readingPlan: ReadingPlan) : ReadingPlanState()
    data class Error(val message: String) : ReadingPlanState()
}

sealed class ReadingPlansState {
    object Idle : ReadingPlansState()
    object Loading : ReadingPlansState()
    data class Success(val readingPlans: List<ReadingPlan>) : ReadingPlansState()
    data class Error(val message: String) : ReadingPlansState()
}

sealed class BookDetailState {
    object Idle : BookDetailState()
    object Loading : BookDetailState()
    data class Success(val book: Book) : BookDetailState()
    data class Error(val message: String) : BookDetailState()
}

sealed class UpdateBookState {
    object Idle : UpdateBookState()
    object Loading : UpdateBookState()
    data class Success(val book: Book) : UpdateBookState()
    data class Error(val message: String) : UpdateBookState()
}

sealed class ReadingStatsState {
    object Idle : ReadingStatsState()
    object Loading : ReadingStatsState()
    data class Success(val stats: List<ReadingStats>) : ReadingStatsState()
    data class Error(val message: String) : ReadingStatsState()
}

class BookViewModel : ViewModel() {
    private val TAG = "BookViewModel"
    private var repository: BookRepository? = null
    private var currentUserId: String? = null

    // State flows for observing book operations
    private val _booksState = MutableStateFlow<BookState>(BookState.Idle)
    val booksState: StateFlow<BookState> = _booksState.asStateFlow()

    private val _addBookState = MutableStateFlow<AddBookState>(AddBookState.Idle)
    val addBookState: StateFlow<AddBookState> = _addBookState.asStateFlow()

    private val _bookDetailState = MutableStateFlow<BookDetailState>(BookDetailState.Idle)
    val bookDetailState: StateFlow<BookDetailState> = _bookDetailState.asStateFlow()

    // Reading plan state
    private val _readingPlanState = MutableStateFlow<ReadingPlanState>(ReadingPlanState.Idle)
    val readingPlanState: StateFlow<ReadingPlanState> = _readingPlanState.asStateFlow()

    // Reading plans list state
    private val _readingPlansState = MutableStateFlow<ReadingPlansState>(ReadingPlansState.Idle)
    val readingPlansState: StateFlow<ReadingPlansState> = _readingPlansState.asStateFlow()

    // Update book state
    private val _updateBookState = MutableStateFlow<UpdateBookState>(UpdateBookState.Idle)
    val updateBookState: StateFlow<UpdateBookState> = _updateBookState.asStateFlow()

    // Reading stats state
    private val _readingStatsState = MutableStateFlow<ReadingStatsState>(ReadingStatsState.Idle)
    val readingStatsState: StateFlow<ReadingStatsState> = _readingStatsState.asStateFlow()

    // Initialize repository with user ID
    fun initializeWithUser(userId: String) {
        currentUserId = userId
        repository = BookRepository(userId)
        Log.d(TAG, "Repository initialized with user ID: $userId")
    }

    // Function to load all books
    fun loadBooks() {
        if (repository == null || currentUserId == null) {
            _booksState.value = BookState.Error("User not initialized")
            return
        }

        viewModelScope.launch {
            _booksState.value = BookState.Loading
            val result = repository!!.getAllBooks()
            _booksState.value = result.fold(
                onSuccess = { BookState.Success(it) },
                onFailure = { BookState.Error(it.message ?: "Failed to load books") }
            )
        }
    }

    // Function to load all reading plans
    fun loadReadingPlans() {
        if (repository == null || currentUserId == null) {
            _readingPlansState.value = ReadingPlansState.Error("User not initialized")
            return
        }

        viewModelScope.launch {
            _readingPlansState.value = ReadingPlansState.Loading

            try {
                val response = ApiService.api.getReadingPlans(currentUserId!!)

                if (response.isSuccessful) {
                    val readingPlansResponse = response.body()
                    if (readingPlansResponse != null) {
                        _readingPlansState.value = ReadingPlansState.Success(readingPlansResponse.readingPlans)
                        Log.d(TAG, "Reading plans loaded: ${readingPlansResponse.readingPlans.size}")
                    } else {
                        _readingPlansState.value = ReadingPlansState.Error("Empty response when loading reading plans")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _readingPlansState.value = ReadingPlansState.Error("Failed to load reading plans: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading reading plans", e)
                _readingPlansState.value = ReadingPlansState.Error(e.message ?: "Error loading reading plans")
            }
        }
    }

    // Function to load reading stats for a specific book
    fun loadReadingStats(bookTitle: String) {
        if (repository == null || currentUserId == null) {
            _readingStatsState.value = ReadingStatsState.Error("User not initialized")
            return
        }

        viewModelScope.launch {
            _readingStatsState.value = ReadingStatsState.Loading
            Log.d(TAG, "Loading reading stats for book: $bookTitle")

            val result = repository!!.getReadingStats(bookTitle, currentUserId!!)
            _readingStatsState.value = result.fold(
                onSuccess = {
                    Log.d(TAG, "Reading stats loaded successfully: ${it.size} entries")
                    ReadingStatsState.Success(it)
                },
                onFailure = {
                    Log.e(TAG, "Failed to load reading stats: ${it.message}")
                    ReadingStatsState.Error(it.message ?: "Failed to load reading stats")
                }
            )
        }
    }

    // Function to load a specific book
    fun loadBook(id: Int) {
        if (repository == null) {
            _bookDetailState.value = BookDetailState.Error("User not initialized")
            return
        }

        viewModelScope.launch {
            _bookDetailState.value = BookDetailState.Loading
            val result = repository!!.getBookById(id)
            _bookDetailState.value = result.fold(
                onSuccess = { BookDetailState.Success(it) },
                onFailure = { BookDetailState.Error(it.message ?: "Failed to load book") }
            )
        }
    }

    // Function to add a new book with enhanced error handling
    fun addBook(title: String, pagesRead: Int, startDate: String, endDate: String, notes: String?, totalPages: Int) {
        if (repository == null || currentUserId == null) {
            _addBookState.value = AddBookState.Error("User not initialized")
            return
        }

        viewModelScope.launch {
            _addBookState.value = AddBookState.Loading
            Log.d(TAG, "Adding book without reading plan: $title")
            Log.d(TAG, "Book details: totalPages=$totalPages, pagesRead=$pagesRead")
            Log.d(TAG, "Dates: startDate=$startDate, endDate=$endDate")

            // Validate input
            try {
                if (title.isBlank()) {
                    throw IllegalArgumentException("Book title cannot be empty")
                }

                if (totalPages <= 0) {
                    throw IllegalArgumentException("Total pages must be positive")
                }

                if (pagesRead < 0) {
                    throw IllegalArgumentException("Pages read cannot be negative")
                }

                if (pagesRead > totalPages) {
                    throw IllegalArgumentException("Pages read cannot exceed total pages")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Validation failed", e)
                _addBookState.value = AddBookState.Error(e.message ?: "Invalid input")
                return@launch
            }

            // Create book object
            val book = Book(
                title = title,
                pagesRead = pagesRead,
                totalPages = totalPages,
                startDate = startDate,
                endDate = endDate,
                notes = notes
            )

            val result = repository!!.addBook(book, currentUserId!!)
            _addBookState.value = result.fold(
                onSuccess = {
                    Log.d(TAG, "Book added successfully with ID: ${it.id}")
                    AddBookState.Success(it)
                },
                onFailure = {
                    Log.e(TAG, "Failed to add book: ${it.message}")
                    AddBookState.Error(it.message ?: "Failed to add book")
                }
            )

            // Reload books after adding a new one
            loadBooks()
        }
    }

    // Function to create a reading plan
    fun createReadingPlan(bookTitle: String, pagesPerDay: Int) {
        if (repository == null || currentUserId == null) {
            _readingPlanState.value = ReadingPlanState.Error("User not initialized")
            return
        }

        viewModelScope.launch {
            _readingPlanState.value = ReadingPlanState.Loading
            Log.d(TAG, "Creating reading plan only: $bookTitle, $pagesPerDay pages per day")

            val result = repository!!.createReadingPlan(bookTitle, pagesPerDay, currentUserId!!)
            _readingPlanState.value = result.fold(
                onSuccess = {
                    Log.d(TAG, "Reading plan created successfully")
                    ReadingPlanState.Success(it.readingPlan)
                },
                onFailure = {
                    Log.e(TAG, "Failed to create reading plan: ${it.message}")
                    ReadingPlanState.Error(it.message ?: "Failed to create reading plan")
                }
            )

            // Reload reading plans after creating a new one
            loadReadingPlans()
        }
    }

    // Add book and create reading plan in one operation with enhanced error handling
    fun addBookWithReadingPlan(
        title: String,
        pagesRead: Int,
        startDate: String,
        endDate: String,
        notes: String?,
        totalPages: Int,
        pagesPerDay: Int
    ) {
        if (repository == null || currentUserId == null) {
            _addBookState.value = AddBookState.Error("User not initialized")
            return
        }

        viewModelScope.launch {
            _addBookState.value = AddBookState.Loading

            // Debug log
            Log.d(TAG, "Starting addBookWithReadingPlan - title: $title, pagesPerDay: $pagesPerDay")
            Log.d(TAG, "Dates - startDate: $startDate, endDate: $endDate")
            Log.d(TAG, "Page info - totalPages: $totalPages, pagesRead: $pagesRead")

            // Validate input
            try {
                if (title.isBlank()) {
                    throw IllegalArgumentException("Book title cannot be empty")
                }

                if (totalPages <= 0) {
                    throw IllegalArgumentException("Total pages must be positive")
                }

                if (pagesRead < 0) {
                    throw IllegalArgumentException("Pages read cannot be negative")
                }

                if (pagesRead > totalPages) {
                    throw IllegalArgumentException("Pages read cannot exceed total pages")
                }

                if (pagesPerDay <= 0) {
                    throw IllegalArgumentException("Pages per day must be positive")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Validation failed", e)
                _addBookState.value = AddBookState.Error(e.message ?: "Invalid input")
                return@launch
            }

            // First add the book
            val book = Book(
                title = title,
                pagesRead = pagesRead,
                totalPages = totalPages,
                startDate = startDate,
                endDate = endDate,
                notes = notes
            )

            val bookResult = repository!!.addBook(book, currentUserId!!)

            val addBookState = bookResult.fold(
                onSuccess = {
                    Log.d(TAG, "Book added successfully, now creating reading plan")
                    AddBookState.Success(it)
                },
                onFailure = {
                    Log.e(TAG, "Failed to add book: ${it.message}")
                    AddBookState.Error(it.message ?: "Failed to add book")
                }
            )

            _addBookState.value = addBookState

            // If book was added successfully, create reading plan
            if (bookResult.isSuccess) {
                _readingPlanState.value = ReadingPlanState.Loading

                // Debug log
                Log.d(TAG, "Book added, now creating reading plan with pagesPerDay: $pagesPerDay")

                val planResult = repository!!.createReadingPlan(title, pagesPerDay, currentUserId!!)

                val readingPlanState = planResult.fold(
                    onSuccess = {
                        Log.d(TAG, "Reading plan created successfully")
                        ReadingPlanState.Success(it.readingPlan)
                    },
                    onFailure = {
                        Log.e(TAG, "Failed to create reading plan: ${it.message}")
                        ReadingPlanState.Error(it.message ?: "Failed to create reading plan")
                    }
                )

                _readingPlanState.value = readingPlanState
            }

            // Reload books and reading plans after adding a new one
            loadBooks()
            loadReadingPlans()
        }
    }

    /**
     * Updates a book's reading progress in the database using the specific API endpoint
     */
    fun updateBookProgress(bookId: Int, title: String, currentPage: Int, totalPages: Int) {
        if (repository == null || currentUserId == null) {
            _updateBookState.value = UpdateBookState.Error("User not initialized")
            return
        }

        viewModelScope.launch {
            _updateBookState.value = UpdateBookState.Loading
            Log.d(TAG, "Updating book progress for '$title': currentPage=$currentPage")

            try {
                // Validate input
                if (title.isBlank()) {
                    throw IllegalArgumentException("Book title cannot be empty")
                }

                if (currentPage < 0) {
                    throw IllegalArgumentException("Current page cannot be negative")
                }

                if (currentPage > totalPages) {
                    throw IllegalArgumentException("Current page cannot exceed total pages")
                }

                // Call the specific API endpoint for updating book progress
                val result = repository!!.updateBookProgress(
                    bookTitle = title,
                    userId = currentUserId!!,
                    currentPage = currentPage
                )

                _updateBookState.value = result.fold(
                    onSuccess = {
                        Log.d(TAG, "Book progress updated successfully")
                        UpdateBookState.Success(it)
                    },
                    onFailure = {
                        Log.e(TAG, "Failed to update book progress: ${it.message}")
                        UpdateBookState.Error(it.message ?: "Failed to update book progress")
                    }
                )

                // Reload books to refresh the data
                loadBooks()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating book progress", e)
                _updateBookState.value = UpdateBookState.Error(e.message ?: "Error updating progress")
            }
        }
    }

    /**
     * Marks a book as finished using the specific API endpoint
     */
    fun markBookAsFinished(bookId: Int, title: String, totalPages: Int) {
        if (repository == null || currentUserId == null) {
            _updateBookState.value = UpdateBookState.Error("User not initialized")
            return
        }

        viewModelScope.launch {
            _updateBookState.value = UpdateBookState.Loading
            Log.d(TAG, "Marking book '$title' as finished")

            try {
                if (title.isBlank()) {
                    throw IllegalArgumentException("Book title cannot be empty")
                }

                // Call the specific API endpoint for marking a book as finished
                val result = repository!!.markBookAsFinished(
                    bookTitle = title,
                    userId = currentUserId!!
                )

                _updateBookState.value = result.fold(
                    onSuccess = {
                        Log.d(TAG, "Book marked as finished successfully")
                        UpdateBookState.Success(it)
                    },
                    onFailure = {
                        Log.e(TAG, "Failed to mark book as finished: ${it.message}")
                        UpdateBookState.Error(it.message ?: "Failed to mark book as finished")
                    }
                )

                // Reload books to refresh the data
                loadBooks()
            } catch (e: Exception) {
                Log.e(TAG, "Error marking book as finished", e)
                _updateBookState.value = UpdateBookState.Error(e.message ?: "Error marking as finished")
            }
        }
    }

    // Helper function to format dates from Date object to String
    fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(date)
    }

    // Check if user exists or needs to be created
    fun checkUser(userId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val tempRepository = BookRepository()
            val result = tempRepository.checkUser(userId)

            result.fold(
                onSuccess = { response ->
                    // Initialize repository with the confirmed user ID
                    initializeWithUser(response.userId)
                    onComplete(response.isNewUser)
                },
                onFailure = {
                    Log.e(TAG, "Failed to check user: ${it.message}")
                    // Initialize anyway to prevent app from breaking
                    initializeWithUser(userId)
                    onComplete(false)
                }
            )
        }
    }


    fun resetAddBookState() {
        _addBookState.value = AddBookState.Idle
    }

    fun resetBookDetailState() {
        _bookDetailState.value = BookDetailState.Idle
    }

    fun resetBooksState() {
        _booksState.value = BookState.Idle
    }

    fun resetReadingPlanState() {
        _readingPlanState.value = ReadingPlanState.Idle
    }

    fun resetReadingPlansState() {
        _readingPlansState.value = ReadingPlansState.Idle
    }

    fun resetUpdateBookState() {
        _updateBookState.value = UpdateBookState.Idle
    }

    fun resetReadingStatsState() {
        _readingStatsState.value = ReadingStatsState.Idle
    }
}