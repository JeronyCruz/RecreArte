package edu.ucne.recrearte.presentation.work

import edu.ucne.recrearte.data.remote.dto.ArtistListDto
import edu.ucne.recrearte.data.remote.dto.ImagesDto
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import edu.ucne.recrearte.data.remote.dto.WorksDto
import edu.ucne.recrearte.data.remote.dto.WorksListDto

data class WorkUiState(
    val workId: Int? = null,
    val title: String = "",
    val dimension: String = "",
    val techniqueId: Int = 0,
    val artistId: Int = 0,
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
    val works: List<WorksListDto> = emptyList(),
    val techniquesL: List<TechniquesDto> = emptyList(),
    val artists: List<ArtistListDto> = emptyList(),
    val image: ImagesDto? = null,
    val images: Map<Int, String> = emptyMap() // Para cachear imágenes por workId
)