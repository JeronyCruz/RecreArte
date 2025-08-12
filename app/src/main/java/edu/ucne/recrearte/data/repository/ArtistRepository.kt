package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.local.dao.ArtistDao
import edu.ucne.recrearte.data.local.dao.ArtistListDao
import edu.ucne.recrearte.data.local.entities.ArtistsEntity
import edu.ucne.recrearte.data.local.entities.ArtistsListEntity
import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.ArtistListDto
import edu.ucne.recrearte.data.remote.dto.ArtistsDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ArtistRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val artistDao: ArtistDao,
    private val artistList: ArtistListDao
){
    fun getArtists(): Flow<Resource<List<ArtistListDto>>> = flow {
        try {
            println("[DEBUG] Intentando obtener Artists...")
            emit(Resource.Loading())

            val remoteArtists = remoteDataSource.getArtists()
            println("[DEBUG] API devolvió ${remoteArtists.size} artistas")

            val artistsEntities = remoteArtists.map { it.toEntity() }
            artistList.save(artistsEntities)

            emit(Resource.Success(remoteArtists))

        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                401 -> "Your session has expired. Please log in again."
                403 -> "You do not have permission to access this resource."
                else -> "Server error (${e.code()})"
            }
            println("[DEBUG] Error HTTP: $errorMsg")


            val localArtists = artistList.getAll().map { it.toDto() }
            if (localArtists.isNotEmpty()) {
                println("[DEBUG] Usando datos locales (${localArtists.size} artistas)")
                emit(Resource.Success(localArtists))
            } else {
                emit(Resource.Error(errorMsg))
            }

        } catch (e: IOException) {
            val errorMsg = "Connection error: ${e.message}"
            println("[DEBUG] Network Error: $errorMsg")

            val localArtists = artistList.getAll().map { it.toDto() }
            if (localArtists.isNotEmpty()) {
                println("[DEBUG] Usando datos locales (${localArtists.size} artistas)")
                emit(Resource.Success(localArtists))
            } else {
                emit(Resource.Error(errorMsg))
            }

        } catch (e: Exception) {
            val errorMsg = "Unexpected error: ${e.message}"
            println("[DEBUG] Unexpected Error: $errorMsg")

            val localArtists = artistList.getAll().map { it.toDto() }
            if (localArtists.isNotEmpty()) {
                println("[DEBUG] Usando datos locales (${localArtists.size} artistas)")
                emit(Resource.Success(localArtists))
            } else {
                emit(Resource.Error(errorMsg))
            }
        }
    }
    private fun ArtistListDto.toEntity() = ArtistsListEntity(
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
    )

    // Conversión de Entity a DTO
    private fun ArtistsListEntity.toDto() = ArtistListDto(
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
    )

    fun getArtistById(id: Int): Flow<Resource<ArtistsDto>> = flow {
        var artistDto: ArtistsDto? = null

        try {
            emit(Resource.Loading())

            val remoteArtist = remoteDataSource.getArtistById(id)
            artistDto = remoteArtist
            val artistEntity = remoteArtist.toEntity()

            artistDao.saveOne(artistEntity)

        } catch (e: HttpException) {
            emit(Resource.Error("Internet error: ${e.message()}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }

        val localArtist = artistDao.find(id)
        if (localArtist != null) {
            emit(Resource.Success(localArtist.toDto()))
        } else if (artistDto != null) {
            emit(Resource.Success(artistDto))
        } else {
            emit(Resource.Error("No local or remote data found"))
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