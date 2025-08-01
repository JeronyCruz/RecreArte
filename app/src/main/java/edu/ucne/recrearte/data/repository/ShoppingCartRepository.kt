package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.dto.BillsDto
import edu.ucne.recrearte.data.remote.dto.ShoppingCartsDto
import javax.inject.Inject

class ShoppingCartRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun getCart(customerId: Int): ShoppingCartsDto{
        return remoteDataSource.getCart(customerId)
    }

    suspend fun addToCart(customerId: Int, workId: Int) = remoteDataSource.addToCart(customerId, workId)
    suspend fun removeFromCart(itemId: Int) = remoteDataSource.removeFromCart(itemId)
    suspend fun clearCart(customerId: Int) = remoteDataSource.clearCart(customerId)
    suspend fun checkout(customerId: Int): BillsDto = remoteDataSource.checkout(customerId)
}