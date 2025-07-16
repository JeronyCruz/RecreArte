package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.ImagesDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

class ImageRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    fun getImages(): Flow<Resource<List<ImagesDto>>> = flow {
        try {
            emit(Resource.Loading())
            val image = remoteDataSource.getImages()
            emit(Resource.Success(image))
        }catch (e: HttpException){
            emit(Resource.Error("Internet error: ${e.message()}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }

    suspend fun getImageById(id: Int): Resource<ImagesDto> {
        return try {
            val technique = remoteDataSource.getByIdImage(id)
            Resource.Success(technique)
        } catch (e: HttpException) {
            Resource.Error("Internet error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }

    suspend fun createImage(imageDto: ImagesDto) = remoteDataSource.createImage(imageDto)

    suspend fun updateImage(id: Int, imageDto: ImagesDto) = remoteDataSource.updateImage(id, imageDto)

    suspend fun deleteImage(id: Int) = remoteDataSource.deleteImage(id)

}