package edu.ucne.recrearte.presentation.work

import edu.ucne.recrearte.data.remote.dto.ImagesDto
import edu.ucne.recrearte.presentation.techniques.TechniqueEvent

sealed interface WorkEvent {
    data class WorkdIdChange(val workId: Int): WorkEvent
    data class TitleChange(val title: String): WorkEvent
    data class DimensionChange(val dimension: String): WorkEvent
    data class TechniqueChange(val techniqueId: Int): WorkEvent
    data class ArtistChange(val artistId: Int): WorkEvent
    data class PriceChange(val price: Double): WorkEvent
    data class DescriptionChange(val description: String): WorkEvent
    data class ImageIdChange(val imageId: Int) : WorkEvent

    data object GetWorks: WorkEvent
    data object CreateWork: WorkEvent

    data class ImageCreate(val image: ImagesDto) : WorkEvent

    data object New: WorkEvent
    data class UpdateWork(val id: Int): WorkEvent
    data class DeleteWork(val id: Int): WorkEvent
    data object ResetSuccessMessage: WorkEvent
    data object ClearErrorMessage: WorkEvent
}