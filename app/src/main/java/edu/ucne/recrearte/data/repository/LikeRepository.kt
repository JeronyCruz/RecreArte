package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.LikesDto
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import edu.ucne.recrearte.data.remote.dto.WorksDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

class LikeRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
){
    fun getLikes(): Flow<Resource<List<LikesDto>>> = flow {
        try {
            emit(Resource.Loading())
            val like = remoteDataSource.getLikes()
            emit(Resource.Success(like))
        }catch (e: HttpException){
            emit(Resource.Error("Internet error: ${e.message()}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
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
        try {
            emit(Resource.Loading())
            val works = remoteDataSource.getWorksLikedByCustomer(customerId)
            emit(Resource.Success(works))
        } catch (e: HttpException) {
            emit(Resource.Error("Internet error: ${e.message()}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
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
            val works = remoteDataSource.getTop10MostLikedWorks()
            emit(Resource.Success(works))
        }catch (e: HttpException){
        } catch (e: HttpException) {
            emit(Resource.Error("Internet error: ${e.message()}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }
}