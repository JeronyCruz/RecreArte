package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.BillsDto
import edu.ucne.recrearte.data.remote.dto.ShoppingCartsDto
import retrofit2.HttpException
import java.net.ConnectException
import javax.inject.Inject

class ShoppingCartRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun getCart(customerId: Int): Resource<ShoppingCartsDto> {

       return try {
            val shoppingCart = remoteDataSource.getCart(customerId)
            Resource.Success(shoppingCart)
        } catch (e: HttpException) {
            Resource.Error("Internet error: ${e.message()}")
        } catch (e: ConnectException) {
            Resource.Error("You don't have an internet connection")
        } catch (e: Exception) {
            Resource.Error("You don't have an internet connection")
        }
    }

    suspend fun addToCart(customerId: Int, workId: Int) = remoteDataSource.addToCart(customerId, workId)
    suspend fun removeFromCart(itemId: Int) = remoteDataSource.removeFromCart(itemId)
    suspend fun clearCart(customerId: Int) = remoteDataSource.clearCart(customerId)
    suspend fun checkout(customerId: Int): BillsDto = remoteDataSource.checkout(customerId)
}