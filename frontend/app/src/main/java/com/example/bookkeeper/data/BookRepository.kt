package com.example.bookkeeper.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Book repository with enhanced debugging
class BookRepository {
    private val api = ApiService.api
    private val TAG = "BookRepository"

    suspend fun getAllBooks(): Result<List<Book>> {
        return try {
            Log.d(TAG, "Fetching all books...")
            val response = api.getAllBooks()
            if (response.isSuccessful) {
                val books = response.body() ?: emptyList()
                Log.d(TAG, "Successfully fetched ${books.size} books")
                Result.success(books)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to fetch books: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch books: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching books", e)
            Result.failure(e)
        }
    }

    suspend fun getBookById(id: Int): Result<Book> {
        return try {
            Log.d(TAG, "Fetching book with ID: $id")
            val response = api.getBookById(id)
            if (response.isSuccessful) {
                val book = response.body()!!
                Log.d(TAG, "Successfully fetched book: ${book.title}")
                Result.success(book)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to fetch book: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch book: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching book", e)
            Result.failure(e)
        }
    }

    suspend fun addBook(book: Book): Result<Book> {
        return try {
            // Log the book data being sent
            Log.d(TAG, "Adding book: ${book.title}, Pages: ${book.pagesRead}, " +
                    "Start: ${book.startDate}, End: ${book.endDate}, " +
                    "Notes: ${book.notes ?: "No notes"}")

            // Print the raw JSON being sent
            val gson = com.google.gson.Gson()
            val jsonBody = gson.toJson(book)
            Log.d(TAG, "Request JSON: $jsonBody")

            val response = api.addBook(book)

            if (response.isSuccessful) {
                val newBook = response.body()?.newBook!!
                Log.d(TAG, "Successfully added book: ${newBook.title} with ID: ${newBook.id}")
                Result.success(newBook)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to add book: HTTP ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to add book: HTTP ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception adding book", e)
            Result.failure(e)
        }
    }

    suspend fun getBookHistory(): Result<String> {
        return try {
            Log.d(TAG, "Fetching book history...")
            val response = api.getBookHistory()
            if (response.isSuccessful) {
                val history = response.body() ?: "No history available"
                Log.d(TAG, "Successfully fetched book history")
                Result.success(history)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to fetch book history: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch book history: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching book history", e)
            Result.failure(e)
        }
    }

    // Test method to directly test the API connection
    suspend fun testApiConnection(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Try to hit the root endpoint
                val response = api.testConnection()
                if (response.isSuccessful) {
                    Log.d(TAG, "API connection successful")
                    Result.success("API connection successful: ${response.body()}")
                } else {
                    Log.e(TAG, "API connection failed: ${response.code()}")
                    Result.failure(Exception("API connection failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception testing API connection", e)
                Result.failure(e)
            }
        }
    }
}