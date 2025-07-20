package edu.ucne.recrearte.util

fun GetInitials(firstName: String, lastName: String): String {
    val firstInitial = firstName.take(1).uppercase()
    val lastInitial = lastName.take(1).uppercase()
    return "$firstInitial$lastInitial"
}