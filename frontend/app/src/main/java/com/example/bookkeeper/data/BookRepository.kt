package com.example.bookkeeper.data

import android.util.Log

// Book repository
class BookRepository {
    private val api = ApiService.api

    suspend fun getAllBooks(): Result<List<Book>> {
        return try {
            val response = api.getAllBooks()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch books: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBookById(id: Int): Result<Book> {
        return try {
            val response = api.getBookById(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch book: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addBook(book: Book): Result<Book> {
        return try {
            val response = api.addBook(book)
            if (response.isSuccessful) {
                Result.success(response.body()?.newBook!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("BookRepository", "Error adding book: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to add book: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("BookRepository", "Exception adding book", e)
            Result.failure(e)
        }
    }

    suspend fun getBookHistory(): Result<String> {
        return try {
            val response = api.getBookHistory()
            if (response.isSuccessful) {
                Result.success(response.body() ?: "No history available")
            } else {
                Result.failure(Exception("Failed to fetch book history: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}