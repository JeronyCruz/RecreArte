package edu.ucne.recrearte.data.remote

import edu.ucne.recrearte.data.remote.dto.PaymentMethodsDto
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val recreArteingApi: RecreArteingApi
){
    //PaymentMethod
    suspend fun getPaymentMethod() = recreArteingApi.getAllPaymentMethod()
    suspend fun updatePaymentMethod(id: Int, paymentMethodsDto: PaymentMethodsDto) =
        recreArteingApi.updatePaymentMethod(id, paymentMethodsDto)
    suspend fun getById(id: Int) = recreArteingApi.getByIdPaymentMethod(id)
    suspend fun createPaymentMethod(paymentMethodsDto: PaymentMethodsDto) = recreArteingApi.createPaymentMethod(paymentMethodsDto)
    suspend fun deletePaymentMethod(id: Int) = recreArteingApi.deletePaymentMethod(id)

    //Techniques
    suspend fun getTechniques() = recreArteingApi.getAllTechniques()
    suspend fun updateTechnique(id: Int, techniqueDto: TechniquesDto) =
        recreArteingApi.updateTechnique(id, techniqueDto)
    suspend fun createTechnique(techniqueDto: TechniquesDto) = recreArteingApi.createTechnique(techniqueDto)
    suspend fun deleteTechnique(id: Int) = recreArteingApi.deleteTechnique(id)
}