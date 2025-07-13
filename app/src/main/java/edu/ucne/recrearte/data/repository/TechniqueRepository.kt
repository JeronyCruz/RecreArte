package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

class TechniqueRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    fun getTechniques(): Flow<Resource<List<TechniquesDto>>> = flow {
        try {
            emit(Resource.Loading())
            val technique = remoteDataSource.getTechniques()
            emit(Resource.Success(technique))
        }catch (e: HttpException){
            emit(Resource.Error("Internet error: ${e.message()}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }
    suspend fun getTechniqueById(id: Int): Resource<TechniquesDto> {
        return try {
            val technique = remoteDataSource.getTechniqueById(id)
            Resource.Success(technique)
        } catch (e: HttpException) {
            Resource.Error("Internet error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }

    suspend fun createTechnique(techniqueDto: TechniquesDto) = remoteDataSource.createTechnique(techniqueDto)

    suspend fun updateTechnique(id: Int, techniqueDto: TechniquesDto) = remoteDataSource.updateTechnique(id, techniqueDto)

    suspend fun deleteTechnique(id: Int) = remoteDataSource.deleteTechnique(id)
}