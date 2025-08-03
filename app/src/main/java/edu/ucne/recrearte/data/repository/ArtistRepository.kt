package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.local.dao.ArtistDao
import edu.ucne.recrearte.data.local.dao.UserDao
import edu.ucne.recrearte.data.local.entities.ArtistsEntity
import edu.ucne.recrearte.data.local.entities.UsersEntity
import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.ArtistListDto
import edu.ucne.recrearte.data.remote.dto.ArtistsDto
import edu.ucne.recrearte.data.remote.dto.CustomersDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.util.Date
import javax.inject.Inject

class ArtistRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val artistDao: ArtistDao
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

    fun getArtistById(id: Int): Flow<Resource<ArtistsDto>> = flow {
        var artistDto: ArtistsDto? = null

        try {
            emit(Resource.Loading())

            // Obtener datos remotos
            val remoteArtist = remoteDataSource.getArtistById(id)
            artistDto = remoteArtist
            val artistEntity = remoteArtist.toEntity()

            // Guardar en base de datos local
            artistDao.saveOne(artistEntity)

        } catch (e: HttpException) {
            emit(Resource.Error("Internet error: ${e.message()}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }

        // Obtener datos locales (ya sea como fallback o después de éxito remoto)
        val localArtist = artistDao.find(id)
        if (localArtist != null) {
            emit(Resource.Success(localArtist.toDto()))
        } else if (artistDto != null) {
            // Si tenemos datos remotos pero no locales (caso raro)
            emit(Resource.Success(artistDto))
        } else {
            // No hay datos ni locales ni remotos
            emit(Resource.Error("No se encontraron datos locales ni remotos"))
        }
    }

    private fun ArtistsDto.toEntity() = ArtistsEntity(
         artistId = this.artistId,
         artStyle = this.artStyle ?: "",
         socialMediaLinks = this.socialMediaLinks ?: "",
         firstName  = this.firstName ?: "",
         lastName = this.lastName ?: "",
         email = this.email ?: "",
         password  = this.password ?: "",
         userName = this.userName ?: "",
         phoneNumber = this.phoneNumber ?: "",
         documentNumber = this.documentNumber ?: "",
         updateAt = this.updateAt,
         roleId = this.roleId,
         description = this.description ?: "",
         token = this.token ?: ""
    )

    private fun ArtistsEntity.toDto() = ArtistsDto(
        artistId = this.artistId,
        artStyle = this.artStyle ?: "",
        socialMediaLinks = this.socialMediaLinks ?: "",
        firstName  = this.firstName ?: "",
        lastName = this.lastName ?: "",
        email = this.email ?: "",
        password  = this.password ?: "",
        userName = this.userName ?: "",
        phoneNumber = this.phoneNumber ?: "",
        documentNumber = this.documentNumber ?: "",
        updateAt = this.updateAt,
        roleId = this.roleId,
        description = this.description ?: "",
        token = this.token ?: ""
    )

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

    suspend fun updateArtist(id: Int, artistsDto: ArtistsDto): Resource<Unit> {
        return try {
            remoteDataSource.updateArtist(id, artistsDto)
            Resource.Success(Unit) // Indicamos éxito sin datos
        } catch (e: HttpException) {
            Resource.Error("HTTP error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Update failed: ${e.message}")
        }
    }
    suspend fun changePassword(userId: Int, currentPassword: String, newPassword: String, confirmPassword: String): Boolean {
        return remoteDataSource.changePassword(userId, currentPassword, newPassword, confirmPassword)
    }

    suspend fun deleteArtist(id: Int) = remoteDataSource.deleteArtist(id)
}