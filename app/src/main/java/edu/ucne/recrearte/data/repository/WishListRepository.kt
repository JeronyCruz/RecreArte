package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.local.dao.WishListDao
import edu.ucne.recrearte.data.local.dao.WishListDetailsDao
import edu.ucne.recrearte.data.local.dao.WorkDao
import edu.ucne.recrearte.data.local.entities.WishListDetailsEntity
import edu.ucne.recrearte.data.local.entities.WishListsEntity
import edu.ucne.recrearte.data.local.entities.WorksEntity
import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.WishListsDto
import edu.ucne.recrearte.data.remote.dto.WorksDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.net.ConnectException
import javax.inject.Inject
import kotlin.collections.emptyList
import kotlin.collections.map

class WishListRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val worksDao: WorkDao,
    private val wishListDao: WishListDao,
    private val wishListDetailsDao: WishListDetailsDao
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
            // Primero intentamos obtener datos de la API
            val remoteWishlist = remoteDataSource.getWorksInWishlistByCustomer(customerId)

            // Si tenemos datos remotos, actualizamos la base local
            remoteWishlist.forEach { workDto ->
                worksDao.saveOne(workDto.toEntity())
            }

            // Actualizamos la wishlist local
            val wishList = wishListDao.findByCustomerId(customerId) ?: run {
                val newWishList = WishListsEntity(
                    wishListId = 0, // Se generará automáticamente si es autoincremental
                    customerId = customerId,
                    userName = null
                )
                wishListDao.saveOne(newWishList)
                newWishList
            }

            // Actualizamos los detalles
            val details = remoteWishlist.map { work ->
                WishListDetailsEntity(
                    wishListId = wishList.wishListId,
                    workId = work.workId!!
                )
            }
            wishListDetailsDao.save(details)

            // Devolvemos los datos remotos (frescos)
            Resource.Success(remoteWishlist)
        } catch (e: Exception) {
            // Si falla la conexión, devolvemos datos locales
            when (e) {
                is HttpException, is ConnectException, is java.net.UnknownHostException -> {
                    val localWorks = wishListDao.getByCustomer(customerId)
                    if (localWorks.isNotEmpty()) {
                        Resource.Success(localWorks.map { it.toDto() })
                    } else {
                        Resource.Error("No hay conexión y no hay datos locales disponibles")
                    }
                }
                else -> Resource.Error("Error desconocido: ${e.message}")
            }
        }
    }


    suspend fun toggleWorkInWishlist(customerId: Int, workId: Int): Resource<Boolean> {
        return try {
            // Primero intentamos con la API
            val result = remoteDataSource.toggleWorkInWishlist(customerId, workId)

            // Actualizamos localmente según el resultado
            val wishList = wishListDao.findByCustomerId(customerId) ?: run {
                val newWishList = WishListsEntity(
                    wishListId = 0,
                    customerId = customerId,
                    userName = null
                )
                wishListDao.saveOne(newWishList)
                newWishList
            }

            if (result) {
                // Agregar a la wishlist local
                wishListDetailsDao.saveOne(
                    WishListDetailsEntity(
                        wishListId = wishList.wishListId,
                        workId = workId
                    )
                )
            } else {
                // Quitar de la wishlist local
                wishListDetailsDao.delete(wishList.wishListId, workId)
            }

            Resource.Success(result)
        } catch (e: Exception) {
            when (e) {
                is HttpException, is ConnectException, is java.net.UnknownHostException -> {
                    // Modo offline - operación local
                    val wishList = wishListDao.findByCustomerId(customerId) ?: run {
                        val newWishList = WishListsEntity(
                            wishListId = 0,
                            customerId = customerId,
                            userName = null
                        )
                        wishListDao.saveOne(newWishList)
                        newWishList
                    }

                    val isInWishlist = wishListDetailsDao.exists(wishList.wishListId, workId)

                    if (isInWishlist) {
                        wishListDetailsDao.delete(wishList.wishListId, workId)
                        Resource.Success(false)
                    } else {
                        wishListDetailsDao.saveOne(
                            WishListDetailsEntity(
                                wishListId = wishList.wishListId,
                                workId = workId
                            )
                        )
                        Resource.Success(true)
                    }
                }
                else -> Resource.Error("Error desconocido: ${e.message}")
            }
        }
    }

    suspend fun isWorkInWishlist(customerId: Int, workId: Int): Resource<Boolean> {
        return try {
            // Primero intentamos con la API
            val result = remoteDataSource.isWorkInWishlist(customerId, workId)
            Resource.Success(result)
        } catch (e: Exception) {
            when (e) {
                is HttpException, is ConnectException, is java.net.UnknownHostException -> {
                    // Modo offline - verificación local
                    val wishList = wishListDao.findByCustomerId(customerId) ?: return Resource.Success(false)
                    val exists = wishListDetailsDao.exists(wishList.wishListId, workId)
                    Resource.Success(exists)
                }
                else -> Resource.Error("Error desconocido: ${e.message}")
            }
        }
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