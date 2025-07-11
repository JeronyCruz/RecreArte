package edu.ucne.recrearte.data.remote

import edu.ucne.recrearte.data.remote.dto.PaymentMethodDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RecreArteingApi {

    //PaymentMethod
    @GET("api/PaymentMethods")
    suspend fun getAllPaymentMethod(): List<PaymentMethodDto>

    @GET("api/PaymentMethods/{id}")
    suspend fun getByIdPaymentMethod(@Path("id") id: Int): PaymentMethodDto

    @POST("api/PaymentMethods")
    suspend fun createPaymentMethod(@Body paymentMethodDto:  PaymentMethodDto): PaymentMethodDto

    @PUT("api/PaymentMethods/{id}")
    suspend fun updatePaymentMethod(
        @Path("id") id: Int,
        @Body paymentMethodDto: PaymentMethodDto
    ): PaymentMethodDto

    @DELETE("api/PaymentMethods/{id}")
    suspend fun deletePaymentMethod(@Path("id") id: Int): Response<Unit>

}