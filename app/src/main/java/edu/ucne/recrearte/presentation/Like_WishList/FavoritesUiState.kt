package edu.ucne.recrearte.presentation.Like_WishList

import edu.ucne.recrearte.data.remote.dto.WorksDto

data class FavoritesUiState(
    val works: List<WorksDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)