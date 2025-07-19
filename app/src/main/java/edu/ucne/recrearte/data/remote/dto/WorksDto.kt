package edu.ucne.recrearte.data.remote.dto

data class WorksDto(
    val workId: Int?,
    val title: String,
    val dimension: String,
    val techniqueId: Int,
    val artistId: Int,
    val price: Double,
    val description: String,
    val imageId: Int,
    val base64: String? = null
)
