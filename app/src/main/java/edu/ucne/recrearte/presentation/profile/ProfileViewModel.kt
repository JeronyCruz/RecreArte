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
import edu.ucne.recrearte.presentation.profile.ProfileUiState.Error
import edu.ucne.recrearte.presentation.profile.ProfileUiState.Loading
import edu.ucne.recrearte.presentation.profile.ProfileUiState.Success
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
        val currentState = _uiState.value as? ProfileUiState.Success ?: return false

        var isValid = true
        var newErrors = currentState.validationErrors.copy() // Start with current errors

        _editableArtist.value?.let { artist ->
            newErrors = newErrors.copy(
                userName = validateUserName(artist.userName).also { if (it != null) isValid = false },
                email = validateEmail(artist.email).also { if (it != null) isValid = false },
                phoneNumber = validatePhoneNumber(artist.phoneNumber).also { if (it != null) isValid = false },
                documentNumber = validateDocumentNumber(artist.documentNumber).also { if (it != null) isValid = false }
            )
        }

        _editableCustomer.value?.let { customer ->
            newErrors = newErrors.copy(
                userName = validateUserName(customer.userName).also { if (it != null) isValid = false },
                email = validateEmail(customer.email).also { if (it != null) isValid = false },
                phoneNumber = validatePhoneNumber(customer.phoneNumber).also { if (it != null) isValid = false },
                documentNumber = validateDocumentNumber(customer.documentNumber).also { if (it != null) isValid = false }
            )
        }

        // Update the state with the new errors
        _uiState.value = currentState.copy(validationErrors = newErrors)

        return isValid
    }

    private fun validateUserName(userName: String?): String? {
        return when {
            userName.isNullOrBlank() -> "Usuario es requerido"
            !userName.matches(Regex("^[a-zA-Z0-9]+\$")) -> "Solo letras y números"
            userName.length < 4 -> "Mínimo 4 caracteres"
            userName.length > 20 -> "Máximo 20 caracteres"
            else -> null
        }
    }

    private fun validateEmail(email: String?): String? {
        val emailRegex = Regex(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$",
            RegexOption.IGNORE_CASE
        )

        return when {
            email.isNullOrBlank() -> "El correo electrónico es requerido"
            !email.matches(emailRegex) -> "Formato de correo inválido (ej: usuario@ucne.edu.do)"
            !email.contains("@") -> "Falta el símbolo @"
            email.count { it == '@' } > 1 -> "Solo puede tener un @"
            email.substringAfterLast('@').isEmpty() -> "Falta el dominio después del @"
            email.substringBefore('@').isEmpty() -> "Falta el nombre antes del @"
            !email.substringAfterLast('@').contains(".") -> "El dominio debe contener un punto (.)"
            email.substringAfterLast('.').length < 2 -> "La extensión del dominio es muy corta (ej: .com)"
            else -> null
        }
    }

    private fun validatePhoneNumber(phoneNumber: String?): String? {
        val phoneRegex = Regex("^(809|829|849)[0-9]{7}\$")

        return when {
            phoneNumber.isNullOrBlank() -> "Teléfono es requerido"
            !phoneNumber.matches(Regex("^[0-9]+\$")) -> "Solo números"
            phoneNumber.length != 10 -> "Debe tener 10 dígitos"
            !phoneNumber.matches(phoneRegex) -> "Formato inválido (ej: 8091234567)"
            else -> null
        }
    }

    private fun validateDocumentNumber(documentNumber: String?): String? {
        return when {
            documentNumber.isNullOrBlank() -> "Cédula es requerida"
            !documentNumber.matches(Regex("^[0-9]+\$")) -> "Solo números"
            documentNumber.length != 11 -> "La cédula debe tener 11 dígitos"
            else -> null
        }
    }

    fun clearValidationErrors() {
        when (val currentState = _uiState.value) {
            is ProfileUiState.Success -> {
                _uiState.value = currentState.copy(validationErrors = ValidationErrors())
            }
            else -> {}
        }
    }

    // Función para actualizar campos - más eficiente
    fun updateField(event: ProfileEvent) {
        val currentState = _uiState.value as? ProfileUiState.Success ?: return
        when (event) {
            is ProfileEvent.UserNameChange -> {
                _editableArtist.value = _editableArtist.value?.copy(userName = event.userName)
                _editableCustomer.value = _editableCustomer.value?.copy(userName = event.userName)
                val newErrors = currentState.validationErrors.copy(
                    userName = validateUserName(event.userName)
                )
                _uiState.value = currentState.copy(validationErrors = newErrors)
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
                val newErrors = currentState.validationErrors.copy(
                    email = validateEmail(event.email)
                )
                _uiState.value = currentState.copy(validationErrors = newErrors)
            }
            is ProfileEvent.PhoneNumberChange -> {
                _editableArtist.value = _editableArtist.value?.copy(phoneNumber = event.phoneNumber)
                _editableCustomer.value = _editableCustomer.value?.copy(phoneNumber = event.phoneNumber)
                validatePhoneNumber(event.phoneNumber)
                val newErrors = currentState.validationErrors.copy(
                    phoneNumber = validatePhoneNumber(event.phoneNumber)
                )
                _uiState.value = currentState.copy(validationErrors = newErrors)
            }
            is ProfileEvent.DocumentNumberChange -> {
                _editableArtist.value = _editableArtist.value?.copy(documentNumber = event.documentNumber)
                _editableCustomer.value = _editableCustomer.value?.copy(documentNumber = event.documentNumber)
                validateDocumentNumber(event.documentNumber)
                val newErrors = currentState.validationErrors.copy(
                    documentNumber = validateDocumentNumber(event.documentNumber)
                )
                _uiState.value = currentState.copy(validationErrors = newErrors)
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

                // Creamos una bandera para saber si encontramos el perfil
                var profileFound = false

                // Primero intentamos obtener el artista
                artistRepository.getArtistById(userId).collect { artistResult ->
                    when (artistResult) {
                        is Resource.Success -> {
                            artistResult.data?.let { artist ->
                                if (artist.firstName != null) {
                                    currentPasswordHash = artist.password

                                    _uiState.value = ProfileUiState.Success(artist)
                                    profileFound = true

                                    return@collect
                                }
                            }
                            // Si llegamos aquí y no encontramos artista, buscamos customer
                            if (!profileFound) {
                                loadCustomerProfile(userId)
                            }
                        }
                        is Resource.Error -> {
                            // Si hay error con artista, intentamos con customer
                            loadCustomerProfile(userId)
                        }
                        is Resource.Loading -> {
                            // Mantenemos el estado de carga
                            _uiState.value = ProfileUiState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Error al cargar perfil")
            }
        }
    }

    private suspend fun loadCustomerProfile(userId: Int) {
        customerRepository.getCustomerById(userId).collect { customerResult ->
            when (customerResult) {
                is Resource.Success -> {
                    customerResult.data?.let { customer ->
                        if (customer.firstName != null) {
                            currentPasswordHash = customer.password
                            _uiState.value = ProfileUiState.Success(customer)
                            return@collect
                        }
                    }
                    _uiState.value = ProfileUiState.Error("Perfil no encontrado")
                }
                is Resource.Error -> {
                    _uiState.value = ProfileUiState.Error(
                        customerResult.message ?: "Error al cargar perfil de cliente"
                    )
                }
                is Resource.Loading -> {
                    _uiState.value = ProfileUiState.Loading
                }
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
                _uiState.value = Error(result.message ?: "Update failed")
            }
            is Resource.Loading<*> -> {
                _uiState.value = Loading
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