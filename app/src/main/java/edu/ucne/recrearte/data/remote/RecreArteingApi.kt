package edu.ucne.recrearte.data.remote

import edu.ucne.recrearte.data.remote.dto.LoginRequestDto
import edu.ucne.recrearte.data.remote.dto.LoginResponseDto
import edu.ucne.recrearte.data.remote.dto.PaymentMethodDto
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RecreArteingApi {

    // Login
    @POST("api/Login")
    suspend fun loginUser(@Body LoginRequest: LoginRequestDto): LoginResponseDto

    //PaymentMethod
    @GET("api/PaymentMethods")
    suspend fun getAllPaymentMethod(): List<PaymentMethodsDto>

    @GET("api/PaymentMethods/{id}")
    suspend fun getByIdPaymentMethod(@Path("id") id: Int): PaymentMethodsDto
    @POST("api/PaymentMethods")
    suspend fun createPaymentMethod(@Body paymentMethodsDto:  PaymentMethodsDto): PaymentMethodsDto
    @PUT("api/PaymentMethods/{id}")
    suspend fun updatePaymentMethod(
        @Path("id") id: Int,
        @Body paymentMethodsDto: PaymentMethodsDto
    ): PaymentMethodsDto
    @DELETE("api/PaymentMethods/{id}")
    suspend fun deletePaymentMethod(@Path("id") id: Int): Response<Unit>


    // Techniques
    @GET("api/Techniques")
    suspend fun getAllTechniques(): List<TechniquesDto>
    @POST("api/Techniques")
    suspend fun createTechnique(@Body techniqueDto:  TechniquesDto): TechniquesDto
    @PUT("api/Techniques/{id}")
    suspend fun updateTechnique(
        @Path("id") id: Int,
        @Body techniqueDto: TechniquesDto
    ): TechniquesDto
    @DELETE("api/Techniques/{id}")
    suspend fun deleteTechnique(@Path("id") id: Int): Response<Unit>
}