package edu.ucne.recrearte.data.remote.dto

import java.util.Date

data class LikesDto(
    val likeId: Int?,
    val dateLiked: Date,
    val customerId: Int,
    val workId: Int
)