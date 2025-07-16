package edu.ucne.recrearte.presentation.signUp

sealed class SignUpEvent {
    data class EmailChange(val email: String) : SignUpEvent()
    data class PasswordChange(val password: String) : SignUpEvent()
    data class FirstNameChange(val firstName: String) : SignUpEvent()
    data class LastNameChange(val lastName: String) : SignUpEvent()
    data class UserNameChange(val userName: String) : SignUpEvent()
    data class PhoneNumberChange(val phoneNumber: String) : SignUpEvent()
    data class DocumentNumberChange(val documentNumber: String) : SignUpEvent()
    data class RoleChange(val isArtist: Boolean) : SignUpEvent()
    data class AddressChange(val address: String) : SignUpEvent()
    data class ArtStyleChange(val artStyle: String) : SignUpEvent()
    data class SocialMediaLinksChange(val socialMediaLinks: String) : SignUpEvent()
    object NextStep : SignUpEvent()
    object SignUp : SignUpEvent()
    data object PreviousStep : SignUpEvent()
//    object NavigateToLogin : SignUpEvent()
}