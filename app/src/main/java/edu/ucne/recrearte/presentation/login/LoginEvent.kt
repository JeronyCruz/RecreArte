package edu.ucne.recrearte.presentation.login

sealed interface LoginEvent {
    data object LoginUser: LoginEvent
    data class EmailChange(val email: String): LoginEvent
    data class PasswordChange(val password: String): LoginEvent
}