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
    private val repository = BookRepository()

    // State flows for observing book operations
    private val _booksState = MutableStateFlow<BookState>(BookState.Idle)
    val booksState: StateFlow<BookState> = _booksState.asStateFlow()

    private val _addBookState = MutableStateFlow<AddBookState>(AddBookState.Idle)
    val addBookState: StateFlow<AddBookState> = _addBookState.asStateFlow()

    private val _bookDetailState = MutableStateFlow<BookDetailState>(BookDetailState.Idle)
    val bookDetailState: StateFlow<BookDetailState> = _bookDetailState.asStateFlow()

    // Function to load all books
    fun loadBooks() {
        viewModelScope.launch {
            _booksState.value = BookState.Loading
            val result = repository.getAllBooks()
            _booksState.value = result.fold(
                onSuccess = { BookState.Success(it) },
                onFailure = { BookState.Error(it.message ?: "Failed to load books") }
            )
        }
    }

    // Function to load a specific book
    fun loadBook(id: Int) {
        viewModelScope.launch {
            _bookDetailState.value = BookDetailState.Loading
            val result = repository.getBookById(id)
            _bookDetailState.value = result.fold(
                onSuccess = { BookDetailState.Success(it) },
                onFailure = { BookDetailState.Error(it.message ?: "Failed to load book") }
            )
        }
    }

    // Function to add a new book - UPDATED to match server field names
    fun addBook(title: String, pagesRead: Int, startDate: String, endDate: String, notes: String?) {
        viewModelScope.launch {
            _addBookState.value = AddBookState.Loading

            // Changed from 'title' to 'bookName' to match server expectations
            val book = Book(
                title = title,  // This maps to 'bookName' in the server due to @SerializedName in Book data class
                pagesRead = pagesRead,
                startDate = startDate,
                endDate = endDate,
                notes = notes
            )

            val result = repository.addBook(book)
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