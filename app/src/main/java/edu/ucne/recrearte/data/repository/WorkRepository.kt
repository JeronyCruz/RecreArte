package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import edu.ucne.recrearte.data.remote.dto.WorksDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

class WorkRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    fun getWorks(): Flow<Resource<List<WorksDto>>> = flow {
        try {
            emit(Resource.Loading())
            val work = remoteDataSource.getWorks()
            emit(Resource.Success(work))
        }catch (e: HttpException){
            emit(Resource.Error("Internet error: ${e.message()}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }

    suspend fun getWorkById(id: Int): Resource<WorksDto> {
        return try {
            val work = remoteDataSource.getByIdWork(id)
            Resource.Success(work)
        } catch (e: HttpException) {
            Resource.Error("Internet error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }

    suspend fun createWork(
        title: String,
        dimension: String,
        techniqueId: Int,
        artistId: Int,
        price: Double,
        description: String,
        imageFile: File?
    ): Resource<WorksDto> {
        return try {
            val response = remoteDataSource.createWork(
                title = title,
                dimension = dimension,
                techniqueId = techniqueId,
                artistId = artistId,
                price = price,
                description = description,
                imageFile = imageFile
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty response")
            } else {
                // Intenta parsear el mensaje de error del backend
                val errorBody = response.errorBody()?.string()
                Resource.Error(errorBody ?: "Error ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun updateWork(
        id: Int,
        title: String,
        dimension: String,
        techniqueId: Int,
        artistId: Int,
        price: Double,
        description: String,
        imageFile: File?
    ): Resource<Unit> {
        return try {
            val response = remoteDataSource.updateWork(
                id = id,
                title = title,
                dimension = dimension,
                techniqueId = techniqueId,
                artistId = artistId,
                price = price,
                description = description,
                imageFile = imageFile
            )

            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to update work: ${response.message()}")
            }
        } catch (e: HttpException) {
            Resource.Error("HTTP error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }



    suspend fun deleteWork(id: Int) = remoteDataSource.deleteWork(id)

    fun getWorksByTechnique(techniqueId: Int): Flow<Resource<List<WorksDto>>> = flow {
        try {
            emit(Resource.Loading())
            val work = remoteDataSource.getWorksByTechnique(techniqueId)
            emit(Resource.Success(work))
        }catch (e: HttpException){
            emit(Resource.Error("Internet error: ${e.message()}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }

    fun getWorksByArtist(artistId: Int): Flow<Resource<List<WorksDto>>> = flow {
        try {
            emit(Resource.Loading())
            val work = remoteDataSource.getWorksByArtist(artistId)
            emit(Resource.Success(work))
        }catch (e: HttpException){
            emit(Resource.Error("Internet error: ${e.message()}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }
}