package com.example.bookkeeper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Authentication states to represent different stages of auth process
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Simple User data class to represent an authenticated user
 */
data class User(
    val id: String,
    val email: String,
    val name: String = "Book Keeper User"
)

/**
 * ViewModel for handling authentication locally.
 * This provides mock authentication functionality for testing
 * before connecting to the real backend.
 */
class LocalAuthViewModel : ViewModel() {
    // In-memory map of registered users for testing
    private val users = mutableMapOf<String, String>() // email -> password

    // Current authenticated user
    private var currentUser: User? = null

    // State flows to observe authentication states
    private val _signUpState = MutableStateFlow<AuthState>(AuthState.Idle)
    val signUpState: StateFlow<AuthState> = _signUpState.asStateFlow()

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    // Add a test user on initialization
    init {
        // Add a test user (email: test@example.com, password: password123)
        users["test@example.com"] = "password123"
    }

    /**
     * Sign up function to register a new user
     */
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            // Set state to loading
            _signUpState.value = AuthState.Loading

            // Add a small delay to simulate network request
            delay(1000)

            // Simple validation
            if (email.isBlank() || password.isBlank()) {
                _signUpState.value = AuthState.Error("Email and password cannot be empty")
                return@launch
            }

            // Basic email validation
            if (!email.contains("@")) {
                _signUpState.value = AuthState.Error("Please enter a valid email address")
                return@launch
            }

            // Check if user already exists
            if (users.containsKey(email)) {
                _signUpState.value = AuthState.Error("User with this email already exists")
                return@launch
            }

            // Password length validation
            if (password.length < 6) {
                _signUpState.value = AuthState.Error("Password must be at least 6 characters")
                return@launch
            }

            // Register the new user
            users[email] = password

            // Create user and update state
            val newUser = User(
                id = generateUserId(),
                email = email
            )
            currentUser = newUser

            // Update state to success
            _signUpState.value = AuthState.Success(newUser)
        }
    }

    /**
     * Login function to authenticate an existing user
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            // Set state to loading
            _loginState.value = AuthState.Loading

            // Add a small delay to simulate network request
            delay(1000)

            // Simple validation
            if (email.isBlank() || password.isBlank()) {
                _loginState.value = AuthState.Error("Email and password cannot be empty")
                return@launch
            }

            // Check if user exists and password matches
            if (!users.containsKey(email)) {
                _loginState.value = AuthState.Error("User not found")
                return@launch
            }

            if (users[email] != password) {
                _loginState.value = AuthState.Error("Incorrect password")
                return@launch
            }

            // Create user and update state
            val loggedInUser = User(
                id = generateUserId(),
                email = email
            )
            currentUser = loggedInUser

            // Update state to success
            _loginState.value = AuthState.Success(loggedInUser)
        }
    }

    /**
     * Log out function
     */
    fun logout() {
        currentUser = null
        _loginState.value = AuthState.Idle
        _signUpState.value = AuthState.Idle
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return currentUser != null
    }

    /**
     * Get current user
     */
    fun getCurrentUser(): User? {
        return currentUser
    }

    /**
     * Generate a simple user ID
     */
    private fun generateUserId(): String {
        return "user_${System.currentTimeMillis()}"
    }

    /**
     * Reset error states
     */
    fun resetAuthStates() {
        _loginState.value = AuthState.Idle
        _signUpState.value = AuthState.Idle
    }
}