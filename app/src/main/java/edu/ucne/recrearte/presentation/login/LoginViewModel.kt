package edu.ucne.recrearte.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.recrearte.data.remote.InvalidCredentialsException
import edu.ucne.recrearte.data.remote.dto.LoginRequestDto
import edu.ucne.recrearte.data.repository.LoginRepository
import edu.ucne.recrearte.presentation.navigation.Screen
import edu.ucne.recrearte.util.TokenManager
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

    // En LoginViewModel.kt
    private fun loginUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Validación básica de campos
                if (_uiState.value.email.isBlank() || _uiState.value.password.isBlank()) {
                    _uiState.update {
                        it.copy(
                            errorMessage = "Por favor ingrese correo y contraseña",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                val response = repository.loginUser(
                    LoginRequestDto(
                        email = _uiState.value.email,
                        password = _uiState.value.password
                    )
                )

                tokenManager.saveToken(response.token)
                tokenManager.saveRoleId(response.roleId)

                _uiState.update {
                    it.copy(
                        isSuccess = true,
                        isLoading = false,
                        roleId = response.roleId
                    )
                }
            } catch (e: InvalidCredentialsException) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message, // "Correo o contraseña incorrectos"
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Error de conexión. Intente nuevamente",
                        isLoading = false
                    )
                }
                println("[LOGIN ERROR] ${e.message}")
            }
        }
    }

    fun logout(navController: NavHostController) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.logout()
            navController.navigate(Screen.LoginScreen) {
                popUpTo(0)
            }
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