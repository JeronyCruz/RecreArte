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

    fun loadFavorites() {
        val customerId = tokenManager.getUserId() ?: run {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Usuario no autenticado"
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                if (_showWishlist.value) {
                    // Manejar WishList (suspending function)
                    val result = wishListRepository.getWorksInWishlistByCustomer(customerId)
                    handleRepositoryResult(result)
                } else {
                    // Manejar Likes (Flow)
                    likeRepository.getWorksLikedByCustomer(customerId)
                        .collect { result ->
                            handleRepositoryResult(result)
                        }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al cargar favoritos"
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
                        errorMessage = result.message ?: "Error desconocido"
                    )
                }
            }
            is Resource.Loading -> {
                // Opcional: Puedes manejar loading adicional aquí si es necesario
            }
        }
    }
}