package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.dto.BillsDto
import javax.inject.Inject

class BillRepository@Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun getAllBills(): List<BillsDto> = remoteDataSource.getAllBills()

    suspend fun getBillById(id: Int): BillsDto = remoteDataSource.getBillById(id)

    suspend fun getBillsByCustomerId(customerId: Int): List<BillsDto> =
        remoteDataSource.getBillsByCustomerId(customerId)

    suspend fun createBill(billDto: BillsDto): BillsDto = remoteDataSource.createBill(billDto)

    suspend fun updateBill(id: Int, billDto: BillsDto) = remoteDataSource.updateBill(id, billDto)

    suspend fun deleteBill(id: Int) = remoteDataSource.deleteBill(id)
}