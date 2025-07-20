package edu.ucne.recrearte.presentation.Home

import edu.ucne.recrearte.data.remote.dto.ArtistListDto
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import edu.ucne.recrearte.data.remote.dto.WorksDto

data class HomeUiState (
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val errorName: String? = null,
    val works: List<WorksDto> = emptyList(),
    val worksByTechnique: List<WorksDto> = emptyList(),
    val worksByArtistsDto: List<WorksDto> = emptyList(),
    val listTopTen: List<WorksDto> = emptyList(),
    val listArtist: List<ArtistListDto> = emptyList(),
    val techniquesL: List<TechniquesDto> = emptyList()
)