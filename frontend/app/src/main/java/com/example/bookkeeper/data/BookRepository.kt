package com.example.bookkeeper.data

import android.util.Log

// Book repository
class BookRepository(private val userId: String? = null) {
    private val api = ApiService.api
    private val TAG = "BookRepository"

    suspend fun getAllBooks(): Result<List<Book>> {
        return try {
            if (userId == null) {
                Log.e(TAG, "Cannot get books: userId is null")
                return Result.failure(Exception("User ID is required"))
            }

            val response = api.getHistory(userId)
            if (response.isSuccessful) {
                val historyResponse = response.body()
                Result.success(historyResponse?.books ?: emptyList())
            } else {
                Log.e(TAG, "Failed to fetch books: ${response.code()}")
                Result.failure(Exception("Failed to fetch books: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching books", e)
            Result.failure(e)
        }
    }

    suspend fun getBookById(id: Int): Result<Book> {
        // For now, we'll need to fetch all books and filter by id
        // since the server doesn't provide a direct endpoint for this
        return try {
            val booksResult = getAllBooks()
            if (booksResult.isSuccess) {
                val books = booksResult.getOrNull() ?: emptyList()
                val book = books.find { it.id == id }
                if (book != null) {
                    Result.success(book)
                } else {
                    Result.failure(Exception("Book not found"))
                }
            } else {
                Result.failure(Exception("Failed to fetch book"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addBook(book: Book, userId: String): Result<Book> {
        return try {
            val bookRequest = BookAddRequest(
                bookTitle = book.title,
                totalPages = book.totalPages ?: 0,
                userId = userId,
                pagesRead = book.pagesRead,
                startDate = book.startDate,
                endDate = book.endDate,
                notes = book.notes
            )

            val response = api.addBook(bookRequest)
            if (response.isSuccessful) {
                val addBookResponse = response.body()
                if (addBookResponse != null) {
                    Log.d(TAG, "Book added successfully: ${addBookResponse.message}")
                    Result.success(addBookResponse.book)
                } else {
                    Log.e(TAG, "Empty response when adding book")
                    Result.failure(Exception("Empty response when adding book"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error adding book: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to add book: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception adding book", e)
            Result.failure(e)
        }
    }

    suspend fun checkUser(userId: String): Result<UserCheckResponse> {
        return try {
            val request = UserCheckRequest(userId)
            val response = api.checkUser(request)

            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse != null) {
                    Result.success(userResponse)
                } else {
                    Result.failure(Exception("Empty response when checking user"))
                }
            } else {
                Result.failure(Exception("Failed to check user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}