package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.local.dao.TechniqueDao
import edu.ucne.recrearte.data.local.entities.TechniquesEntity
import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class TechniqueRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val techniqueDao: TechniqueDao
) {
    fun getTechniques(): Flow<Resource<List<TechniquesDto>>> = flow {
        var listTechnique: List<TechniquesEntity> = emptyList()

        try {
            emit(Resource.Loading())
            val technique = remoteDataSource.getTechniques()
            val techniqueEntity = technique.map {
                it.toEntity()
            }
            techniqueDao.save(techniqueEntity)

        }catch (e: HttpException){
            emit(Resource.Error("Internet error: ${e.message()}"))
        }catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
        listTechnique = techniqueDao.getAll()
        val finalList = listTechnique.map {
            it.toDto()
        }

        emit(Resource.Success(finalList))
    }
    suspend fun getTechniqueById(id: Int): Resource<TechniquesDto> {
        return try {
            println("[DEBUG] Intentando obtener técnica con ID $id desde la API...")
            val remoteTechnique = remoteDataSource.getTechniqueById(id)
            println("[DEBUG] API devolvió: ${remoteTechnique.techniqueName}")


            val techniqueEntity = remoteTechnique.toEntity()
            techniqueDao.saveOne(techniqueEntity)

            println("[DEBUG] Técnica guardada en base de datos local")
            Resource.Success(remoteTechnique)

        } catch (e: HttpException) {
            val errorMsg = "Error HTTP: ${e.message()}"
            println("[DEBUG] $errorMsg")


            val localTechnique = techniqueDao.find(id)?.toDto()
            if (localTechnique != null) {
                println("[DEBUG] Usando datos locales: ${localTechnique.techniqueName}")
                Resource.Success(localTechnique)
            } else {
                Resource.Error(errorMsg)
            }

        } catch (e: IOException) {
            val errorMsg = "Connection error: ${e.message}"
            println("[DEBUG] $errorMsg")

            val localTechnique = techniqueDao.find(id)?.toDto()
            if (localTechnique != null) {
                println("[DEBUG] Usando datos locales: ${localTechnique.techniqueName}")
                Resource.Success(localTechnique)
            } else {
                Resource.Error(errorMsg)
            }

        } catch (e: Exception) {
            val errorMsg = "Unexpected error: ${e.message}"
            println("[DEBUG] $errorMsg")

            val localTechnique = techniqueDao.find(id)?.toDto()
            if (localTechnique != null) {
                println("[DEBUG] Usando datos locales: ${localTechnique.techniqueName}")
                Resource.Success(localTechnique)
            } else {
                Resource.Error(errorMsg)
            }
        }
    }

    suspend fun createTechnique(techniqueDto: TechniquesDto) = remoteDataSource.createTechnique(techniqueDto)

    suspend fun updateTechnique(id: Int, techniqueDto: TechniquesDto) = remoteDataSource.updateTechnique(id, techniqueDto)

    suspend fun deleteTechnique(id: Int): Boolean {
        return try {

            remoteDataSource.deleteTechnique(id)

            techniqueDao.deleteById(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun TechniquesDto.toEntity() = TechniquesEntity(
        techniqueId = this.techniqueId,
        techniqueName = this.techniqueName ?: ""
    )
    private fun TechniquesEntity.toDto() = TechniquesDto(
        techniqueId = this.techniqueId,
        techniqueName = this.techniqueName ?: ""
    )
}