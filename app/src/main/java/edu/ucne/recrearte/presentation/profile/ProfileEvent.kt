package edu.ucne.recrearte.presentation.profile

sealed class ProfileEvent {
    data class FirstNameChange(val firstName: String) : ProfileEvent()
    data class UserNameChange(val userName: String) : ProfileEvent()
    data class LastNameChange(val lastName: String) : ProfileEvent()
    data class EmailChange(val email: String) : ProfileEvent()
    data class PhoneNumberChange(val phoneNumber: String) : ProfileEvent()
    data class PasswordChange(val password: String) : ProfileEvent()
    data class DocumentNumberChange(val documentNumber: String) : ProfileEvent()
    data class AddressChange(val address: String) : ProfileEvent()
    data class ArtStyleChange(val artStyle: String) : ProfileEvent()
    data class SocialMediaLinksChange(val socialMediaLinks: String) : ProfileEvent()
    object SaveChanges : ProfileEvent()
    object EditProfile : ProfileEvent()
    object CancelEdit : ProfileEvent()
}