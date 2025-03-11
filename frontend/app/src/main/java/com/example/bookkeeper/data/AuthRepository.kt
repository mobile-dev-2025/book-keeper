package com.example.bookkeeper.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository class for handling authentication operations.
 *
 * This implementation currently returns mock data, but it's structured
 * to be easily replaced with actual API calls when the backend is ready.
 */
class AuthRepository {
    // Reference to API service
    private val api = ApiService.api

    /**
     * Sign up a new user.
     * Currently returns mock data, will be replaced with actual API call.
     */
    suspend fun signUp(email: String, password: String): Result<AuthResponse> {
        // For now, we'll simulate a network call with a delay
        return withContext(Dispatchers.IO) {
            try {
                // Simulate network delay
                kotlinx.coroutines.delay(1000)

                // Mock successful response
                // When backend is ready, replace with:
                // val response = api.signUp(SignUpRequest(email, password))

                // Return mock data
                Result.success(
                    AuthResponse(
                        token = "mock-token-${System.currentTimeMillis()}",
                        userId = "user-id-${System.currentTimeMillis()}",
                        email = email
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Log in an existing user.
     * Currently returns mock data, will be replaced with actual API call.
     */
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Simulate network delay
                kotlinx.coroutines.delay(1000)

                // Basic validation (remove this when using real backend)
                if (email.isBlank() || password.isBlank()) {
                    return@withContext Result.failure(Exception("Email and password cannot be empty"))
                }

                // Mock authentication logic
                // This is just for testing - when backend is ready, replace with:
                // val response = api.login(LoginRequest(email, password))

                // Hardcoded test user - only allows this user to log in
                if (email == "test@example.com" && password == "password123") {
                    return@withContext Result.success(
                        AuthResponse(
                            token = "mock-token-${System.currentTimeMillis()}",
                            userId = "test-user-id",
                            email = email
                        )
                    )
                } else {
                    return@withContext Result.failure(Exception("Invalid credentials"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Save user credentials locally.
     * This would typically store the auth token in secure storage.
     */
    fun saveUserCredentials(token: String, userId: String) {
        // TODO: Implement using DataStore or SharedPreferences
        // For now, just logging for testing
        println("Saved credentials: Token=$token, UserId=$userId")
    }

    /**
     * Clear saved user credentials on logout.
     */
    fun clearUserCredentials() {
        // TODO: Implement using DataStore or SharedPreferences
        // For now, just logging for testing
        println("Cleared credentials")
    }
}