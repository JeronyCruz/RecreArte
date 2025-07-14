package edu.ucne.recrearte.data.remote

import edu.ucne.recrearte.data.remote.dto.ArtistsDto
import edu.ucne.recrearte.data.remote.dto.CustomersDto
import edu.ucne.recrearte.data.remote.dto.LoginRequestDto
import edu.ucne.recrearte.data.remote.dto.LoginResponseDto
import edu.ucne.recrearte.data.remote.dto.PaymentMethodsDto
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import edu.ucne.recrearte.data.remote.dto.UsersDto
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
    suspend fun getByIdPaymentMethod(@Path("id") id: Int): Response<PaymentMethodsDto>
    @POST("api/PaymentMethods")
    suspend fun createPaymentMethod(@Body paymentMethodsDto:  PaymentMethodsDto): PaymentMethodsDto
    @PUT("api/PaymentMethods/{id}")
    suspend fun updatePaymentMethod(
        @Path("id") id: Int,
        @Body paymentMethodsDto: PaymentMethodsDto
    ): Response<Unit>
    @DELETE("api/PaymentMethods/{id}")
    suspend fun deletePaymentMethod(@Path("id") id: Int): Response<Unit>


    // Techniques
    @GET("api/Techniques")
    suspend fun getAllTechniques(): List<TechniquesDto>
    @GET("api/Techniques/{id}")
    suspend fun getByIdTecnique(@Path("id") id: Int): Response<TechniquesDto>
    @POST("api/Techniques")
    suspend fun createTechnique(@Body techniqueDto:  TechniquesDto): TechniquesDto
    @PUT("api/Techniques/{id}")
    suspend fun updateTechnique(
        @Path("id") id: Int,
        @Body techniqueDto: TechniquesDto
    ):  Response<Unit>
    @DELETE("api/Techniques/{id}")
    suspend fun deleteTechnique(@Path("id") id: Int): Response<Unit>

    //Artists
    @GET("api/Artists")
    suspend fun getAllArtists(): List<ArtistsDto>
    @GET("api/Artists/{id}")
    suspend fun getByIdArtists(@Path("id") id: Int): Response<ArtistsDto>
    @POST("api/Artists")
    suspend fun createArtists(@Body artistDto: ArtistsDto): ArtistsDto
    @PUT("api/Artists/{id}")
    suspend fun updateArtists(
        @Path("id") id: Int,
        @Body artistDto: ArtistsDto
    ): Response<Unit>
    @DELETE("api/Artists/{id}")
    suspend fun deleteArtists(@Path("id") id: Int): Response<Unit>


    //Customers
    @GET("api/Customers")
    suspend fun getAllCustomers(): List<CustomersDto>
    @GET("api/Customers/{id}")
    suspend fun getByIdCustomers(@Path("id") id: Int): Response<CustomersDto>
    @POST("api/Customers")
    suspend fun createCustomers(@Body customersDto: CustomersDto): CustomersDto
    @PUT("api/Customers/{id}")
    suspend fun updateCustomers(
        @Path("id") id: Int,
        @Body customersDto: CustomersDto
    ): Response<Unit>
    @DELETE("api/Customers/{id}")
    suspend fun deleteCustomers(@Path("id") id: Int): Response<Unit>

    //Users
    @GET("api/Users")
    suspend fun getAllUsers(): List<UsersDto>
    @GET("api/Users/{id}")
    suspend fun getByIdUsers(@Path("id") id: Int): Response<UsersDto>
}