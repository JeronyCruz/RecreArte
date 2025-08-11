package edu.ucne.recrearte.presentation.techniques

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import edu.ucne.recrearte.data.repository.TechniqueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TechniqueViewModel @Inject constructor(
    private val repository: TechniqueRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(TechniqueUiState())
    val uiSate = _uiState.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading : StateFlow<Boolean> = _loading
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _searchResults = MutableStateFlow<List<TechniquesDto>>(emptyList())
    val searchResults: StateFlow<List<TechniquesDto>> = _searchResults.asStateFlow()

    fun onEvent(event: TechniqueEvent){
        when(event) {
            TechniqueEvent.ClearErrorMessage -> clearErrorMessage()
            TechniqueEvent.CreateTechnique -> createPaymentMethod()
            is TechniqueEvent.DeleteTechnique -> deleteTechnique(event.id)
            TechniqueEvent.GetTechniques -> getTechniques()
            is TechniqueEvent.NameChange -> nameOnChange(event.name)
            TechniqueEvent.New -> new()
            TechniqueEvent.ResetSuccessMessage -> resetSuccessMessage()
            is TechniqueEvent.TechniquedIdChange -> techniqueIdOnChange(event.techniqueId)
            is TechniqueEvent.UpdateTechnique -> updateTechnique(event.id)
        }
    }
    init {
        getTechniques()
        //Para la busqueda
        viewModelScope.launch {
            _searchQuery
                .debounce(600)
                .distinctUntilChanged()
                .mapLatest { query ->
                    filter(query)
                }
                .collectLatest { filtered ->
                    _searchResults.value = filtered
                }
        }

    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun filter(query: String): List<TechniquesDto> {
        return if (query.isBlank()) {
            _uiState.value.Techniques
        } else {
            _uiState.value.Techniques.filter {
                it.techniqueName.contains(query, ignoreCase = true)
            }
        }
    }

    private fun nameOnChange(name: String){
        _uiState.value = _uiState.value
            .copy(
                techniqueName = name,
                errorMessage = null
            )
    }

    private fun techniqueIdOnChange(id: Int){
        _uiState.value = _uiState.value
            .copy(techniqueId = id)
    }

    private fun createPaymentMethod(){
        val name = _uiState.value.techniqueName.trim()
        val validationError = isValidTechniqueName(name)

        if (validationError != null) {
            _uiState.value = _uiState.value.copy(errorMessage = validationError)
            return
        }
        viewModelScope.launch {
            try {
                val method = TechniquesDto(
                    techniqueId = 0,
                    techniqueName = _uiState.value.techniqueName
                )
                repository.createTechnique(method)
                _uiState.value = _uiState.value.copy(
                    isSuccess = true,
                    successMessage = "Technique created successfully",
                    techniqueName = "",
                    techniqueId = null
                )
                onEvent(TechniqueEvent.GetTechniques)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error creating: ${e.message}")
            }
        }
    }
    //revisar
    private fun updateTechnique(id: Int){
        val name = _uiState.value.techniqueName.trim()
        val validationError = isValidTechniqueName(name)

        if (validationError != null) {
            _uiState.value = _uiState.value.copy(errorMessage = validationError)
            return
        }

        viewModelScope.launch {
            try {
                val method = TechniquesDto(
                    techniqueId = id,
                    techniqueName = _uiState.value.techniqueName
                )
                repository.updateTechnique(id, method)
                _uiState.value = _uiState.value.copy(
                    isSuccess = true,
                    successMessage = "Technique updated successfully"
                )
               onEvent(TechniqueEvent.GetTechniques)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error updating: ${e.message}")
            }
        }
    }

    private fun new(){
        _uiState.value = _uiState.value.copy(
            techniqueId = null,
            techniqueName = "",
            errorMessage = null,
            isSuccess = false,
            successMessage = null
        )
    }

    private fun resetSuccessMessage(){
        _uiState.value = _uiState.value
            .copy(
                isSuccess = false, successMessage = null
            )
    }

    private fun clearErrorMessage(){
        _uiState.value = _uiState.value
            .copy(errorMessage = null)
    }

    private fun deleteTechnique(id: Int) {
        viewModelScope.launch {
            try {
                val isDeleted = repository.deleteTechnique(id)
                if (isDeleted) {
                    _uiState.value = _uiState.value.copy(
                        isSuccess = true,
                        successMessage = "Technique successfully removed"
                    )
                    onEvent(TechniqueEvent.GetTechniques) // Refresca la lista
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to delete technique"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error deleting technique: ${e.message}"
                )
            }
        }
    }

    fun getTechniques(){
        viewModelScope.launch {
            repository.getTechniques().collectLatest { getting ->
                when (getting){
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }

                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                Techniques = getting.data ?: emptyList(),
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun isValidTechniqueName(name: String): String? {
        if (name.isBlank()) {
            return "The name cannot be empty."
        }

        val regex = Regex("^[a-zA-Z0-9\\sáéíóúÁÉÍÓÚñÑüÜ.-]*$")
        if (!regex.matches(name)) {
            return "The name contains illegal characters."
        }

        return null
    }

    fun loadTechnique(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.getTechniqueById(id)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        techniqueId = result.data?.techniqueId,
                        techniqueName = result.data?.techniqueName ?: "",
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message,
                        isLoading = false
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }
}