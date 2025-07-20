package edu.ucne.recrearte.presentation.profile


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.ArtistsDto
import edu.ucne.recrearte.data.remote.dto.CustomersDto
import edu.ucne.recrearte.data.repository.ArtistRepository
import edu.ucne.recrearte.data.repository.CustomerRepository
import edu.ucne.recrearte.data.repository.UserRepository
import edu.ucne.recrearte.util.TokenManager
import edu.ucne.recrearte.util.getUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val artistRepository: ArtistRepository,
    private val customerRepository: CustomerRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Estado para manejar contraseñas temporalmente
    private var currentPasswordHash: String? = null

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val userId = tokenManager.getUserId() ?: throw Exception("Usuario no autenticado")

                // Intenta obtener como artista primero
                when (val artistResult = artistRepository.getArtistById(userId)) {
                    is Resource.Success -> {
                        artistResult.data?.let { artist ->
                            if (artist.firstName != null) {
                                currentPasswordHash = artist.password // Guarda el hash actual
                                _uiState.value = ProfileUiState.Success(artist)
                                return@launch
                            }
                        }
                    }
                    is Resource.Error -> Unit // Continuar con cliente
                    is Resource.Loading<*> -> Unit
                }

                // Intenta como cliente si no es artista
                when (val customerResult = customerRepository.getCustomerById(userId)) {
                    is Resource.Success -> {
                        customerResult.data?.let { customer ->
                            if (customer.firstName != null) {
                                currentPasswordHash = customer.password // Guarda el hash actual
                                _uiState.value = ProfileUiState.Success(customer)
                                return@launch
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.value = ProfileUiState.Error("Perfil no encontrado")
                    }
                    is Resource.Loading<*> -> Unit
                }

                _uiState.value = ProfileUiState.Error("Perfil no encontrado")

            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Error al cargar perfil")
            }
        }
    }

    fun updateProfile(updatedData: Any, newPassword: String? = null) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val userId = tokenManager.getUserId() ?: throw Exception("User not logged in")

                when (updatedData) {
                    is ArtistsDto -> {
                        // Prepara el DTO para actualización
                        val artistToUpdate = if (!newPassword.isNullOrBlank()) {
                            updatedData.copy(password = newPassword) // Envía contraseña en texto plano
                        } else {
                            updatedData.copy(password = currentPasswordHash ?: "") // Mantiene la actual
                        }

                        val result = artistRepository.updateArtist(userId, artistToUpdate)
                        handleUpdateResult(result)
                    }
                    is CustomersDto -> {
                        val customerToUpdate = if (!newPassword.isNullOrBlank()) {
                            updatedData.copy(password = newPassword)
                        } else {
                            updatedData.copy(password = currentPasswordHash ?: "")
                        }

                        val result = customerRepository.updateCustomer(userId, customerToUpdate)
                        handleUpdateResult(result)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Error updating profile")
            }
        }
    }

    private fun handleUpdateResult(result: Resource<*>) {
        when (result) {
            is Resource.Success -> {
                // Recargar el perfil después de actualizar
                loadUserProfile()
            }
            is Resource.Error -> {
                _uiState.value = ProfileUiState.Error(result.message ?: "Update failed")
            }
            is Resource.Loading<*> -> {
                _uiState.value = ProfileUiState.Loading
            }
        }
    }

    // Agrega esta función al ViewModel
    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Validaciones básicas
                if (newPassword != confirmPassword) {
                    onError("Las contraseñas no coinciden")
                    return@launch
                }

                if (newPassword.length < 6) {
                    onError("La contraseña debe tener al menos 6 caracteres")
                    return@launch
                }

                val userId = tokenManager.getUserId() ?: run {
                    onError("Usuario no autenticado")
                    return@launch
                }

                // Llamada al repositorio
                when (val result = userRepository.changePassword(
                    userId = userId,
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )) {
                    is Resource.Success -> {
                        if (result.data == true) { // Asumiendo que el repositorio devuelve Resource<Boolean>
                            onSuccess()
                        } else {
                            onError("La contraseña introducida no es la actual")
                        }
                    }
                    is Resource.Error -> {
                        onError(result.message ?: "Error al cambiar contraseña")
                    }
                    is Resource.Loading -> {
                        // Puedes manejar el estado de carga si es necesario
                    }
                }
            } catch (e: Exception) {
                onError("Error: ${e.message ?: "Error desconocido"}")
            }
        }
    }
}