package com.example.bookkeeper.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for handling authentication with Auth0.
 */
class Auth0ViewModel : ViewModel() {
    private val TAG = "Auth0ViewModel"

    // Auth0 client instance
    private lateinit var auth0: Auth0

    // Store credentials in memory
    private var credentials: Credentials? = null
    private var userProfile: UserProfile? = null

    // State flows to observe authentication states
    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    // Same state flow is used for sign up and login since Auth0 handles both
    val signUpState: StateFlow<AuthState> = loginState

    // Current authenticated user
    private var currentUser: User? = null

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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Auth0", e)
        }
    }

    /**
     * Login using Auth0 Universal Login (web-based)
     */
    fun login(context: Context) {
        _loginState.value = AuthState.Loading

        try {
            Log.d(TAG, "Login: starting Auth0 login process")

            WebAuthProvider.login(auth0)
                .withScheme(SCHEME)
                .withScope("openid profile email")
                .withAudience("https://$DOMAIN/api/v2/")
                .start(context, object : Callback<Credentials, AuthenticationException> {
                    override fun onSuccess(result: Credentials) {
                        Log.d(TAG, "Login successful")
                        credentials = result
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
     * Sign up using Auth0 Universal Login (web-based)
     */
    fun signUp(context: Context) {
        _loginState.value = AuthState.Loading

        try {
            Log.d(TAG, "SignUp: starting Auth0 signup process")

            WebAuthProvider.login(auth0)
                .withScheme(SCHEME)
                .withScope("openid profile email")
                .withAudience("https://$DOMAIN/api/v2/")
                .withParameters(mapOf("screen_hint" to "signup"))
                .start(context, object : Callback<Credentials, AuthenticationException> {
                    override fun onSuccess(result: Credentials) {
                        Log.d(TAG, "Signup successful")
                        credentials = result
                        getUserProfile(result.accessToken)
                    }

                    override fun onFailure(error: AuthenticationException) {
                        val errorMsg = error.getDescription() ?: "Unknown error"
                        Log.e(TAG, "Signup failed: $errorMsg", error)
                        _loginState.value = AuthState.Error(errorMsg)
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "SignUp: Exception during Auth0 signup", e)
            _loginState.value = AuthState.Error("Sign up error: ${e.message}")
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
                    _loginState.value = AuthState.Success(user)
                }

                override fun onFailure(error: AuthenticationException) {
                    Log.e(TAG, "Failed to get user profile", error)
                    _loginState.value = AuthState.Error(error.getDescription() ?: "Failed to get user profile")
                }
            })
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
                        credentials = null
                        userProfile = null
                        currentUser = null
                        _loginState.value = AuthState.Idle
                    }

                    override fun onFailure(error: AuthenticationException) {
                        Log.e(TAG, "Logout failed", error)
                        credentials = null
                        userProfile = null
                        currentUser = null
                        _loginState.value = AuthState.Idle
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Auth0 logout", e)
            credentials = null
            userProfile = null
            currentUser = null
            _loginState.value = AuthState.Idle
        }
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return credentials != null && !isTokenExpired()
    }

    /**
     * Check if token is expired
     */
    private fun isTokenExpired(): Boolean {
        val expiresAt = credentials?.expiresAt?.time ?: 0
        return expiresAt < System.currentTimeMillis()
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
    }
}
