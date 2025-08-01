package edu.ucne.recrearte.presentation.work

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.ArtistListDto
import edu.ucne.recrearte.data.remote.dto.ImagesDto
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import edu.ucne.recrearte.data.remote.dto.WorksDto
import edu.ucne.recrearte.data.repository.ArtistRepository
import edu.ucne.recrearte.data.repository.ImageRepository
import edu.ucne.recrearte.data.repository.LikeRepository
import edu.ucne.recrearte.data.repository.TechniqueRepository
import edu.ucne.recrearte.data.repository.WishListRepository
import edu.ucne.recrearte.data.repository.WorkRepository
import edu.ucne.recrearte.util.TokenManager
import edu.ucne.recrearte.util.getUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.collections.emptyList
import kotlin.collections.filter

@HiltViewModel
class WorkViewModel @Inject constructor(
    private val workRepository: WorkRepository,
    private val likeRepository: LikeRepository,
    private val wishListRepository: WishListRepository,
    private val techniqueRepository: TechniqueRepository,
    private val artistRepository: ArtistRepository,
    private val imageRepository: ImageRepository,
    private val tokenManager: TokenManager,
): ViewModel() {
    private val _uiState = MutableStateFlow(WorkUiState())
    val uiSate = _uiState.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading : StateFlow<Boolean> = _loading
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _searchResults = MutableStateFlow<List<WorksDto>>(emptyList())
    val searchResults: StateFlow<List<WorksDto>> = _searchResults.asStateFlow()

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    private val _isInWishlist = MutableStateFlow(false)
    val isInWishlist: StateFlow<Boolean> = _isInWishlist.asStateFlow()

    private val _likeCount = MutableStateFlow(0)
    val likeCount: StateFlow<Int> = _likeCount.asStateFlow()

    private val _showOnlyArtistWorks = MutableStateFlow(false)
    val showOnlyArtistWorks: StateFlow<Boolean> = _showOnlyArtistWorks.asStateFlow()

    private val _selectedImage = MutableStateFlow<File?>(null)
    val selectedImage = _selectedImage.asStateFlow()


    fun onEvent(event: WorkEvent){
        when(event){
            is WorkEvent.ArtistChange -> artistOnChange(event.artistId)
            WorkEvent.ClearErrorMessage -> clearErrorMessage()
            WorkEvent.CreateWork -> createWork()
            is WorkEvent.DeleteWork -> deleteWork(event.id)
            is WorkEvent.DescriptionChange -> descriptionOnChange(event.description)
            is WorkEvent.DimensionChange -> dimensionOnChange(event.dimension)
            WorkEvent.GetWorks -> getWorks()
            WorkEvent.New -> new()
            is WorkEvent.PriceChange -> priceOnChange(event.price)
            WorkEvent.ResetSuccessMessage -> resetSuccessMessage()
            is WorkEvent.TechniqueChange -> techniqueOnChange(event.techniqueId)
            is WorkEvent.TitleChange -> titleOnChange(event.title)
            is WorkEvent.UpdateWork -> updateWork(event.id)
            is WorkEvent.WorkdIdChange -> workIdOnchange(event.workId)
            is WorkEvent.ImageIdChange -> imageOnChange(event.imageId)
            WorkEvent.ToggleLike -> toggleLike()
            WorkEvent.ToggleWishlist -> toggleWishlist()
            is WorkEvent.StatusChange -> statusOnChange(event.statusId)
            is WorkEvent.UpdateWorksStatus -> updateWorksStatus(event.workIds, event.statusId)
        }
    }
    init {
        getWorks()
        getTechniques()
        getArtists()
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

    private fun getLoggedUserId(): Int {
        return tokenManager.getUserId()?.also { userId ->
            println("ID de usuario obtenido del token: $userId")
        } ?: run {
            println("⚠️ No se pudo obtener el ID del usuario - Usando valor por defecto")
            if (System.getProperty("DEBUG") != null) {  // Alternativa para desarrollo
                1 // Valor temporal para desarrollo/debug
            } else {
                throw IllegalStateException("Usuario no autenticado")
            }
        }
    }

    private fun new(){
        _uiState.value = _uiState.value.copy(
            title = "",
            dimension = "",
            artistId = 0,
            techniqueId = 0,
            statusId = 1,
            price = 0.0,
            description = "",
            errorTitle = "",
            errorPrice = "",
            errorDimension = "",
            errorDescription = "",
            errorMessage = "",
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

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun filter(query: String): List<WorksDto> {
        return if (query.isBlank()) {
            _uiState.value.works
        } else {
            _uiState.value.works.filter {
                it.title.contains(query, ignoreCase = true) ||
                        (it.description.contains(query, ignoreCase = true) ?: false)
            }
        }
    }

    private fun workIdOnchange(id: Int){
        _uiState.value = _uiState.value
            .copy(
            workId = id
        )
    }
    private fun titleOnChange(title: String){
        _uiState.value = _uiState.value
            .copy(
                title = title
            )
    }

    private fun imageOnChange(imageId: Int){
        _uiState.value = _uiState.value
            .copy(
            imageId = imageId
        )
    }

    private fun dimensionOnChange(dimension: String){
        _uiState.value = _uiState.value
            .copy(
                dimension = dimension
            )
    }

    private fun descriptionOnChange(description: String){
        _uiState.value = _uiState.value
            .copy(
                description = description
            )
    }

    private fun priceOnChange(price: Double){
        _uiState.value = _uiState.value
            .copy(
                price= price
            )
    }

    private fun artistOnChange(id: Int){
        _uiState.value = _uiState.value
            .copy(
                artistId = id
            )
    }
    private fun techniqueOnChange(id: Int){
        _uiState.value = _uiState.value
            .copy(
                techniqueId = id
            )
    }
    private fun statusOnChange(id: Int){
        _uiState.value = _uiState.value
            .copy(
                statusId = id
            )
    }

    private fun deleteWork(id: Int) {
        viewModelScope.launch {
            try {
                // Primero obtenemos el work para saber el imageId
                val work = workRepository.getWorkById(id)
                if (work is Resource.Success) {
                    workRepository.deleteWork(id)
                    onEvent(WorkEvent.GetWorks)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error deleting: ${e.message}")
            }
        }
    }

    fun getWorks() {
        viewModelScope.launch {
            workRepository.getWorks().collectLatest { getting ->
                when (getting) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                works = getting.data ?: emptyList(),
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
    private fun isValidField(field: String): String? {
        if (field.isBlank()) {
            return "The field cannot be empty."
        }

        val regex = Regex("^[a-zA-Z0-9\\sáéíóúÁÉÍÓÚñÑüÜ.-]*$")
        if (!regex.matches(field)) {
            return "The field contains illegal characters."
        }

        return null
    }

    fun loadWork(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = workRepository.getWorkById(id)) {
                is Resource.Success -> {
                    val customerId = getLoggedUserId()
                    _uiState.value = _uiState.value.copy(
                        workId = result.data?.workId,
                        techniqueId = result.data?.techniqueId ?: 0,
                        title = result.data?.title ?: "",
                        dimension = result.data?.dimension ?: "",
                        description = result.data?.description ?: "",
                        price = result.data?.price ?: 0.0,
                        isLoading = false,
                        imageRemoved = false
                    )
                    loadLikeStatus(id, customerId)
                }
                // ... (keep rest of the cases)
                is Resource.Error<*> -> TODO()
                is Resource.Loading<*> -> TODO()
            }
        }
    }

    //para los artistas
    fun findArtist(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = artistRepository.getArtistById(id)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        artistId = result.data?.artistId ?: 0,
                        nameArtist = result.data?.userName ?: "A",
                        errorMessage = null
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

    fun createWork() {
        val loggedArtistId = tokenManager.getUserId() ?: throw IllegalStateException("User not logged in")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = workRepository.createWork(
                title = _uiState.value.title,
                dimension = _uiState.value.dimension,
                techniqueId = _uiState.value.techniqueId,
                artistId = loggedArtistId,
                price = _uiState.value.price,
                description = _uiState.value.description,
                imageFile = _selectedImage.value
            )

            _uiState.value = when (result) {
                is Resource.Success -> {
                    _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        successMessage = "Obra creada"
                    )
                }
                is Resource.Error -> {
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }

                is Resource.Loading<*> -> TODO()
            }
        }
    }

    fun selectImage(file: File) {
        _selectedImage.value = file
    }



    private fun updateWork(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = workRepository.updateWork(
                id = id,
                title = _uiState.value.title,
                dimension = _uiState.value.dimension,
                techniqueId = _uiState.value.techniqueId,
                artistId = _uiState.value.artistId,
                price = _uiState.value.price,
                description = _uiState.value.description,
                imageFile = _selectedImage.value
            )

            _uiState.value = when (result) {
                is Resource.Success -> {
                    _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        successMessage = "Obra actualizada"
                    )
                }
                is Resource.Error -> {
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Resource.Loading<*> -> _uiState.value
            }
        }
    }

    //Para los select
    val techniques: StateFlow<Resource<List<TechniquesDto>>> = techniqueRepository.getTechniques()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading()
        )

    val artists: StateFlow<Resource<List<ArtistListDto>>> = artistRepository.getArtists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading()
        )
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

    private fun getArtists() {
        viewModelScope.launch {
            artistRepository.getArtists().collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        val artists = result.data ?: emptyList()
                        println("Artists fetched: $artists") // Debug log
                        _uiState.update {
                            it.copy(artists = artists)
                        }
                    }
                    is Resource.Error -> {
                        println("Error fetching artists: ${result.message}") // Debug log
                        _uiState.update {
                            it.copy(errorMessage = result.message ?: "Error fetching artists")
                        }
                    }
                    is Resource.Loading -> {
                        println("Loading artists...") // Debug log
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    // Cambia la definición de toggleLike en el ViewModel
    private fun toggleLike() {
        viewModelScope.launch {
            try {
                val workId = _uiState.value.workId ?: return@launch
                val customerId = getLoggedUserId()

                // Toggle the like
                when (val result = likeRepository.toggleLike(customerId, workId)) {
                    is Resource.Success -> {
                        // Update like status
                        _isLiked.value = result.data ?: false

                        // Refresh like count
                        when (val countResult = likeRepository.getLikeCountForWork(workId)) {
                            is Resource.Success -> _likeCount.value = countResult.data ?: 0
                            is Resource.Error -> _uiState.update {
                                it.copy(errorMessage = countResult.message)
                            }

                            is Resource.Loading<*> -> TODO()
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(errorMessage = result.message) }
                    }

                    is Resource.Loading<*> -> TODO()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error: ${e.message}") }
            }
        }
    }

    private fun toggleWishlist() {
        viewModelScope.launch {
            try {
                val workId = _uiState.value.workId ?: return@launch
                val customerId = getLoggedUserId()

                when (val result = wishListRepository.toggleWorkInWishlist(customerId, workId)) {
                    is Resource.Success -> {
                        _isInWishlist.value = result.data ?: false
                        // You might want to show a snackbar or other feedback here
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(errorMessage = result.message) }
                    }

                    is Resource.Loading<*> -> TODO()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error: ${e.message}") }
            }
        }
    }

    fun loadLikeStatus(workId: Int, customerId: Int) {
        viewModelScope.launch {
            // Cargar estado de like
            when (val result = likeRepository.hasCustomerLikedWork(customerId, workId)) {
                is Resource.Success -> {
                    _isLiked.value = result.data ?: false
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                else -> {}
            }

            // Cargar contador de likes - CORRECCIÓN AQUÍ
            when (val result = likeRepository.getLikeCountForWork(workId)) {
                is Resource.Success -> {
                    _likeCount.value = result.data ?: 0 // Usamos result.data en lugar de result
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                else -> {}
            }

            // Cargar estado de wishlist
            when (val result = wishListRepository.isWorkInWishlist(customerId, workId)) {
                is Resource.Success -> {
                    _isInWishlist.value = result.data ?: false
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }

    //para actualizar el status
    private fun updateWorksStatus(workIds: List<Int>, statusId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                workIds.forEach { workId ->
                    // Get the current work
                    when (val result = workRepository.getWorkById(workId)) {
                        is Resource.Success -> {
                            result.data?.let { work ->
                                // Update only the statusId
                                val updatedWork = work.copy(statusId = statusId)
                                workRepository.updateWork(
                                    id = workId,
                                    title = updatedWork.title,
                                    dimension = updatedWork.dimension,
                                    techniqueId = updatedWork.techniqueId,
                                    artistId = updatedWork.artistId,
                                    price = updatedWork.price,
                                    description = updatedWork.description,
                                    imageFile = null // No image update needed for status change
                                )
                            }
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                            return@launch
                        }
                        is Resource.Loading<*> -> {}
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    successMessage = "Estados actualizados correctamente"
                )

                // Refresh works list
                getWorks()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error updating works status: ${e.message}"
                )
            }
        }
    }
}