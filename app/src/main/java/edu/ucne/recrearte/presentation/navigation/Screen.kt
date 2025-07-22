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
    data object LoginScreen: Screen()
    @Serializable
    data object RegisterScreen: Screen()
    @Serializable
    data object TechniqueList: Screen()
    @Serializable
    data class TechniqueScreen(val id: Int): Screen()
    @Serializable
    data object SignUpScreen: Screen()
    @Serializable
    data object WorkListScreen: Screen()
    @Serializable
    data class WorkScreen(val id: Int): Screen()

    @Serializable
    object FavoritesScreen : Screen()

    @Serializable
    object CartScreen: Screen()

    @Serializable
    object ProfileScreen : Screen()

    @Serializable
    object RecreArteScreen : Screen()
    @Serializable
    data class WorkByTechnique(val techniqueId: Int): Screen()
    @Serializable
    data class WorkByArtist(val artistId: Int): Screen()
    @Serializable
    data class WorkDetails(val workId: Int): Screen()
}