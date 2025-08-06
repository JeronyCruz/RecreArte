package edu.ucne.recrearte.presentation.work

import edu.ucne.recrearte.data.remote.dto.ArtistListDto
import edu.ucne.recrearte.data.remote.dto.ImagesDto
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import edu.ucne.recrearte.data.remote.dto.WorksDto

data class WorkUiState(
    val workId: Int? = null,
    val title: String = "",
    val dimension: String = "",
    val techniqueId: Int = 0,
    val artistId: Int = 0,
    val statusId: Int = 1,
    val price: Double = 0.0,
    val description: String = "",
    val imageId: Int = 0,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = "",
    val errorTitle: String? = "",
    val errorDimension: String? = "",
    val errorPrice: String? = "",
    val errorDescription: String? = "",
    val works: List<WorksDto> = emptyList(),
    val techniquesL: List<TechniquesDto> = emptyList(),
    val artists: List<ArtistListDto> = emptyList(),
    val imageRemoved: Boolean = false,
    val nameArtist: String = "",
    val imageUrl: String = "",
    val isCached: Boolean = false,
    val networkMessage: String? = null,
    val lastUpdated: String = "",
    val showRetryButton: Boolean = false,
    val isOnline: Boolean = true
)