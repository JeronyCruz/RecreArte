package edu.ucne.recrearte.data.remote

import edu.ucne.recrearte.data.remote.dto.LoginRequestDto
import edu.ucne.recrearte.data.remote.dto.LoginResponseDto
import edu.ucne.recrearte.data.remote.dto.PaymentMethodsDto
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
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
    suspend fun getById(id: Int) = recreArteingApi.getByIdPaymentMethod(id)
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
}