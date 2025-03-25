package com.example.bookkeeper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookkeeper.data.Book
import com.example.bookkeeper.data.BookRepository
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

    // Function to add a new book - Updated to match server expectations
    fun addBook(title: String, pagesRead: Int, startDate: String, endDate: String, notes: String?, totalPages: Int = 0) {
        if (repository == null || currentUserId == null) {
            _addBookState.value = AddBookState.Error("User not initialized")
            return
        }

        viewModelScope.launch {
            _addBookState.value = AddBookState.Loading

            // Create book object matching server expectations
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
                onSuccess = { AddBookState.Success(it) },
                onFailure = { AddBookState.Error(it.message ?: "Failed to add book") }
            )

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
}