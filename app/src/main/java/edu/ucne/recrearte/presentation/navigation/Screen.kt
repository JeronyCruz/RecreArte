package edu.ucne.recrearte.presentation.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    data object Home: Screen()
    @Serializable
    data object PaymentMethodList: Screen()
    @Serializable
    data class PaymentMethodScreen(val id: Int): Screen()
    @Serializable
    data object TechniqueList: Screen()
    @Serializable
    data class TechniqueScreen(val id: Int): Screen()
}