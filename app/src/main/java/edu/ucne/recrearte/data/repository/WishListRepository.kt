package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.WishListsDto
import edu.ucne.recrearte.data.remote.dto.WorksDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

class WishListRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    fun getWishLists(): Flow<Resource<List<WishListsDto>>> = flow {
        try {
            emit(Resource.Loading())
            val wishLists = remoteDataSource.getWishLists()
            emit(Resource.Success(wishLists))
        } catch (e: HttpException) {
            emit(Resource.Error("Internet error: ${e.message()}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }

    suspend fun getWorksInWishlistByCustomer(customerId: Int): Resource<List<WorksDto>> {
        return try {
            val works = remoteDataSource.getWorksInWishlistByCustomer(customerId)
            Resource.Success(works)
        } catch (e: HttpException) {
            Resource.Error("Internet error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }

    suspend fun toggleWorkInWishlist(customerId: Int, workId: Int): Resource<Boolean> {
        return try {
            val result = remoteDataSource.toggleWorkInWishlist(customerId, workId)
            Resource.Success(result)
        } catch (e: HttpException) {
            Resource.Error("Internet error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }

    suspend fun isWorkInWishlist(customerId: Int, workId: Int): Resource<Boolean> {
        return try {
            val result = remoteDataSource.isWorkInWishlist(customerId, workId)
            Resource.Success(result)
        } catch (e: HttpException) {
            Resource.Error("Internet error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }
}