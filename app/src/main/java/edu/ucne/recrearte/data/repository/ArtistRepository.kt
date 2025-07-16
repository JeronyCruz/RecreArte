package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.ArtistListDto
import edu.ucne.recrearte.data.remote.dto.ArtistsDto
import edu.ucne.recrearte.data.remote.dto.CustomersDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ArtistRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
){
    fun getArtists(): Flow<Resource<List<ArtistListDto>>> = flow {
        try {
            println("[DEBUG] Intentando obtener Artists...")
            emit(Resource.Loading())

            val methods = remoteDataSource.getArtists()
            println("[DEBUG] API devolvio ${methods.size} Artist")

            emit(Resource.Success(methods))
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                401 -> "Tu sesión ha expirado. Por favor inicia sesión nuevamente"
                403 -> "No tienes permiso para acceder a este recurso"
                else -> "Error del servidor (${e.code()})"
            }
            println("[DEBUG] Error HTTP: $errorMsg")
            emit(Resource.Error(errorMsg))
        } catch (e: IOException) {
            val errorMsg = "Error de conexión: ${e.message}"
            println("[DEBUG] Network Error: $errorMsg")
            emit(Resource.Error(errorMsg))
        } catch (e: Exception) {
            val errorMsg = "Error inesperado: ${e.message}"
            println("[DEBUG] Unexpected Error: $errorMsg")
            emit(Resource.Error(errorMsg))
        }catch (e: HttpException){
            emit(Resource.Error("Internet error: ${e.message()}"))
        }
    }

    suspend fun getArtistById(id: Int): Resource<ArtistsDto> {
        return try {
            val artist = remoteDataSource.getArtistById(id)
            Resource.Success(artist)
        } catch (e: HttpException) {
            Resource.Error("Internet error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }

    suspend fun createArtist(artistsDto: ArtistsDto): Resource<ArtistsDto> { // Cambia el tipo de retorno
        return try {
            val response = remoteDataSource.createArtist(artistsDto)
            Resource.Success(response) // Devuelve el DTO completo con el token
        } catch (e: HttpException) {
            Resource.Error("Internet error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }

    suspend fun updateArtist(id: Int, artistsDto: ArtistsDto) = remoteDataSource.updateArtist(id, artistsDto)

    suspend fun deleteArtist(id: Int) = remoteDataSource.deleteArtist(id)
}