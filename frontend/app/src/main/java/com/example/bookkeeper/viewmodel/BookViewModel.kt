package com.example.bookkeeper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookkeeper.data.Book
import com.example.bookkeeper.data.BookRepository
import com.example.bookkeeper.data.ReadingPlan
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

sealed class BookDetailState {
    object Idle : BookDetailState()
    object Loading : BookDetailState()
    data class Success(val book: Book) : BookDetailState()
    data class Error(val message: String) : BookDetailState()
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

            // Reload books after adding a new one
            loadBooks()
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

    // Reset states
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
}