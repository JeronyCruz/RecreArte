package edu.ucne.recrearte.data.remote.dto

data class ImagesDto(
    val imageId: Int?,
    val workId: Int = 0,
    val title: String,
    val base64: String?
)