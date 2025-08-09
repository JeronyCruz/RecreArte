package edu.ucne.recrearte.presentation.Home

sealed interface HomeEvent {
    data class GetWorksByArtist(val artistId: Int): HomeEvent
    data class GetWorksByTechnique(val techinqueId: Int): HomeEvent
    data object GetArtists : HomeEvent
    data object GetTop10MostLikedWorks : HomeEvent
    data object GetTechniques: HomeEvent
}