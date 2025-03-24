package com.example.bookkeeper.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import com.example.bookkeeper.data.ApiService
import com.example.bookkeeper.data.UserCheckRequest
import com.example.bookkeeper.data.UserCheckResponse
import com.auth0.android.authentication.storage.CredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import java.util.Date

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
 * User data class to represent an authenticated user
 */
data class User(
    val id: String,
    val email: String,
    val name: String = "Book Keeper User"
)

/**
 * ViewModel for handling authentication with Auth0.
 * Supports both web-based (email/password) and Google authentication.
 * Maintains authentication state across app restarts.
 */
class Auth0ViewModel : ViewModel() {
    private val TAG = "Auth0ViewModel"

    // Auth0 client instance
    private lateinit var auth0: Auth0

    // Store credentials in memory
    private var credentials: Credentials? = null
    private var userProfile: UserProfile? = null

    // Auth0 credentials manager
    private lateinit var credentialsManager: CredentialsManager

    // SharedPreferences for storing user info
    private lateinit var prefs: SharedPreferences
    private val PREFS_NAME = "Auth0UserPrefs"
    private val KEY_USER = "user_info"

    // State flows to observe authentication states
    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState

    // StateFlow for sign-up state
    private val _signUpState = MutableStateFlow<AuthState>(AuthState.Idle)
    val signUpState: StateFlow<AuthState> = _signUpState

    // Current authenticated user
    private var currentUser: User? = null

    // Track which flow (login or signup) is currently active
    private var isSignupFlow = false

    // Client constants
    private val CLIENT_ID = "jlD62JzB1GY9lgjUeDUOUUMdgaFTJ44z"
    private val DOMAIN = "dev-6wa8fjdrx1cprn50.us.auth0.com"
    private val SCHEME = "demo"

    /**
     * Initialize Auth0 client with your application context
     */
    fun initialize(context: Context) {
        try {
            auth0 = Auth0(CLIENT_ID, DOMAIN)
            val packageName = context.packageName
            Log.d(TAG, "App package name: $packageName")
            Log.d(TAG, "Auth0 initialized with Client ID: $CLIENT_ID and Domain: $DOMAIN")

            // Initialize CredentialsManager
            val storage = SharedPreferencesStorage(context)
            credentialsManager = CredentialsManager(AuthenticationAPIClient(auth0), storage)

            // Initialize SharedPreferences for user info
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            // Try to restore credentials and user information
            checkPreviousSession()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Auth0", e)
        }
    }

