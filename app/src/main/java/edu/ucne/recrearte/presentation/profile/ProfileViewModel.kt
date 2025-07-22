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

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    private val _showChangePasswordDialog = MutableStateFlow(false)
    val showChangePasswordDialog: StateFlow<Boolean> = _showChangePasswordDialog.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    // Estados editables
    private val _editableArtist = MutableStateFlow<ArtistsDto?>(null)
    private val _editableCustomer = MutableStateFlow<CustomersDto?>(null)
    val editableArtist: StateFlow<ArtistsDto?> = _editableArtist.asStateFlow()
    val editableCustomer: StateFlow<CustomersDto?> = _editableCustomer.asStateFlow()

    private val _passwordChangeError = MutableStateFlow<String?>(null)
    val passwordChangeError: StateFlow<String?> = _passwordChangeError.asStateFlow()

    // Estados para errores de validación
    private val _errorUserName = MutableStateFlow<String?>(null)
    val errorUserName: StateFlow<String?> = _errorUserName.asStateFlow()

    private val _errorEmail = MutableStateFlow<String?>(null)
    val errorEmail: StateFlow<String?> = _errorEmail.asStateFlow()

    private val _errorPhoneNumber = MutableStateFlow<String?>(null)
    val errorPhoneNumber: StateFlow<String?> = _errorPhoneNumber.asStateFlow()

    private val _errorDocumentNumber = MutableStateFlow<String?>(null)
    val errorDocumentNumber: StateFlow<String?> = _errorDocumentNumber.asStateFlow()

    private var currentPasswordHash: String? = null

    init {
        loadUserProfile()
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun showChangePasswordDialog(show: Boolean) {
        _showChangePasswordDialog.value = show
    }

    fun startEditing() {
        when (val currentData = (_uiState.value as? ProfileUiState.Success)?.userData) {
            is ArtistsDto -> _editableArtist.value = currentData.copy()
            is CustomersDto -> _editableCustomer.value = currentData.copy()
        }
        _isEditing.value = true
    }

    fun cancelEdit() {
        _editableArtist.value = null
        _editableCustomer.value = null
        _isEditing.value = false
        clearValidationErrors()
    }

    fun saveChanges() {
        if (validateFields()) {
            _editableArtist.value?.let { updateProfile(it) }
            _editableCustomer.value?.let { updateProfile(it) }
            _isEditing.value = false
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        _editableArtist.value?.let { artist ->
            if (!validateUserName(artist.userName)) isValid = false
            if (!validateEmail(artist.email)) isValid = false
            if (!validatePhoneNumber(artist.phoneNumber)) isValid = false
            if (!validateDocumentNumber(artist.documentNumber)) isValid = false
        }

        _editableCustomer.value?.let { customer ->
            if (!validateUserName(customer.userName)) isValid = false
            if (!validateEmail(customer.email)) isValid = false
            if (!validatePhoneNumber(customer.phoneNumber)) isValid = false
            if (!validateDocumentNumber(customer.documentNumber)) isValid = false
        }

        return isValid
    }

    private fun validateUserName(userName: String?): Boolean {
        return when {
            userName.isNullOrBlank() -> {
                _errorUserName.value = "Usuario es requerido"
                false
            }
            !userName.matches(Regex("^[a-zA-Z0-9]+\$")) -> {
                _errorUserName.value = "Solo letras y números"
                false
            }
            userName.length < 4 -> {
                _errorUserName.value = "Mínimo 4 caracteres"
                false
            }
            userName.length > 20 -> {
                _errorUserName.value = "Máximo 20 caracteres"
                false
            }
            else -> {
                _errorUserName.value = null
                true
            }
        }
    }

    private fun validateEmail(email: String?): Boolean {
        val emailRegex = Regex(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$",
            RegexOption.IGNORE_CASE
        )

        return when {
            email.isNullOrBlank() -> {
                _errorEmail.value = "El correo electrónico es requerido"
                false
            }
            !email.matches(emailRegex) -> {
                _errorEmail.value = "Formato de correo inválido (ej: usuario@ucne.edu.do)"
                false
            }
            !email.contains("@") -> {
                _errorEmail.value = "Falta el símbolo @"
                false
            }
            email.count { it == '@' } > 1 -> {
                _errorEmail.value = "Solo puede tener un @"
                false
            }
            email.substringAfterLast('@').isEmpty() -> {
                _errorEmail.value = "Falta el dominio después del @"
                false
            }
            email.substringBefore('@').isEmpty() -> {
                _errorEmail.value = "Falta el nombre antes del @"
                false
            }
            !email.substringAfterLast('@').contains(".") -> {
                _errorEmail.value = "El dominio debe contener un punto (.)"
                false
            }
            email.substringAfterLast('.').length < 2 -> {
                _errorEmail.value = "La extensión del dominio es muy corta (ej: .com)"
                false
            }
            else -> {
                _errorEmail.value = null
                true
            }
        }
    }

    private fun validatePhoneNumber(phoneNumber: String?): Boolean {
        // Validación para República Dominicana:
        // - Debe empezar con 809, 829, 849 (o permitir otros formatos)
        // - Exactamente 10 dígitos
        val phoneRegex = Regex("^(809|829|849)[0-9]{7}\$")

        return when {
            phoneNumber.isNullOrBlank() -> {
                _errorPhoneNumber.value = "Teléfono es requerido"
                false
            }
            !phoneNumber.matches(Regex("^[0-9]+\$")) -> {
                _errorPhoneNumber.value = "Solo números"
                false
            }
            phoneNumber.length != 10 -> {
                _errorPhoneNumber.value = "Debe tener 10 dígitos"
                false
            }
            !phoneNumber.matches(phoneRegex) -> {
                _errorPhoneNumber.value = "Formato inválido (ej: 8091234567)"
                false
            }
            else -> {
                _errorPhoneNumber.value = null
                true
            }
        }
    }

    private fun validateDocumentNumber(documentNumber: String?): Boolean {
        // Validación para cédula dominicana:
        // - 11 dígitos exactos
        // - Puedes añadir validación del dígito verificador si lo deseas

        return when {
            documentNumber.isNullOrBlank() -> {
                _errorDocumentNumber.value = "Cédula es requerida"
                false
            }
            !documentNumber.matches(Regex("^[0-9]+\$")) -> {
                _errorDocumentNumber.value = "Solo números"
                false
            }
            documentNumber.length != 11 -> {
                _errorDocumentNumber.value = "La cédula debe tener 11 dígitos"
                false
            }
            else -> {
                _errorDocumentNumber.value = null
                true
            }
        }
    }

    private fun clearValidationErrors() {
        _errorUserName.value = null
        _errorEmail.value = null
        _errorPhoneNumber.value = null
        _errorDocumentNumber.value = null
    }

    // Función para actualizar campos - más eficiente
    fun updateField(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.UserNameChange -> {
                _editableArtist.value = _editableArtist.value?.copy(userName = event.userName)
                _editableCustomer.value = _editableCustomer.value?.copy(userName = event.userName)
                validateUserName(event.userName)
            }
            is ProfileEvent.FirstNameChange -> {
                _editableArtist.value = _editableArtist.value?.copy(firstName = event.firstName)
                _editableCustomer.value = _editableCustomer.value?.copy(firstName = event.firstName)
            }
            is ProfileEvent.LastNameChange -> {
                _editableArtist.value = _editableArtist.value?.copy(lastName = event.lastName)
                _editableCustomer.value = _editableCustomer.value?.copy(lastName = event.lastName)
            }
            is ProfileEvent.EmailChange -> {
                _editableArtist.value = _editableArtist.value?.copy(email = event.email)
                _editableCustomer.value = _editableCustomer.value?.copy(email = event.email)
                validateEmail(event.email)
            }
            is ProfileEvent.PhoneNumberChange -> {
                _editableArtist.value = _editableArtist.value?.copy(phoneNumber = event.phoneNumber)
                _editableCustomer.value = _editableCustomer.value?.copy(phoneNumber = event.phoneNumber)
                validatePhoneNumber(event.phoneNumber)
            }
            is ProfileEvent.DocumentNumberChange -> {
                _editableArtist.value = _editableArtist.value?.copy(documentNumber = event.documentNumber)
                _editableCustomer.value = _editableCustomer.value?.copy(documentNumber = event.documentNumber)
                validateDocumentNumber(event.documentNumber)
            }
            is ProfileEvent.ArtStyleChange -> {
                _editableArtist.value = _editableArtist.value?.copy(artStyle = event.artStyle)
            }
            is ProfileEvent.SocialMediaLinksChange -> {
                _editableArtist.value = _editableArtist.value?.copy(socialMediaLinks = event.socialMediaLinks)
            }
            is ProfileEvent.AddressChange -> {
                _editableCustomer.value = _editableCustomer.value?.copy(address = event.address)
            }
            else -> {}
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val userId = tokenManager.getUserId() ?: throw Exception("Usuario no autenticado")

                when (val artistResult = artistRepository.getArtistById(userId)) {
                    is Resource.Success -> {
                        artistResult.data?.let { artist ->
                            if (artist.firstName != null) {
                                currentPasswordHash = artist.password
                                _uiState.value = ProfileUiState.Success(artist)
                                return@launch
                            }
                        }
                    }
                    is Resource.Error -> Unit
                    is Resource.Loading<*> -> Unit
                }

                when (val customerResult = customerRepository.getCustomerById(userId)) {
                    is Resource.Success -> {
                        customerResult.data?.let { customer ->
                            if (customer.firstName != null) {
                                currentPasswordHash = customer.password
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

    private fun updateProfile(updatedData: Any) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val userId = tokenManager.getUserId() ?: throw Exception("User not logged in")

                when (updatedData) {
                    is ArtistsDto -> {
                        val result = artistRepository.updateArtist(userId, updatedData)
                        handleUpdateResult(result)
                    }
                    is CustomersDto -> {
                        val result = customerRepository.updateCustomer(userId, updatedData)
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
            is Resource.Success -> loadUserProfile()
            is Resource.Error -> {
                _uiState.value = ProfileUiState.Error(result.message ?: "Update failed")
            }
            is Resource.Loading<*> -> {
                _uiState.value = ProfileUiState.Loading
            }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Resetear el error previo
                _passwordChangeError.value = null

                if (newPassword != confirmPassword) {
                    _passwordChangeError.value = "Las contraseñas no coinciden"
                    return@launch
                }

                if (newPassword.length < 6) {
                    _passwordChangeError.value = "La contraseña debe tener al menos 6 caracteres"
                    return@launch
                }

                val userId = tokenManager.getUserId() ?: run {
                    _passwordChangeError.value = "Usuario no autenticado"
                    return@launch
                }

                when (val result = userRepository.changePassword(
                    userId = userId,
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )) {
                    is Resource.Success -> {
                        if (result.data == true) {
                            onSuccess()
                            _showChangePasswordDialog.value = false
                        } else {
                            _passwordChangeError.value = "La contraseña actual es incorrecta"
                        }
                    }
                    is Resource.Error -> {
                        _passwordChangeError.value = result.message ?: "Error al cambiar contraseña"
                    }
                    is Resource.Loading -> Unit
                }
            } catch (e: Exception) {
                _passwordChangeError.value = "Error: ${e.message ?: "Error desconocido"}"
            }
        }
    }

    fun clearPasswordError() {
        _passwordChangeError.value = null
    }
}