package edu.ucne.recrearte.data.repository

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import edu.ucne.recrearte.data.local.dao.WorkDao
import edu.ucne.recrearte.data.local.entities.WorksEntity
import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.WorksDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.File
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class WorkRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val worksDao: WorkDao
) {
    fun getWorks(): Flow<Resource<List<WorksDto>>> = flow {

        var listWorksDto: List<WorksEntity> = emptyList()

        try {
            emit(Resource.Loading())
            val work = remoteDataSource.getWorks()
            val worksEntity = work.map {
                it.toEntity()
            }
            work.forEach { workDto ->
                println("Work ID: ${workDto.workId}, Image URL: ${workDto.imageUrl}")
            }

            worksDao.save(worksEntity)

        }catch (e: HttpException){
            emit(Resource.Error("Internet error: ${e.message()}"))
        }catch (e: ConnectException) {
            // No emitir error cuando no hay conexión
            emit(Resource.Error("No tienes conexion a internet"))
        }catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
        listWorksDto = worksDao.getAll()
        val finalList = listWorksDto.map {
            it.toDto()
        }

        emit(Resource.Success(finalList))
    }


    suspend fun getWorkById(id: Int): Resource<WorksDto> {
        var workDto: WorksDto? = null

        try {
            val remoteWork = remoteDataSource.getByIdWork(id)
            workDto = remoteWork
            val workEntity = remoteWork.toEntity()

            worksDao.saveOne(workEntity)

        } catch (e: HttpException) {

            val localWork = worksDao.find(id)
            return if (localWork != null) {
                Resource.Success(localWork.toDto())
            } else {
                Resource.Error("Internet error: ${e.message()}")
            }
        } catch (e: Exception) {
            val localWork = worksDao.find(id)
            return if (localWork != null) {
                Resource.Success(localWork.toDto())
            } else {
                Resource.Error("Unknown error: ${e.message}")
            }
        }

        val localWork = worksDao.find(id)
        return if (localWork != null) {
            Resource.Success(localWork.toDto())
        } else if (workDto != null) {
            Resource.Success(workDto)
        } else {
            Resource.Error("No se encontró la obra ni local ni remotamente")
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
        workId: Int,
        title: String,
        dimension: String,
        techniqueId: Int,
        artistId: Int,
        price: Double,
        description: String,
        statusId: Int,
        imageFile: File?
    ): Resource<Unit> {
        return try {
            val response = remoteDataSource.updateWork(
                workId = workId,
                title = title,
                dimension = dimension,
                techniqueId = techniqueId,
                artistId = artistId,
                price = price,
                description = description,
                statusId = statusId,
                imageFile = imageFile
            )

            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error(errorBody ?: "Error ${response.code()}")
            }
        } catch (e: HttpException) {
            Resource.Error("HTTP error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }



    suspend fun deleteWork(id: Int) = remoteDataSource.deleteWork(id)

    fun getWorksByTechnique(techniqueId: Int): Flow<Resource<List<WorksDto>>> = flow {
        var listWorksByTechniqueDto: List<WorksEntity> = emptyList()

        try {
            emit(Resource.Loading())
        }catch (e: HttpException){
            emit(Resource.Error("Internet error: ${e.message()}"))
        }catch (e: UnknownHostException) {
            // No mostrar mensaje, solo usar datos locales
            emit(Resource.Error("No tienes conexion a internet"))
        } catch (e: ConnectException) {
            // No emitir error cuando no hay conexión
            emit(Resource.Error("No tienes conexion a internet"))
        }catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }

        listWorksByTechniqueDto = worksDao.getByTechnique(techniqueId)
        val finalList = listWorksByTechniqueDto.map {
            it.toDto()
        }

        emit(Resource.Success(finalList))
    }

    fun getWorksByArtist(artistId: Int): Flow<Resource<List<WorksDto>>> = flow {
        var listWorksByByArtist: List<WorksEntity> = emptyList()

        try {
            emit(Resource.Loading())
        }catch (e: HttpException){
            emit(Resource.Error("Internet error: ${e.message()}"))
        }catch (e: UnknownHostException) {
            // No mostrar mensaje, solo usar datos locales
            emit(Resource.Error("No tienes conexion a internet"))
        }catch (e: ConnectException) {
            // No emitir error cuando no hay conexión
            emit(Resource.Error("No tienes conexion a internet"))
        }catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
        listWorksByByArtist = worksDao.getByArtist(artistId)
        val finalList = listWorksByByArtist.map {
            it.toDto()
        }

        emit(Resource.Success(finalList))
    }

    private fun WorksDto.toEntity() = WorksEntity(
        workId = this.workId,
        title = this.title ?: "",
        dimension = this.dimension ?: "",
        techniqueId = this.techniqueId ?: 0,
        artistId = this.artistId ?: 0,
        statusId = this.statusId ?: 0,
        price = this.price ?: 0.0,
        description = this.description ?: "",
        imageUrl = this.imageUrl ?: ""
    )

    private fun WorksEntity.toDto() = WorksDto(
        workId = this.workId,
        title = this.title ?: "",
        dimension = this.dimension ?: "",
        techniqueId = this.techniqueId ?: 0,
        artistId = this.artistId ?: 0,
        statusId = this.statusId ?: 0,
        price = this.price ?: 0.0,
        description = this.description ?: "",
        imageUrl = this.imageUrl ?: ""
    )
}