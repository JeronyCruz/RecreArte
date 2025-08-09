package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.dto.StatesDto
import javax.inject.Inject

class StateRepository@Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun getAllStates(): List<StatesDto> = remoteDataSource.getAllStates()

    suspend fun getStateById(id: Int): StatesDto = remoteDataSource.getStateById(id)
}