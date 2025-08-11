package edu.ucne.recrearte.presentation.profile

data class ValidationErrors(
    val userName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val documentNumber: String? = null,
    val passwordChangeError: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val artStyle: String? = null,
    val socialMediaLinks: String? = null,
    val address: String? = null
)