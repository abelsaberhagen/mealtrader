package edu.virginia.cs4720.mealtrader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }
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
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Something went wrong") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}