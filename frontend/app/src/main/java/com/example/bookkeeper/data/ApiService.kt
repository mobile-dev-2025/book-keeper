package com.example.bookkeeper.data

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

// Data classes for Book
data class Book(
    val id: Int? = null,
    @SerializedName("bookTitle") val title: String,
    val totalPages: Int? = null,
    val pagesRead: Int,
    val startDate: String,
    val endDate: String,
    val notes: String? = null
)

// Response wrapper for adding a book
data class AddBookResponse(
    val message: String,
    val book: Book
)

// History response
data class HistoryResponse(
    val message: String,
    val books: List<Book>
)

// User check request/response
data class UserCheckRequest(
    val userId: String
)

data class UserCheckResponse(
    val isNewUser: Boolean,
    val userId: String
)

// Request model for adding a book with proper toString() for debugging
data class BookAddRequest(
    val bookTitle: String,
    val totalPages: Int,
    val userId: String,
    val pagesRead: Int,
    val startDate: String,
    val endDate: String,
    val notes: String? = null
) {
    override fun toString(): String {
        return "BookAddRequest(bookTitle='$bookTitle', totalPages=$totalPages, userId='$userId', " +
                "pagesRead=$pagesRead, startDate='$startDate', endDate='$endDate', notes=$notes)"
    }
}

// Reading plan request/response models
data class ReadingPlanRequest(
    val bookTitle: String,
    val userId: String,
    val pagesPerDay: Int
) {
    override fun toString(): String {
        return "ReadingPlanRequest(bookTitle='$bookTitle', userId='$userId', pagesPerDay=$pagesPerDay)"
    }
}

data class ReadingPlan(
    val bookTitle: String,
    val userId: String,
    val totalPages: Int,
    val pagesRead: Int,
    val pagesPerDay: Int,
    val startDate: String,
    val endDate: String,
    val estimatedDays: Int,
    val createdAt: String
)

data class ReadingPlanResponse(
    val message: String,
    val readingPlan: ReadingPlan
)

// Response for reading plans list
data class ReadingPlansResponse(
    val message: String,
    val readingPlans: List<ReadingPlan>
)

// Update book progress models
data class UpdateBookProgressRequest(
    val userId: String,
    val bookTitle: String,
    val currentPage: Int,
    val notes: String? = null
)

data class UpdateBookResponse(
    val message: String,
    val updatedBook: Book
)

// Finish book models
data class FinishBookRequest(
    val bookTitle: String,
    val userId: String
)

data class FinishBookResponse(
    val message: String,
    val book: Book
)

// Reading stats models
data class ReadingStats(
    val date: String,
    val plan: Int,
    val actual: Int,
    val bonus: Int
)

data class ReadingStatsResponse(
    val message: String,
    val stats: List<ReadingStats>
)

// Updated API interface
interface BookKeeperApi {
    // User check endpoint
    @POST("checkUser")
    suspend fun checkUser(@Body request: UserCheckRequest): Response<UserCheckResponse>

    // Book endpoints
    @GET("history")
    suspend fun getHistory(@Query("userId") userId: String): Response<HistoryResponse>

    @POST("addBook")
    suspend fun addBook(@Body book: BookAddRequest): Response<AddBookResponse>

    // Reading plan endpoints
    @POST("readingPlans")
    suspend fun createReadingPlan(@Body readingPlan: ReadingPlanRequest): Response<ReadingPlanResponse>

    @GET("readingPlans")
    suspend fun getReadingPlans(@Query("userId") userId: String): Response<ReadingPlansResponse>

    // Update book progress endpoint
    @PUT("currentBook")
    suspend fun updateBookProgress(@Body request: UpdateBookProgressRequest): Response<UpdateBookResponse>

    // Mark book as finished endpoint
    @POST("finishedBook")
    suspend fun markBookAsFinished(@Body request: FinishBookRequest): Response<FinishBookResponse>

    // Reading stats endpoint
    @GET("readingStats")
    suspend fun getReadingStats(
        @Query("userId") userId: String,
        @Query("bookTitle") bookTitle: String
    ): Response<ReadingStatsResponse>
}

// API service singleton
object ApiService {
    // Updated BASE_URL
    private const val BASE_URL = "https://book-keeper-h3ha.onrender.com/" // Replace with your server URL if needed

    // Create logging interceptor for debugging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Create HTTP client with logging
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Create Retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Create API interface implementation
    val api: BookKeeperApi = retrofit.create(BookKeeperApi::class.java)
}