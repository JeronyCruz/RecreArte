package edu.ucne.recrearte.presentation.profile

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val userData: Any) : ProfileUiState() // Puede ser ArtistsDto o CustomersDto
    data class Error(val message: String) : ProfileUiState()
}