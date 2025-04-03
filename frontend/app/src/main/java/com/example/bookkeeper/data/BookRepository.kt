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
            // Log before creating request to check values
            Log.d(TAG, "Adding book - Title: ${book.title}")
            Log.d(TAG, "Total Pages: ${book.totalPages}")
            Log.d(TAG, "Pages Read: ${book.pagesRead}")
            Log.d(TAG, "Start Date: ${book.startDate}")
            Log.d(TAG, "End Date: ${book.endDate}")
            Log.d(TAG, "Notes: ${book.notes}")
            Log.d(TAG, "User ID: $userId")

            // Validate data before making the API call
            if (book.title.isBlank()) {
                return Result.failure(Exception("Book title cannot be empty"))
            }

            if (book.totalPages == null || book.totalPages <= 0) {
                return Result.failure(Exception("Total pages must be a positive number"))
            }

            if (book.pagesRead < 0) {
                return Result.failure(Exception("Pages read cannot be negative"))
            }

            if (book.pagesRead > book.totalPages) {
                return Result.failure(Exception("Pages read cannot exceed total pages"))
            }

            val bookRequest = BookAddRequest(
                bookTitle = book.title,
                totalPages = book.totalPages,
                userId = userId,
                pagesRead = book.pagesRead,
                startDate = book.startDate,
                endDate = book.endDate,
                notes = book.notes
            )

            Log.d(TAG, "Sending request: $bookRequest")

            val response = api.addBook(bookRequest)
            Log.d(TAG, "Add book API response code: ${response.code()}")

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

                // Check for specific errors
                when {
                    errorBody.contains("already exists") -> {
                        Result.failure(Exception("A book with this title already exists in your library"))
                    }
                    errorBody.contains("required") -> {
                        Result.failure(Exception("Missing required fields: $errorBody"))
                    }
                    else -> {
                        Result.failure(Exception("Failed to add book: ${response.code()} - $errorBody"))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception adding book", e)
            Result.failure(e)
        }
    }

    // Create a reading plan for a book with enhanced error handling
    suspend fun createReadingPlan(bookTitle: String, pagesPerDay: Int, userId: String): Result<ReadingPlanResponse> {
        return try {
            // Validations
            if (bookTitle.isBlank()) {
                return Result.failure(Exception("Book title cannot be empty"))
            }

            if (pagesPerDay <= 0) {
                return Result.failure(Exception("Pages per day must be positive"))
            }

            val readingPlanRequest = ReadingPlanRequest(
                bookTitle = bookTitle,
                pagesPerDay = pagesPerDay,
                userId = userId
            )

            // Log the request payload
            Log.d(TAG, "Creating reading plan with: bookTitle=$bookTitle, pagesPerDay=$pagesPerDay, userId=$userId")

            val response = api.createReadingPlan(readingPlanRequest)

            // Log the raw response
            Log.d(TAG, "Reading plan API response code: ${response.code()}")

            if (response.isSuccessful) {
                val readingPlanResponse = response.body()
                if (readingPlanResponse != null) {
                    Log.d(TAG, "Reading plan created successfully: ${readingPlanResponse.message}")
                    Result.success(readingPlanResponse)
                } else {
                    Log.e(TAG, "Empty response when creating reading plan")
                    Result.failure(Exception("Empty response when creating reading plan"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error creating reading plan: ${response.code()} - $errorBody")

                // Check for specific errors
                when {
                    errorBody.contains("not found") -> {
                        Result.failure(Exception("Book not found. Make sure to add the book first."))
                    }
                    errorBody.contains("required") -> {
                        Result.failure(Exception("Missing required fields: $errorBody"))
                    }
                    else -> {
                        Result.failure(Exception("Failed to create reading plan: ${response.code()} - $errorBody"))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating reading plan", e)
            Result.failure(e)
        }
    }

    /**
     * Updates the reading progress of a book
     */
    suspend fun updateBookProgress(bookTitle: String, userId: String, currentPage: Int): Result<Book> {
        return try {
            if (userId.isBlank() || bookTitle.isBlank()) {
                Log.e(TAG, "Cannot update book progress: userId or bookTitle is null/blank")
                return Result.failure(Exception("User ID and Book Title are required"))
            }

            Log.d(TAG, "Updating book progress - Title: $bookTitle, currentPage: $currentPage")

            val updateRequest = UpdateBookProgressRequest(
                userId = userId,
                bookTitle = bookTitle,
                currentPage = currentPage
            )

            val response = api.updateBookProgress(updateRequest)
            Log.d(TAG, "Update book progress API response code: ${response.code()}")

            if (response.isSuccessful) {
                val updateResponse = response.body()
                if (updateResponse != null) {
                    Log.d(TAG, "Book progress updated successfully: ${updateResponse.message}")
                    Result.success(updateResponse.updatedBook)
                } else {
                    Log.e(TAG, "Empty response when updating book progress")
                    Result.failure(Exception("Empty response when updating book progress"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error updating book progress: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to update book progress: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception updating book progress", e)
            Result.failure(e)
        }
    }

    /**
     * Marks a book as finished
     */
    suspend fun markBookAsFinished(bookTitle: String, userId: String): Result<Book> {
        return try {
            if (userId.isBlank() || bookTitle.isBlank()) {
                Log.e(TAG, "Cannot mark book as finished: userId or bookTitle is null/blank")
                return Result.failure(Exception("User ID and Book Title are required"))
            }

            Log.d(TAG, "Marking book as finished - Title: $bookTitle")

            val finishRequest = FinishBookRequest(
                bookTitle = bookTitle,
                userId = userId
            )

            val response = api.markBookAsFinished(finishRequest)
            Log.d(TAG, "Mark book as finished API response code: ${response.code()}")

            if (response.isSuccessful) {
                val finishResponse = response.body()
                if (finishResponse != null) {
                    Log.d(TAG, "Book marked as finished successfully: ${finishResponse.message}")
                    Result.success(finishResponse.book)
                } else {
                    Log.e(TAG, "Empty response when marking book as finished")
                    Result.failure(Exception("Empty response when marking book as finished"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error marking book as finished: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to mark book as finished: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception marking book as finished", e)
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