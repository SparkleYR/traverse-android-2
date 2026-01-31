package com.example.traverse2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.traverse2.data.SessionManager
import com.example.traverse2.data.api.RetrofitClient
import com.example.traverse2.data.model.LoginRequest
import com.example.traverse2.data.model.RegisterRequest
import com.example.traverse2.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.api
    private val sessionManager = SessionManager.getInstance(application)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoggedIn.value = sessionManager.isLoggedInSync()
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = api.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    val user = authResponse.user

                    // Save session to DataStore
                    sessionManager.saveSession(
                        username = user.username,
                        userId = user.id.toString()
                    )
                    
                    // Save token from response body (backup for cookie)
                    authResponse.token?.let { sessionManager.saveAuthToken(it) }

                    _isLoggedIn.value = true
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = parseErrorMessage(errorBody) ?: "Login failed"
                    _uiState.value = AuthUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all fields")
            return
        }

        if (username.length < 3 || username.length > 20) {
            _uiState.value = AuthUiState.Error("Username must be 3-20 characters")
            return
        }

        if (password.length < 8) {
            _uiState.value = AuthUiState.Error("Password must be at least 8 characters")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState.Error("Please enter a valid email")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = api.register(RegisterRequest(username, email, password))
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    val user = authResponse.user

                    // Save session to DataStore
                    sessionManager.saveSession(
                        username = user.username,
                        userId = user.id.toString()
                    )
                    
                    // Save token from response body (backup for cookie)
                    authResponse.token?.let { sessionManager.saveAuthToken(it) }

                    _isLoggedIn.value = true
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = parseErrorMessage(errorBody) ?: "Registration failed"
                    _uiState.value = AuthUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                // Call logout endpoint to clear server-side cookie
                api.logout()
            } catch (e: Exception) {
                // Ignore network errors during logout
            }

            // Clear local session
            sessionManager.clearSession()
            RetrofitClient.clearCookies()

            _isLoggedIn.value = false
            _uiState.value = AuthUiState.Idle
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody == null) return null
        // Simple parsing - in production use proper JSON parsing
        return try {
            if (errorBody.contains("\"error\"")) {
                val start = errorBody.indexOf("\"error\":\"") + 9
                val end = errorBody.indexOf("\"", start)
                if (start > 8 && end > start) {
                    errorBody.substring(start, end)
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
