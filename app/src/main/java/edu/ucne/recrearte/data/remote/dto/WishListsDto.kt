package edu.ucne.recrearte.data.remote.dto

data class WishListsDto(
    val wishListId: Int,
    val customerId: Int,
    val userName: String?,
    val wishListDetails: List<WishListDetailsDto> = emptyList()
)