    /**
     * Check if there's a previous valid session
     */
    private fun checkPreviousSession() {
        if (credentialsManager.hasValidCredentials()) {
            Log.d(TAG, "Found valid credentials")
            _loginState.value = AuthState.Loading

            // Retrieve credentials
            credentialsManager.getCredentials(object : Callback<Credentials, CredentialsManagerException> {
                override fun onSuccess(result: Credentials) {
                    Log.d(TAG, "Retrieved credentials successfully")
                    credentials = result

                    // Try to get stored user info
                    val storedUserJson = prefs.getString(KEY_USER, null)
                    if (storedUserJson != null) {
                        try {
                            // Parse user info from JSON
                            val parts = storedUserJson.split("|||")
                            if (parts.size >= 3) {
                                val user = User(
                                    id = parts[0],
                                    email = parts[1],
                                    name = parts[2]
                                )
                                currentUser = user
                                _loginState.value = AuthState.Success(user)
                                Log.d(TAG, "User info restored from preferences")
                            } else {
                                // If user info is invalid, get profile from API
                                getUserProfile(result.accessToken)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing stored user info", e)
                            getUserProfile(result.accessToken)
                        }
                    } else {
                        // No stored user info, get from API
                        getUserProfile(result.accessToken)
                    }
                }

                override fun onFailure(error: CredentialsManagerException) {
                    Log.e(TAG, "Failed to retrieve stored credentials", error)
                    _loginState.value = AuthState.Idle
                }
            })
        } else {
            Log.d(TAG, "No valid credentials found")
            _loginState.value = AuthState.Idle
        }
    }

    /**
     * Store user information in SharedPreferences
     */
    private fun storeUserInfo(user: User) {
        try {
            // Store as simple string to avoid Gson dependency
            val userString = "${user.id}|||${user.email}|||${user.name}"
            prefs.edit().putString(KEY_USER, userString).apply()
            Log.d(TAG, "User information stored successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store user information", e)
        }
    }

    /**
     * Login using Auth0 Universal Login (web-based or Google)
     */
    fun login(context: Context) {
        _loginState.value = AuthState.Loading
        isSignupFlow = false

        try {
            Log.d(TAG, "Login: starting Auth0 login process")

            WebAuthProvider.login(auth0)
                .withScheme(SCHEME)
                .withScope("openid profile email offline_access")
                .withAudience("https://$DOMAIN/api/v2/")
                .withParameters(mapOf("prompt" to "login"))
                .start(context, object : Callback<Credentials, AuthenticationException> {
                    override fun onSuccess(result: Credentials) {
                        Log.d(TAG, "Login successful")
                        credentials = result
                        // Save credentials
                        credentialsManager.saveCredentials(result)
                        getUserProfile(result.accessToken)
                    }

                    override fun onFailure(error: AuthenticationException) {
                        val errorMsg = error.getDescription() ?: "Unknown error"
                        Log.e(TAG, "Login failed: $errorMsg", error)
                        _loginState.value = AuthState.Error(errorMsg)
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "Login: Exception during Auth0 login", e)
            _loginState.value = AuthState.Error("Login error: ${e.message}")
        }
    }

    /**
     * Sign up using Auth0 Universal Login (web-based or Google)
     */
    fun signUp(context: Context) {
        _signUpState.value = AuthState.Loading
        isSignupFlow = true

        try {
            Log.d(TAG, "SignUp: starting Auth0 signup process")

            WebAuthProvider.login(auth0)
                .withScheme(SCHEME)
                .withScope("openid profile email offline_access")
                .withAudience("https://$DOMAIN/api/v2/")
                .withParameters(mapOf("screen_hint" to "signup"))
                .start(context, object : Callback<Credentials, AuthenticationException> {
                    override fun onSuccess(result: Credentials) {
                        Log.d(TAG, "Signup successful")
                        credentials = result
                        // Save credentials
                        credentialsManager.saveCredentials(result)
                        getUserProfile(result.accessToken)
                    }

                    override fun onFailure(error: AuthenticationException) {
                        val errorMsg = error.getDescription() ?: "Unknown error"
                        Log.e(TAG, "Signup failed: $errorMsg", error)
                        _signUpState.value = AuthState.Error(errorMsg)
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "SignUp: Exception during Auth0 signup", e)
            _signUpState.value = AuthState.Error("Sign up error: ${e.message}")
        }
    }

    /**
     * Direct login with Google (skips Universal Login screen)
     */
    fun loginWithGoogle(context: Context) {
        _loginState.value = AuthState.Loading
        isSignupFlow = false

        try {
            Log.d(TAG, "Starting Google login process")

            WebAuthProvider.login(auth0)
                .withScheme(SCHEME)
                .withScope("openid profile email offline_access")
                .withAudience("https://$DOMAIN/api/v2/")
                .withConnection("google-oauth2")  // Specify Google OAuth connection
                .withParameters(mapOf(
                    "access_type" to "offline",
                    "prompt" to "select_account"  // Force account selection
                ))
                .start(context, object : Callback<Credentials, AuthenticationException> {
                    override fun onSuccess(result: Credentials) {
                        Log.d(TAG, "Google login successful")
                        credentials = result
                        // Save credentials
                        credentialsManager.saveCredentials(result)
                        getUserProfile(result.accessToken)
                    }

                    override fun onFailure(error: AuthenticationException) {
                        val errorMsg = error.getDescription() ?: "Unknown error"
                        Log.e(TAG, "Google login failed: $errorMsg", error)
                        _loginState.value = AuthState.Error(errorMsg)
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Google login", e)
            _loginState.value = AuthState.Error("Google login error: ${e.message}")
        }
    }

    /**
     * Get user profile information from Auth0
     */
    private fun getUserProfile(accessToken: String) {
        val client = AuthenticationAPIClient(auth0)

        client.userInfo(accessToken)
            .start(object : Callback<UserProfile, AuthenticationException> {
                override fun onSuccess(result: UserProfile) {
                    Log.d(TAG, "User profile retrieved successfully")

                    userProfile = result
                    val subId = result.getId() ?: "Unknown Sub ID"

                    Log.d(TAG, "User sub ID: $subId") // Log user sub ID

                    // Create user from profile
                    val user = User(
                        id = subId,
                        email = result.email ?: "",
                        name = result.name ?: "Book Keeper User"
                    )

                    currentUser = user
                    storeUserInfo(user)

                    // Update the appropriate state based on the flow
                    if (isSignupFlow) {
                        _signUpState.value = AuthState.Success(user)
                    } else {
                        _loginState.value = AuthState.Success(user)
                    }

                    // Send the subId to the server
                    sendSubIdToServer(subId)
                }

                override fun onFailure(error: AuthenticationException) {
                    val errorMessage = error.getDescription() ?: "Failed to get user profile"
                    Log.e(TAG, "Failed to get user profile", error)

                    // Update the appropriate error state based on the flow
                    if (isSignupFlow) {
                        _signUpState.value = AuthState.Error(errorMessage)
                    } else {
                        _loginState.value = AuthState.Error(errorMessage)
                    }
                }
            })
    }

    /**
     * Send the sub ID to the backend server
     */
    private fun sendSubIdToServer(subId: String) {
        viewModelScope.launch {
            try {
                val userCheckRequest = UserCheckRequest(subId)
                val response: Response<UserCheckResponse> = ApiService.api.checkUser(userCheckRequest)

                if (response.isSuccessful) {
                    Log.d(TAG, "User checked successfully with the server")
                    val isNewUser = response.body()?.isNewUser ?: false
                    if (isNewUser) {
                        Log.d(TAG, "New user created on the server")
                    } else {
                        Log.d(TAG, "Existing user found on the server")
                    }
                } else {
                    Log.e(TAG, "Error checking user with the server: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception checking user: ${e.message}", e)
            }
        }
    }

    /**
     * Log out from Auth0
     */
    fun logout(context: Context) {
        try {
            Log.d(TAG, "Starting logout process")

            WebAuthProvider.logout(auth0)
                .withScheme(SCHEME)
                .start(context, object : Callback<Void?, AuthenticationException> {
                    override fun onSuccess(result: Void?) {
                        Log.d(TAG, "Logout successful")
                        clearAuthState()
                    }

                    override fun onFailure(error: AuthenticationException) {
                        Log.e(TAG, "Logout failed", error)
                        clearAuthState()
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Auth0 logout", e)
            clearAuthState()
        }
    }

    /**
     * Clear auth state
     */
    private fun clearAuthState() {
        credentials = null
        userProfile = null
        currentUser = null
        _loginState.value = AuthState.Idle
        _signUpState.value = AuthState.Idle

        // Clear stored credentials
        credentialsManager.clearCredentials()

        // Clear stored user info
        prefs.edit().remove(KEY_USER).apply()

        Log.d(TAG, "Auth state cleared")
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return credentialsManager.hasValidCredentials()
    }

    /**
     * Get current user
     */
    fun getCurrentUser(): User? {
        return currentUser
    }

    /**
     * Get access token for API calls
     */
    fun getAccessToken(): String? {
        return credentials?.accessToken
    }

    /**
     * Reset error states
     */
    fun resetAuthStates() {
        _loginState.value = AuthState.Idle
        _signUpState.value = AuthState.Idle
    }
}