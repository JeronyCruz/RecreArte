package edu.ucne.recrearte.data.remote

import edu.ucne.recrearte.data.remote.dto.LoginRequestDto
import edu.ucne.recrearte.data.remote.dto.LoginResponseDto
import edu.ucne.recrearte.data.remote.dto.PaymentMethodDto
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
    suspend fun updatePaymentMethod(id: Int, paymentMethodDto: PaymentMethodDto) =
        recreArteingApi.updatePaymentMethod(id, paymentMethodDto)
    suspend fun getById(id: Int) = recreArteingApi.getByIdPaymentMethod(id)
    suspend fun createPaymentMethod(paymentMethodDto: PaymentMethodDto) = recreArteingApi.createPaymentMethod(paymentMethodDto)
    suspend fun deletePaymentMethod(id: Int) = recreArteingApi.deletePaymentMethod(id)
}