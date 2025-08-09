package edu.ucne.recrearte.data.remote.dto

data class WishListDetailsDto(
    val wishListDetailId: Int,
    val wishListId: Int,
    val workId: Int,
    val work: WorksDto
//    = WorksDto()
)