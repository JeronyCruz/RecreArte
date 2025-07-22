package edu.ucne.recrearte.presentation.signUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.ArtistsDto
import edu.ucne.recrearte.data.remote.dto.CustomersDto
import edu.ucne.recrearte.data.repository.ArtistRepository
import edu.ucne.recrearte.data.repository.CustomerRepository
import edu.ucne.recrearte.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val customerRepository: CustomerRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.EmailChange -> updateEmail(event.email)
            is SignUpEvent.PasswordChange -> updatePassword(event.password)
            is SignUpEvent.FirstNameChange -> updateFirstName(event.firstName)
            is SignUpEvent.LastNameChange -> updateLastName(event.lastName)
            is SignUpEvent.UserNameChange -> updateUserName(event.userName)
            is SignUpEvent.PhoneNumberChange -> updatePhoneNumber(event.phoneNumber)
            is SignUpEvent.DocumentNumberChange -> updateDocumentNumber(event.documentNumber)
            is SignUpEvent.RoleChange -> updateRole(event.isArtist)
            is SignUpEvent.AddressChange -> updateAddress(event.address)
            is SignUpEvent.ArtStyleChange -> updateArtStyle(event.artStyle)
            is SignUpEvent.SocialMediaLinksChange -> updateSocialMediaLinks(event.socialMediaLinks)
            SignUpEvent.PreviousStep -> {
                _uiState.value = _uiState.value.copy(currentStep = 1)
            }
            SignUpEvent.NextStep -> validateStep1()
            SignUpEvent.SignUp -> validateStep2()
        }
    }

    // === Actualizaciones de estado ===
    private fun updateEmail(email: String) {
        val emailRegex = Regex(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$",
            RegexOption.IGNORE_CASE
        )

        _uiState.value = _uiState.value.copy(
            email = email,
            errorEmail = when {
                email.isBlank() -> "El correo electrónico es requerido"
                !email.matches(emailRegex) -> "Formato de correo inválido (ej: usuario@ucne.edu.do)"
                !email.contains("@") -> "Falta el símbolo @"
                email.count { it == '@' } > 1 -> "Solo puede tener un @"
                email.substringAfterLast('@').isEmpty() -> "Falta el dominio después del @"
                email.substringBefore('@').isEmpty() -> "Falta el nombre antes del @"
                !email.substringAfterLast('@').contains(".") -> "El dominio debe contener un punto (.)"
                email.substringAfterLast('.').length < 2 -> "La extensión del dominio es muy corta (ej: .com)"
                else -> null
            }
        )
    }

    private fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            errorPassword = if (password.length < 6) "Mínimo 6 caracteres" else null
        )
    }

    private fun updateFirstName(firstName: String) {
        _uiState.value = _uiState.value.copy(
            firstName = firstName,
            errorFirstName = if (firstName.isBlank()) "Nombre es requerido"
            else if (!firstName.matches(Regex("^[a-zA-Z\\s]+\$"))) "Solo letras"
            else null
        )
    }

    private fun updateLastName(lastName: String) {
        _uiState.value = _uiState.value.copy(
            lastName = lastName,
            errorLastName = if (lastName.isBlank()) "Apellido es requerido"
            else if (!lastName.matches(Regex("^[a-zA-Z\\s]+\$"))) "Solo letras"
            else null
        )
    }

    private fun updateUserName(userName: String) {
        _uiState.value = _uiState.value.copy(
            userName = userName,
            errorUserName = when {
                userName.isBlank() -> "Usuario es requerido"
                !userName.matches(Regex("^[a-zA-Z0-9]+\$")) -> "Solo letras y números"
                userName.length < 4 -> "Mínimo 4 caracteres"
                userName.length > 20 -> "Máximo 20 caracteres"
                else -> null
            }
        )
    }

    private fun updatePhoneNumber(phoneNumber: String) {
        // Validación para República Dominicana:
        // - Debe empezar con 809, 829, 849 (o permitir otros formatos)
        // - Exactamente 10 dígitos
        val phoneRegex = Regex("^(809|829|849)[0-9]{7}\$")

        _uiState.value = _uiState.value.copy(
            phoneNumber = phoneNumber,
            errorPhoneNumber = when {
                phoneNumber.isBlank() -> "Teléfono es requerido"
                !phoneNumber.matches(Regex("^[0-9]+\$")) -> "Solo números"
                phoneNumber.length != 10 -> "Debe tener 10 dígitos"
                !phoneNumber.matches(phoneRegex) -> "Formato inválido (ej: 8091234567)"
                else -> null
            }
        )
    }

    private fun updateDocumentNumber(documentNumber: String) {
        // Validación para cédula dominicana:
        // - 11 dígitos exactos
        // - Puedes añadir validación del dígito verificador si lo deseas

        _uiState.value = _uiState.value.copy(
            documentNumber = documentNumber,
            errorDocumentNumber = when {
                documentNumber.isBlank() -> "Cédula es requerida"
                !documentNumber.matches(Regex("^[0-9]+\$")) -> "Solo números"
                documentNumber.length != 11 -> "La cédula debe tener 11 dígitos"
                else -> null
            }
        )
    }

    private fun updateRole(isArtist: Boolean) {
        _uiState.value = _uiState.value.copy(isArtist = isArtist)
    }

    private fun updateAddress(address: String) {
        _uiState.value = _uiState.value.copy(
            address = address,
            errorAddress = if (address.isBlank()) "Dirección es requerida"
            else if (!address.matches(Regex("^[a-zA-Z0-9\\s]+\$"))) "Caracteres inválidos"
            else null
        )
    }

    private fun updateArtStyle(artStyle: String) {
        _uiState.value = _uiState.value.copy(
            artStyle = artStyle,
            errorArtStyle = if (artStyle.isBlank() && _uiState.value.isArtist) "Estilo artístico es requerido"
            else if (!artStyle.matches(Regex("^[a-zA-Z\\s]+\$")) && _uiState.value.isArtist) "Solo letras"
            else null
        )
    }

    private fun updateSocialMediaLinks(links: String) {
        _uiState.value = _uiState.value.copy(
            socialMediaLinks = links,
            errorSocialMediaLinks = if (links.isBlank() && _uiState.value.isArtist) "Redes sociales son requeridas"
            else null
        )
    }

    // === Validaciones ===
    private fun validateStep1() {
        val current = _uiState.value
        val isEmailValid = current.email.isNotBlank() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(current.email).matches()
        val isPasswordValid = current.password.length >= 6

        _uiState.value = current.copy(
            errorEmail = if (!isEmailValid) "Email inválido" else null,
            errorPassword = if (!isPasswordValid) "Mínimo 6 caracteres" else null
        )

        if (isEmailValid && isPasswordValid) {
            _uiState.value = current.copy(currentStep = 2)
        }
    }

    private fun validateStep2(): Boolean {
        val current = _uiState.value
        var isValid = true

        if (current.firstName.isBlank()) {
            isValid = false
            _uiState.value = current.copy(errorFirstName = "Campo requerido")
        }
        if (current.lastName.isBlank()) {
            isValid = false
            _uiState.value = current.copy(errorLastName = "Campo requerido")
        }
        if (current.userName.isBlank()) {
            isValid = false
            _uiState.value = current.copy(errorUserName = "Campo requerido")
        }
        if (current.phoneNumber.isBlank()) {
            isValid = false
            _uiState.value = current.copy(errorPhoneNumber = "Campo requerido")
        }
        if (current.documentNumber.isBlank()) {
            isValid = false
            _uiState.value = current.copy(errorDocumentNumber = "Campo requerido")
        }

        if (current.isArtist) {
            if (current.artStyle.isBlank()) {
                isValid = false
                _uiState.value = current.copy(errorArtStyle = "Estilo artístico es requerido")
            }
            if (current.socialMediaLinks.isBlank()) {
                isValid = false
                _uiState.value = current.copy(errorSocialMediaLinks = "Redes sociales son requeridas")
            }
        } else {
            if (current.address.isBlank()) {
                isValid = false
                _uiState.value = current.copy(errorAddress = "Dirección es requerida")
            }
        }

        if (isValid) {
            signUpUser()
        }

        return isValid
    }

    // === Lógica de Registro ===
    private fun signUpUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val current = _uiState.value
                val result = if (current.isArtist) {
                    registerArtist(current)
                } else {
                    registerCustomer(current)
                }

                when (result) {
                    is Resource.Success -> {
                        // Para Success, sabemos que data no es null
                        val token = when (val data = result.data) {
                            is ArtistsDto -> data.token
                            is CustomersDto -> data.token
                            else -> throw IllegalStateException("Tipo de dato no esperado")
                        }

                        tokenManager.saveToken(token ?: "")
                        println("🔑 [DEBUG] Token almacenado: ${token?.take(10)}...")

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            errorMessage = null
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Error desconocido"
                        )
                    }
                    is Resource.Loading -> {
                        // No deberíamos entrar aquí normalmente
                        _uiState.value = _uiState.value.copy(
                            isLoading = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error: ${e.message ?: "Desconocido"}"
                )
            }
        }
    }

    private suspend fun registerArtist(state: SignUpUiState): Resource<ArtistsDto> {
        val artistDto = ArtistsDto(
            artistId = 0,
            firstName = state.firstName,
            lastName = state.lastName,
            email = state.email,
            password = state.password,
            userName = state.userName,
            phoneNumber = state.phoneNumber,
            documentNumber = state.documentNumber,
            updateAt = Date(),
            roleId = 2,
            artStyle = state.artStyle,
            socialMediaLinks = state.socialMediaLinks,
            description = null,
            token = "" // El backend lo completará
        )

        println("🔄 [DEBUG] Registrando artista...")
        val result = artistRepository.createArtist(artistDto)
        println("🔑 [DEBUG] Resultado registro artista: ${result.data?.token?.take(10)}...")
        return result
    }

    private suspend fun registerCustomer(state: SignUpUiState): Resource<CustomersDto> {
        val customerDto = CustomersDto(
            customerId = 0,
            firstName = state.firstName,
            lastName = state.lastName,
            email = state.email,
            password = state.password,
            userName = state.userName,
            phoneNumber = state.phoneNumber,
            documentNumber = state.documentNumber,
            updateAt = Date(),
            roleId = 3,
            address = state.address,
            description = null,
            token = "" // El backend lo completará
        )

        println("🔄 [DEBUG] Registrando cliente...")
        val result = customerRepository.createCustomer(customerDto)
        println("🔑 [DEBUG] Resultado registro cliente: ${result.data?.token?.take(10)}...")
        return result
    }
}