package edu.ucne.recrearte.presentation.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val errorEmail: String? = null,
    val errorPassword: String? = null,
)