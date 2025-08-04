package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.local.dao.LikeDao
import edu.ucne.recrearte.data.local.dao.WorkDao
import edu.ucne.recrearte.data.local.entities.LikesEntity
import edu.ucne.recrearte.data.local.entities.WorksEntity
import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.LikesDto
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import edu.ucne.recrearte.data.remote.dto.WorksDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class LikeRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val workDao: WorkDao,
    private val likeDao: LikeDao
){
    fun getLikes(): Flow<Resource<List<LikesDto>>> = flow {
        var listLikesDto: List<LikesEntity> = emptyList()

        try {
            emit(Resource.Loading())
            val like = remoteDataSource.getLikes()
            val likesEntity = like.map {
                it.toEntity()
            }
            likeDao.save(likesEntity)
        }catch (e: HttpException){
            emit(Resource.Error("Internet error: ${e.message()}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
        listLikesDto = likeDao.getAll()
        val finalList = listLikesDto.map {
            it.toDto()
        }

        emit(Resource.Success(finalList))
    }

    suspend fun getLikeById(id: Int): Resource<LikesDto> {
        return try {
            val like = remoteDataSource.getLikeById(id)
            Resource.Success(like)
        } catch (e: HttpException) {
            Resource.Error("Internet error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }
    suspend fun createLike(likeDto: LikesDto) = remoteDataSource.createLike(likeDto)


    fun getWorksLikedByCustomer(customerId: Int): Flow<Resource<List<WorksDto>>> = flow {
        var listWorksLikedByCustomer: List<WorksEntity> = emptyList()
        try {
            emit(Resource.Loading())
            val works = remoteDataSource.getWorksLikedByCustomer(customerId)

        } catch (e: HttpException) {
            emit(Resource.Error("Internet error: ${e.message()}"))
        } catch (e: UnknownHostException) {
            // No mostrar mensaje, solo usar datos locales
        }catch (e: ConnectException) {
            // No emitir error cuando no hay conexión
        }catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }

        listWorksLikedByCustomer = likeDao.getWorksLikedByCustomer(customerId)
        val finalList = listWorksLikedByCustomer.map {
            it.toDto()
        }
        emit(Resource.Success(finalList))
    }

    suspend fun toggleLike(customerId: Int, workId: Int): Resource<Boolean> {
        return try {
            val result = remoteDataSource.toggleLike(customerId, workId)
            Resource.Success(result)
        } catch (e: HttpException) {
            Resource.Error("Internet error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }

    suspend fun hasCustomerLikedWork(customerId: Int, workId: Int): Resource<Boolean> {
        return try {
            val result = remoteDataSource.hasCustomerLikedWork(customerId, workId)
            Resource.Success(result)
        } catch (e: HttpException) {
            Resource.Error("Internet error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }

    suspend fun getLikeCountForWork(workId: Int): Resource<Int> {
        return try {
            val count = remoteDataSource.getLikeCountForWork(workId)
            Resource.Success(count)
        } catch (e: HttpException) {
            Resource.Error("Internet error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }

    fun getTop10MostLikedWorks(): Flow<Resource<List<WorksDto>>> = flow {
        try {
            emit(Resource.Loading())
            val remoteLikes = remoteDataSource.getLikes()
            likeDao.save(remoteLikes.map { it.toEntity() })

            val remoteWorks = remoteDataSource.getTop10MostLikedWorks()
            workDao.save(remoteWorks.map { it.toEntity() })

            emit(Resource.Success(remoteWorks.take(5)))
        } catch (e: Exception) {
            // En caso de error, usa los datos locales
            val localTop5 = workDao.getTop5().map { it.toDto() }
            if (localTop5.isNotEmpty()) {
                emit(Resource.Success(localTop5))
            } else {
                emit(Resource.Error("Could not fetch data and no cache available"))
            }
        }
    }

    private fun LikesDto.toEntity() = LikesEntity(
        likeId = this.likeId,
        dateLiked = this.dateLiked,
        customerId = this.customerId ?: 0,
        workId = this.workId ?: 0
    )
    private fun LikesEntity.toDto() = LikesDto(
        likeId = this.likeId,
        dateLiked = this.dateLiked,
        customerId = this.customerId ?: 0,
        workId = this.workId ?: 0
    )

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