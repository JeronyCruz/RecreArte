package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.local.dao.RoleDao
import edu.ucne.recrearte.data.local.entities.RolesEntity
import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.RolesDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RoleRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val roleDao: RoleDao
){
    fun getRole(): Flow<Resource<List<RolesDto>>> = flow {
        try {
            println("[DEBUG] Intentando obtener roles...")
            emit(Resource.Loading())

            // Obtener datos remotos
            val remoteRoles = remoteDataSource.getRole()
            println("[DEBUG] API devolvió ${remoteRoles.size} roles")

            // Convertir y guardar en base de datos local
            val rolesEntities = remoteRoles.map { it.toEntity() }
            roleDao.save(rolesEntities)

            // Emitir datos remotos
            emit(Resource.Success(remoteRoles))

        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                401 -> "Tu sesión ha expirado. Por favor inicia sesión nuevamente"
                403 -> "No tienes permiso para acceder a este recurso"
                else -> "Error del servidor (${e.code()})"
            }
            println("[DEBUG] Error HTTP: $errorMsg")

            // Fallback a datos locales
            val localRoles = roleDao.getAll().map { it.toDto() }
            if (localRoles.isNotEmpty()) {
                emit(Resource.Success(localRoles))
            } else {
                emit(Resource.Error(errorMsg))
            }

        } catch (e: IOException) {
            val errorMsg = "Error de conexión: ${e.message}"
            println("[DEBUG] Network Error: $errorMsg")

            // Fallback a datos locales
            val localRoles = roleDao.getAll().map { it.toDto() }
            if (localRoles.isNotEmpty()) {
                emit(Resource.Success(localRoles))
            } else {
                emit(Resource.Error(errorMsg))
            }

        } catch (e: Exception) {
            val errorMsg = "Error inesperado: ${e.message}"
            println("[DEBUG] Unexpected Error: $errorMsg")

            // Fallback a datos locales
            val localRoles = roleDao.getAll().map { it.toDto() }
            if (localRoles.isNotEmpty()) {
                emit(Resource.Success(localRoles))
            } else {
                emit(Resource.Error(errorMsg))
            }
        }
    }

    // Conversión de DTO a Entity
    private fun RolesDto.toEntity() = RolesEntity(
        roleId = this.roleId,
        description = this.description ?: ""
    )

    // Conversión de Entity a DTO
    private fun RolesEntity.toDto() = RolesDto(
        roleId = this.roleId,
        description = this.description
    )

    fun getRoleById(id: Int): Flow<Resource<RolesDto>> = flow {
        try {
            println("[DEBUG] Intentando obtener rol con ID $id...")
            emit(Resource.Loading())

            // Obtener datos remotos
            val remoteRole = remoteDataSource.getRoleById(id)
            println("[DEBUG] API devolvió rol: ${remoteRole.description}")

            // Convertir y guardar en base de datos local
            val roleEntity = remoteRole.toEntity()
            roleDao.saveOne(roleEntity)

            // Emitir datos remotos
            emit(Resource.Success(remoteRole))

        } catch (e: HttpException) {
            val errorMsg = "Error HTTP: ${e.message()}"
            println("[DEBUG] $errorMsg")

            // Fallback a datos locales
            val localRole = roleDao.find(id)?.toDto()
            if (localRole != null) {
                emit(Resource.Success(localRole))
            } else {
                emit(Resource.Error(errorMsg))
            }

        } catch (e: IOException) {
            val errorMsg = "Error de conexión: ${e.message}"
            println("[DEBUG] $errorMsg")

            // Fallback a datos locales
            val localRole = roleDao.find(id)?.toDto()
            if (localRole != null) {
                emit(Resource.Success(localRole))
            } else {
                emit(Resource.Error(errorMsg))
            }

        } catch (e: Exception) {
            val errorMsg = "Error inesperado: ${e.message}"
            println("[DEBUG] $errorMsg")

            // Fallback a datos locales
            val localRole = roleDao.find(id)?.toDto()
            if (localRole != null) {
                emit(Resource.Success(localRole))
            } else {
                emit(Resource.Error(errorMsg))
            }
        }
    }

}