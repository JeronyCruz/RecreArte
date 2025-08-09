package edu.ucne.recrearte.presentation.Like_WishList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.WorksDto
import edu.ucne.recrearte.data.repository.LikeRepository
import edu.ucne.recrearte.data.repository.WishListRepository
import edu.ucne.recrearte.util.TokenManager
import edu.ucne.recrearte.util.getUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val likeRepository: LikeRepository,
    private val wishListRepository: WishListRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState = _uiState.asStateFlow()

    private val _showWishlist = MutableStateFlow(false)
    val showWishlist = _showWishlist.asStateFlow()

    init {
        loadFavorites()
    }

    fun toggleShowWishlist() {
        _showWishlist.update { !it }
        loadFavorites()
    }
    fun toggleLike(workId: Int) {
        val customerId = tokenManager.getUserId() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = likeRepository.toggleLike(customerId, workId)) {
                is Resource.Success -> {
                    // Recargar los favoritos después del cambio
                    loadFavorites()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Error toggling like"
                        )
                    }
                }
                is Resource.Loading -> {}
            }
            }
        }

    fun loadFavorites() {
        val customerId = tokenManager.getUserId() ?: run {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Unauthenticated user"
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val result = if (_showWishlist.value) {
                    // Para Wishlist
                    wishListRepository.getWorksInWishlistByCustomer(customerId)
                } else {
                    // Para Likes
                    var resource: Resource<List<WorksDto>> = Resource.Loading()
                    likeRepository.getWorksLikedByCustomer(customerId)
                        .collect { flowResult ->
                            resource = when (flowResult) {
                                is Resource.Success -> Resource.Success(flowResult.data ?: emptyList())
                                is Resource.Error -> Resource.Error(flowResult.message ?: "Unknown error")
                                is Resource.Loading -> Resource.Loading()
                            }
                        }
                    resource
                }

                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                works = result.data ?: emptyList(),
                                isLoading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message ?: "Error loading favorites"
                            )
                        }
                    }
                    is Resource.Loading -> {
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error loading favorites"
                    )
                }
            }
        }
    }

    private fun handleRepositoryResult(result: Resource<List<WorksDto>>) {
        when (result) {
            is Resource.Success -> {
                _uiState.update {
                    it.copy(
                        works = result.data ?: emptyList(),
                        isLoading = false
                    )
                }
            }
            is Resource.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Unknown error"
                    )
                }
            }
            is Resource.Loading -> {
                // Opcional: Puedes manejar loading adicional aquí si es necesario
            }
        }
    }
}