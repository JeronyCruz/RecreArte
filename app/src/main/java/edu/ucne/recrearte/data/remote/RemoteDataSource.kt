package edu.ucne.recrearte.data.remote

import edu.ucne.recrearte.data.remote.dto.ArtistsDto
import edu.ucne.recrearte.data.remote.dto.CustomersDto
import edu.ucne.recrearte.data.remote.dto.ImagesDto
import edu.ucne.recrearte.data.remote.dto.LoginRequestDto
import edu.ucne.recrearte.data.remote.dto.LoginResponseDto
import edu.ucne.recrearte.data.remote.dto.PaymentMethodsDto
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import edu.ucne.recrearte.data.remote.dto.UsersDto
import edu.ucne.recrearte.data.remote.dto.WorksDto
import edu.ucne.recrearte.data.remote.dto.WorksListDto
import retrofit2.HttpException
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val recreArteingApi: RecreArteingApi
){
    // Login
    suspend fun loginUser(loginRequest: LoginRequestDto): LoginResponseDto {
        return recreArteingApi.loginUser(loginRequest)
    }

    //PaymentMethod
    suspend fun getPaymentMethod() = recreArteingApi.getAllPaymentMethod()
    suspend fun getPaymentMethodById(id: Int): PaymentMethodsDto {
        val response = recreArteingApi.getByIdPaymentMethod(id)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Payment Method not found")
        } else {
            throw HttpException(response)
        }
    }
    suspend fun updatePaymentMethod(id: Int, paymentMethodDto: PaymentMethodsDto) {
        val response = recreArteingApi.updatePaymentMethod(id, paymentMethodDto)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }
    suspend fun createPaymentMethod(paymentMethodsDto: PaymentMethodsDto) = recreArteingApi.createPaymentMethod(paymentMethodsDto)
    suspend fun deletePaymentMethod(id: Int) = recreArteingApi.deletePaymentMethod(id)

    //Techniques
    suspend fun getTechniques() = recreArteingApi.getAllTechniques()
    suspend fun getTechniqueById(id: Int): TechniquesDto {
        val response = recreArteingApi.getByIdTecnique(id)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Technique not found")
        } else {
            throw HttpException(response)
        }
    }
    suspend fun updateTechnique(id: Int, techniqueDto: TechniquesDto) {
        val response = recreArteingApi.updateTechnique(id, techniqueDto)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }
    suspend fun createTechnique(techniqueDto: TechniquesDto) = recreArteingApi.createTechnique(techniqueDto)
    suspend fun deleteTechnique(id: Int) = recreArteingApi.deleteTechnique(id)

    //Artists
    suspend fun getArtists() = recreArteingApi.getAllArtists()
    suspend fun getArtistById(id: Int): ArtistsDto {
        val response = recreArteingApi.getByIdArtists(id)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Artist not found")
        } else {
            throw HttpException(response)
        }
    }
    suspend fun updateArtist(id: Int, artistsDto: ArtistsDto) {
        val response = recreArteingApi.updateArtists(id, artistsDto)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }
    suspend fun createArtist(artistsDto: ArtistsDto): ArtistsDto {
        return recreArteingApi.createArtists(artistsDto) // Devuelve el DTO completo
    }
    suspend fun deleteArtist(id: Int) = recreArteingApi.deleteArtists(id)

    //Customers
    suspend fun getCustomers() = recreArteingApi.getAllCustomers()
    suspend fun getCustomerById(id: Int): CustomersDto {
        val response = recreArteingApi.getByIdCustomers(id)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Customer not found")
        } else {
            throw HttpException(response)
        }
    }
    suspend fun updateCustomer(id: Int, customersDto: CustomersDto) {
        val response = recreArteingApi.updateCustomers(id, customersDto)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }
    suspend fun createCustomer(customersDto: CustomersDto): CustomersDto {
        return recreArteingApi.createCustomers(customersDto) // Devuelve el DTO completo
    }
    suspend fun deleteCustomer(id: Int) = recreArteingApi.deleteCustomers(id)

    //Users
    suspend fun getUsers() = recreArteingApi.getAllUsers()
    suspend fun getUserById(id: Int): UsersDto {
        val response = recreArteingApi.getByIdUsers(id)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Users not found")
        } else {
            throw HttpException(response)
        }
    }

    //Works
    suspend fun getWorks() = recreArteingApi.getWorks()
    suspend fun getByIdWork(id: Int): WorksListDto{
        val response = recreArteingApi.getByIdWork(id)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Work not found")
        } else {
            throw HttpException(response)
        }
    }
    suspend fun updateWork(id: Int, workDto: WorksDto){
        val response = recreArteingApi.updateWork(id, workDto)
        if (!response.isSuccessful){
            throw HttpException(response)
        }
    }
    suspend fun createWork(workDto: WorksDto) = recreArteingApi.createWork(workDto)
    suspend fun deleteWork(id: Int) = recreArteingApi.deleteWork(id)

    //Images
    suspend fun getImages() = recreArteingApi.getImages()
    suspend fun getByIdImage(id: Int): ImagesDto{
        val response = recreArteingApi.getByIdImage(id)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Image not found")
        } else {
            throw HttpException(response)
        }
    }
    suspend fun updateImage(id: Int, imageDto: ImagesDto){
        val response = recreArteingApi.updateImage(id, imageDto)
        if (!response.isSuccessful){
            throw HttpException(response)
        }
    }
    suspend fun createImage(imageDto: ImagesDto) = recreArteingApi.createImage(imageDto)
    suspend fun deleteImage(id: Int) = recreArteingApi.deleteImage(id)

}