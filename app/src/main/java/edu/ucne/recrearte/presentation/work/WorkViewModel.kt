package edu.ucne.recrearte.presentation.work

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.recrearte.data.remote.NetworkMonitor
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.ArtistListDto
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import edu.ucne.recrearte.data.remote.dto.WorksDto
import edu.ucne.recrearte.data.repository.ArtistRepository
import edu.ucne.recrearte.data.repository.LikeRepository
import edu.ucne.recrearte.data.repository.TechniqueRepository
import edu.ucne.recrearte.data.repository.WishListRepository
import edu.ucne.recrearte.data.repository.WorkRepository
import edu.ucne.recrearte.presentation.techniques.TechniqueEvent
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WorkViewModel @Inject constructor(
    private val workRepository: WorkRepository,
    private val likeRepository: LikeRepository,
    private val wishListRepository: WishListRepository,
    private val techniqueRepository: TechniqueRepository,
    private val artistRepository: ArtistRepository,
    private val tokenManager: TokenManager,
    val networkMonitor: NetworkMonitor
): ViewModel() {
    private val _connectionError = MutableStateFlow(false)
    val connectionError: StateFlow<Boolean> = _connectionError.asStateFlow()

    private val _isCachedData = MutableStateFlow(false)
    val isCachedData: StateFlow<Boolean> = _isCachedData.asStateFlow()

    private val _networkMessage = MutableStateFlow<String?>(null)
    val networkMessage: StateFlow<String?> = _networkMessage.asStateFlow()

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (!isOnline) {
                    _connectionError.value = true
                }
            }
        }
    }
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
                throw IllegalStateException("Unauthenticated user")
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
                val isDeleted = workRepository.deleteWork(id)
                if (isDeleted) {
                    _uiState.value = _uiState.value.copy(
                        isSuccess = true,
                        successMessage = "Work successfully removed"
                    )
                    onEvent(WorkEvent.GetWorks) // Refresca la lista
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to delete work"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error deleting work: ${e.message}"
                )
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

    private fun updateUiState(work: WorksDto?, isFromCache: Boolean = false) {
        work?.let {
            _uiState.value = _uiState.value.copy(
                workId = it.workId,
                title = it.title ?: "",
                dimension = it.dimension ?: "",
                techniqueId = it.techniqueId ?: 0,
                artistId = it.artistId ?: 0,
                statusId = it.statusId ?: 1,
                price = it.price ?: 0.0,
                description = it.description ?: "",
                imageUrl = it.imageUrl ?: "",
                isCached = isFromCache,
                networkMessage = if (isFromCache) "Data stored locally" else null,
                lastUpdated = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                isLoading = false
            )
        }
    }

    fun loadWork(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val workResult = if (networkMonitor.isOnline.value) {
                    workRepository.getWorkById(id)
                } else {
                    workRepository.getWorkById(id)
                }

                when (workResult) {
                    is Resource.Success -> {
                        val work = workResult.data
                        val customerId = getLoggedUserId()

                        _uiState.value = _uiState.value.copy(
                            workId = work?.workId,
                            techniqueId = work?.techniqueId ?: 0,
                            title = work?.title ?: "",
                            dimension = work?.dimension ?: "",
                            description = work?.description ?: "",
                            price = work?.price ?: 0.0,
                            isLoading = false,
                            imageRemoved = false
                        )

                        loadLikeStatus(id, customerId)
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = workResult.message ?: "Error loading the work"
                        )
                    }
                    is Resource.Loading -> {
                        // Manejar estado de carga si es necesario
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadFromCache(id: Int) {
        when (val cachedResult = workRepository.getWorkById(id)) {
            is Resource.Success -> {
                updateUiState(cachedResult.data, true)
                _uiState.value = _uiState.value.copy(
                    networkMessage = "Connect for updated data",
                    showRetryButton = true
                )
            }
            is Resource.Error -> {
                _uiState.value = _uiState.value.copy(
                    networkMessage = cachedResult.message ?: "Error loading data",
                    showRetryButton = true,
                    isLoading = false
                )
            }

            is Resource.Loading<*> -> TODO()
        }
    }

    fun retryLoadWork(id: Int) {
        _uiState.value = _uiState.value.copy(
            networkMessage = "Trying to connect...",
            showRetryButton = false
        )
        loadWork(id)
    }

    fun clearNetworkMessage() {
        _uiState.value = _uiState.value.copy(networkMessage = null)
    }

    //para los artistas
    fun findArtist(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            artistRepository.getArtistById(id).collect { result ->
                when (result) {
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
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun createWork() {
        val loggedArtistId = tokenManager.getUserId() ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Unauthenticated user",
                isLoading = false
            )
            return
        }

        val titleError = isValidField(_uiState.value.title)
        val dimensionError = isValidField(_uiState.value.dimension)
        val descriptionError = isValidField(_uiState.value.description)
        val priceError = if (_uiState.value.price <= 0.0) "The price must be greater than zero" else null
        val techniqueError = if (_uiState.value.techniqueId <= 0) "Select a technique" else null


        _uiState.value = _uiState.value.copy(
            errorTitle = titleError ?: "",
            errorDimension = dimensionError ?: "",
            errorDescription = descriptionError ?: "",
            errorPrice = priceError ?: "",
            errorMessage = techniqueError ?: "",
            artistId = loggedArtistId
        )

        if (
            titleError != null ||
            dimensionError != null ||
            descriptionError != null ||
            priceError != null ||
            techniqueError != null
        ) return

        if (listOf(titleError, dimensionError, descriptionError, priceError, techniqueError).all { it == null }){
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
                            successMessage = "Work successfully created"
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Unknown error while creating the work"
                        )
                    }
                    is Resource.Loading -> {
                        _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun selectImage(file: File) {
        _selectedImage.value = file
    }



    private fun updateWork(id: Int) {
        val loggedArtistId = tokenManager.getUserId() ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Unauthenticated user",
                isLoading = false
            )
            return
        }

        val titleError = isValidField(_uiState.value.title)
        val dimensionError = isValidField(_uiState.value.dimension)
        val descriptionError = isValidField(_uiState.value.description)
        val priceError = if (_uiState.value.price <= 0.0) "The price must be greater than zero" else null
        val techniqueError = if (_uiState.value.techniqueId <= 0) "Select a technique" else null


        _uiState.value = _uiState.value.copy(
            errorTitle = titleError ?: "",
            errorDimension = dimensionError ?: "",
            errorDescription = descriptionError ?: "",
            errorPrice = priceError ?: "",
            errorMessage =  techniqueError ?: "",
            artistId = loggedArtistId
        )

        if (
            titleError != null ||
            dimensionError != null ||
            descriptionError != null ||
            priceError != null ||
            techniqueError != null
        ) return

        if (listOf(titleError, dimensionError, descriptionError, priceError, techniqueError).all { it == null }){
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)

                try {
                    // Obtener la obra actual para asegurar que tenemos todos los datos
                    when (val currentWorkResult = workRepository.getWorkById(id)) {
                        is Resource.Success -> {
                            currentWorkResult.data?.let { currentWork ->
                                val result = workRepository.updateWork(
                                    workId = id,
                                    title = _uiState.value.title,
                                    dimension = _uiState.value.dimension,
                                    techniqueId = _uiState.value.techniqueId,
                                    artistId = loggedArtistId,
                                    price = _uiState.value.price,
                                    description = _uiState.value.description,
                                    statusId = 1,
                                    imageFile = _selectedImage.value
                                )

                                _uiState.value = when (result) {
                                    is Resource.Success -> {
                                        _selectedImage.value = null
                                        _uiState.value.copy(
                                            isLoading = false,
                                            isSuccess = true,
                                            successMessage = "Work successfully updated",
                                            works = _uiState.value.works.map {
                                                if (it.workId == id) {
                                                    it.copy(
                                                        title = _uiState.value.title,
                                                        dimension = _uiState.value.dimension,
                                                        techniqueId = _uiState.value.techniqueId,
                                                        price = _uiState.value.price,
                                                        description = _uiState.value.description,
                                                        statusId = _uiState.value.statusId
                                                    )
                                                } else {
                                                    it
                                                }
                                            }
                                        )
                                    }
                                    is Resource.Error -> {
                                        _uiState.value.copy(
                                            isLoading = false,
                                            errorMessage = result.message ?: "Error updating the work"
                                        )
                                    }
                                    else -> _uiState.value.copy(isLoading = false)
                                }
                            } ?: run {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "The current work could not be obtained"
                                )
                            }
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = currentWorkResult.message ?: "Error getting the current work"
                            )
                        }
                        else -> _uiState.value.copy(isLoading = false)
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Unexpected error: ${e.message}"
                    )
                }
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

            // Cargar contador de likes
            when (val result = likeRepository.getLikeCountForWork(workId)) {
                is Resource.Success -> {
                    _likeCount.value = result.data ?: 0
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
                    when (val result = workRepository.getWorkById(workId)) {
                        is Resource.Success -> {
                            result.data?.let { work ->
                                val updatedWork = work.copy(statusId = statusId)

                                workRepository.updateWork(
                                    workId = workId,
                                    title = updatedWork.title,
                                    dimension = updatedWork.dimension,
                                    techniqueId = updatedWork.techniqueId,
                                    artistId = updatedWork.artistId,
                                    price = updatedWork.price,
                                    description = updatedWork.description,
                                    statusId = 2,
                                    imageFile = null // No actualizamos la imagen
                                )
                            }
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = result.message ?: "Error getting the work to update"
                            )
                            return@launch
                        }
                        is Resource.Loading -> {
                            // Podemos manejar el estado de carga si es necesario
                        }
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    successMessage = "States updated successfully"
                )

                // Refresh works list
                getWorks()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error updating states: ${e.message}"
                )
            }
        }
    }
}