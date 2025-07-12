package edu.ucne.recrearte.presentation.techniques

import edu.ucne.recrearte.data.remote.dto.TechniquesDto

data class TechniqueUiState (
    val techniqueId: Int? = null,
    val techniqueName: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val errorName: String? = null,
    val Techniques: List<TechniquesDto> = emptyList()
)