package edu.ucne.recrearte.data.remote

import java.io.File

interface CloudinaryService {
    suspend fun uploadImage(file: File): String?
}