package edu.virginia.cs4720.mealtrader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val EMAIL_KEY = "email"

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val _uiState = MutableStateFlow(
        LoginUiState(email = savedStateHandle[EMAIL_KEY] ?: "")
    )
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        savedStateHandle[EMAIL_KEY] = value
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }

    fun signIn() = submit { AuthRepository.signIn(it.email.trim(), it.password) }
    fun signUp() = submit { AuthRepository.signUp(it.email.trim(), it.password) }

    private fun submit(action: suspend (LoginUiState) -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Enter an email and password") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                action(state)
                _uiState.update { it.copy(password = "") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = friendlyMessage(e)) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun friendlyMessage(e: Exception): String = when (e) {
        is FirebaseAuthWeakPasswordException -> "Password must be at least 6 characters"
        is FirebaseAuthUserCollisionException -> "An account with this email already exists"
        is FirebaseAuthInvalidUserException -> "No account found with this email"
        is FirebaseAuthInvalidCredentialsException -> "Wrong email or password"
        is FirebaseNetworkException -> "No internet connection"
        else -> "Something went wrong. Try again."
    }
}