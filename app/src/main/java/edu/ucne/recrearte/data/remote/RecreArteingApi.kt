package edu.ucne.recrearte.data.remote

import edu.ucne.recrearte.data.remote.dto.ArtistListDto
import edu.ucne.recrearte.data.remote.dto.ArtistsDto
import edu.ucne.recrearte.data.remote.dto.ChangePasswordDto
import edu.ucne.recrearte.data.remote.dto.CustomersDto
import edu.ucne.recrearte.data.remote.dto.ImagesDto
import edu.ucne.recrearte.data.remote.dto.LikesDto
import edu.ucne.recrearte.data.remote.dto.LoginRequestDto
import edu.ucne.recrearte.data.remote.dto.LoginResponseDto
import edu.ucne.recrearte.data.remote.dto.PaymentMethodsDto
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import edu.ucne.recrearte.data.remote.dto.UsersDto
import edu.ucne.recrearte.data.remote.dto.WorksDto
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
    @POST("api/Login/logout")
    suspend fun logoutUser(): Response<Unit>

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
    suspend fun getAllArtists(): List<ArtistListDto>
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
    @PUT("api/Users/change-password")
    suspend fun changePassword(@Body changePasswordDto: ChangePasswordDto): Response<Unit>

    //Works
    @GET("api/Works")
    suspend fun getWorks(): List<WorksDto>
    @GET("api/Works/{id}")
    suspend fun getByIdWork(@Path("id") id: Int): Response<WorksDto>

    @GET("api/Works/by-technique/{techniqueId}")
    suspend fun getWorksByTechnique(@Path("techniqueId") techniqueId: Int): List<WorksDto>

    @GET("api/Works/by-artist/{artistId}")
    suspend fun getWorksByArtist(@Path("artistId") artistId: Int): List<WorksDto>
    @POST("api/Works")
    suspend fun createWork(@Body work: WorksDto):  Response<WorksDto>
    @PUT("api/Works/{id}")
    suspend fun updateWork(@Path("id") id: Int, @Body work: WorksDto): Response<Unit>
    @DELETE("api/Works/{id}")
    suspend fun deleteWork(@Path("id") id: Int): Response<Unit>

    //Images
    @GET("api/Images")
    suspend fun getImages(): List<ImagesDto>
    @GET("api/Images/{id}")
    suspend fun getByIdImage(@Path("id") id: Int): Response<ImagesDto>
    @POST("api/Images")
    suspend fun createImage(@Body image: ImagesDto): ImagesDto
    @PUT("api/Images/{id}")
    suspend fun updateImage(@Path("id") id: Int, @Body work: ImagesDto): Response<Unit>
    @DELETE("api/Images/{id}")
    suspend fun deleteImage(@Path("id") id: Int): Response<Unit>

    // Likes
    @GET("api/Likes")
    suspend fun getLikes(): List<LikesDto>
    @GET("api/Likes/{id}")
    suspend fun getLikeById(@Path("id") id: Int): Response<LikesDto>
    @POST("api/Likes")
    suspend fun createLike(@Body like: LikesDto): Response<LikesDto>
    @PUT("api/Likes/{id}")
    suspend fun updateLike(
        @Path("id") id: Int,
        @Body like: LikesDto
    ): Response<Unit>
    @DELETE("api/Likes/{id}")
    suspend fun deleteLike(@Path("id") id: Int): Response<Unit>
    @GET("api/Likes/customer/{customerId}/works")
    suspend fun getWorksLikedByCustomer(@Path("customerId") customerId: Int): List<WorksDto>
    @POST("api/Likes/customer/{customerId}/work/{workId}/toggle")
    suspend fun toggleLike(
        @Path("customerId") customerId: Int,
        @Path("workId") workId: Int
    ): Boolean
    @GET("api/Likes/customer/{customerId}/work/{workId}/status")
    suspend fun hasCustomerLikedWork(
        @Path("customerId") customerId: Int,
        @Path("workId") workId: Int
    ): Boolean
    @GET("api/Likes/work/{workId}/count")
    suspend fun getLikeCountForWork(@Path("workId") workId: Int): Int
    @GET("api/Likes/top-10")
    suspend fun getTop10MostLikedWorks(): List<WorksDto>

}