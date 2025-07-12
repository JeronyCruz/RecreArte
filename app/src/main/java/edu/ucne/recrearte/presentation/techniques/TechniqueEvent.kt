package edu.ucne.recrearte.presentation.techniques

sealed interface TechniqueEvent {
    data class TechniquedIdChange(val techniqueId: Int): TechniqueEvent
    data class NameChange(val name: String): TechniqueEvent

    data object GetTechniques: TechniqueEvent
    data object CreateTechnique: TechniqueEvent
    data object New: TechniqueEvent
    data class UpdateTechnique(val id: Int): TechniqueEvent
    data class DeleteTechnique(val id: Int): TechniqueEvent
    data object ResetSuccessMessage: TechniqueEvent
    data object ClearErrorMessage: TechniqueEvent
}