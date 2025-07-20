package edu.ucne.recrearte.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.recrearte.util.TokenManager
import edu.ucne.recrearte.data.remote.dto.LoginRequestDto
import edu.ucne.recrearte.data.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when(event) {
            is LoginEvent.EmailChange -> emailOnChange(event.email)
            is LoginEvent.PasswordChange -> passwordOnChange(event.password)
            LoginEvent.LoginUser -> loginUser()
        }
    }

    private fun emailOnChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    private fun passwordOnChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    private fun loginUser() {
        viewModelScope.launch {
            try {
                val response = repository.loginUser(
                    LoginRequestDto(
                        email = _uiState.value.email,
                        password = _uiState.value.password
                    )
                )

                // Debug: Verificar token recibido
                println("🔐 [LOGIN] Token recibido: ${response.token.take(10)}...")

                // Guardar token
                tokenManager.saveToken(response.token)

                // Verificar que se guardó correctamente
                val savedToken = tokenManager.getToken()
                println("🔍 [LOGIN] Token guardado: ${savedToken?.take(10)}...")

                _uiState.update {
                    it.copy(
                        isSuccess = true,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Error de login: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.logout()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSuccess = false,
                    email = "",
                    password = ""
                )
            }
        }
    }
}