package edu.ucne.recrearte.presentation.Home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.WorksDto
import edu.ucne.recrearte.data.repository.ArtistRepository
import edu.ucne.recrearte.data.repository.ImageRepository
import edu.ucne.recrearte.data.repository.LikeRepository
import edu.ucne.recrearte.data.repository.TechniqueRepository
import edu.ucne.recrearte.data.repository.WorkRepository
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
class HomeViewModel @Inject constructor(
    private val workRepository: WorkRepository,
    private val likeRepository: LikeRepository,
    private val artistRepository: ArtistRepository,
    private val imageRepository: ImageRepository,
    private val techniqueRepository: TechniqueRepository
): ViewModel(){
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiSate = _uiState.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading : StateFlow<Boolean> = _loading
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _searchResults = MutableStateFlow<List<WorksDto>>(emptyList())
    val searchResults: StateFlow<List<WorksDto>> = _searchResults.asStateFlow()

    fun onEvent(event: HomeEvent){
        when(event) {
            HomeEvent.GetArtists -> getArtists()
            HomeEvent.GetTop10MostLikedWorks -> getTop10Works()
            is HomeEvent.GetWorksByArtist -> getWorksByArtist(event.artistId)
            is HomeEvent.GetWorksByTechnique -> getWorksByTechnique(event.techinqueId)
            HomeEvent.GetTechniques -> getTechniques()
        }
    }

    init {
        getWorks()
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

    private suspend fun filter(query: String): List<WorksDto> {
        val currentList = when {
            _uiState.value.worksByTechnique.isNotEmpty() -> _uiState.value.worksByTechnique
            _uiState.value.worksByArtistsDto.isNotEmpty() -> _uiState.value.worksByArtistsDto
            else -> _uiState.value.works
        }

        val filtered = if (query.isBlank()) {
            currentList
        } else {
            currentList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        (it.description?.contains(query, ignoreCase = true) ?: false)
            }
        }

        return filtered.map { work ->
            if (work.imageId > 0 && work.base64.isNullOrEmpty()) {
                try {
                    val image = imageRepository.getImageById(work.imageId)
                    work.copy(base64 = image.data?.base64 ?: "")
                } catch (e: Exception) {
                    work.copy(base64 = "")
                }
            } else {
                work
            }
        }
    }

    private fun getWorks() {
        viewModelScope.launch {
            workRepository.getWorks().collectLatest { getting ->
                when (getting) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        // Procesar las imágenes
                        val worksWithImages = getting.data?.map { work ->
                            if (work.imageId > 0) {
                                try {
                                    val image = imageRepository.getImageById(work.imageId)
                                    work.copy(base64 = image.data?.base64 ?: "")
                                } catch (e: Exception) {
                                    work.copy(base64 = "")
                                }
                            } else {
                                work
                            }
                        } ?: emptyList()

                        _uiState.update {
                            it.copy(
                                works = worksWithImages,
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false)
                        }
                    }
                }
            }
        }
    }

    private fun getTop10Works() {
        viewModelScope.launch {
            likeRepository.getTop10MostLikedWorks().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                listTopTen = result.data ?: emptyList(),
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                errorMessage = result.message,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getWorksByArtist(artistId: Int) {
        viewModelScope.launch {
            workRepository.getWorksByArtist(artistId).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }

                    is Resource.Success -> {
                        val worksWithImages = result.data?.map { work ->
                            if (work.imageId > 0) {
                                try {
                                    val image = imageRepository.getImageById(work.imageId)
                                    work.copy(base64 = image.data?.base64 ?: "")
                                } catch (e: Exception) {
                                    work.copy(base64 = "")
                                }
                            } else {
                                work
                            }
                        } ?: emptyList()

                        _uiState.update {
                            it.copy(
                                worksByArtistsDto = worksWithImages,
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                errorMessage = result.message,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }


    private fun getWorksByTechnique(techniqueId: Int) {
        viewModelScope.launch {
            workRepository.getWorksByTechnique(techniqueId).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }

                    is Resource.Success -> {
                        val worksWithImages = result.data?.map { work ->
                            if (work.imageId > 0) {
                                try {
                                    val image = imageRepository.getImageById(work.imageId)
                                    work.copy(base64 = image.data?.base64 ?: "")
                                } catch (e: Exception) {
                                    work.copy(base64 = "")
                                }
                            } else {
                                work
                            }
                        } ?: emptyList()

                        _uiState.update {
                            it.copy(
                                worksByTechnique = worksWithImages,
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                errorMessage = result.message,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }


    private fun getArtists() {
        viewModelScope.launch {
            artistRepository.getArtists().collectLatest { result ->
                if (result is Resource.Success) {
                    _uiState.update {
                        it.copy(listArtist = result.data ?: emptyList())
                    }
                }
            }
        }
    }

    private fun getTechniques() {
        viewModelScope.launch {
            techniqueRepository.getTechniques().collectLatest { result ->
                if (result is Resource.Success) {
                    _uiState.update {
                        it.copy(techniquesL = result.data ?: emptyList())
                    }
                }
            }
        }
    }
}