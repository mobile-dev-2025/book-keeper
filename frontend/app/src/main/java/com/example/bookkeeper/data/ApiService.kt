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
import retrofit2.http.Path

// Data classes for auth request and response
data class SignUpRequest(
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val userId: String,
    val email: String
)

// Data classes for Book
data class Book(
    val id: Int? = null,
    @SerializedName("bookName") val title: String,
    val pagesRead: Int,
    val startDate: String,
    val endDate: String,
    val notes: String? = null
)

// Response wrapper for adding a book
data class AddBookResponse(
    val newBook: Book
)

// Data class for SubId
data class SubIdRequest(
    val subId: String
)

// Combined API interface
interface BookKeeperApi {
    // Auth endpoints
    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // Book endpoints
    @GET("currentBook")
    suspend fun getAllBooks(): Response<List<Book>>

    @GET("currentBook/{id}")
    suspend fun getBookById(@Path("id") id: Int): Response<Book>

    @POST("addBook")
    suspend fun addBook(@Body book: Book): Response<AddBookResponse>

    @GET("history")
    suspend fun getBookHistory(): Response<String>

    // Endpoint to send subId to the server
    @POST("user/subId")
    suspend fun sendSubId(@Body subIdRequest: SubIdRequest): Response<Void>
}

// API service singleton
object ApiService {
    // Updated BASE_URL to localhost
    private const val BASE_URL = "http://192.168.0.18:8000/" // Replace with your local server IP

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
