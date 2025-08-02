package edu.ucne.recrearte.data.remote

import edu.ucne.recrearte.data.remote.dto.ArtistListDto
import edu.ucne.recrearte.data.remote.dto.ArtistsDto
import edu.ucne.recrearte.data.remote.dto.BillsDto
import edu.ucne.recrearte.data.remote.dto.ChangePasswordDto
import edu.ucne.recrearte.data.remote.dto.CustomersDto
import edu.ucne.recrearte.data.remote.dto.ImagesDto
import edu.ucne.recrearte.data.remote.dto.LikesDto
import edu.ucne.recrearte.data.remote.dto.LoginRequestDto
import edu.ucne.recrearte.data.remote.dto.LoginResponseDto
import edu.ucne.recrearte.data.remote.dto.PaymentMethodsDto
import edu.ucne.recrearte.data.remote.dto.ShoppingCartsDto
import edu.ucne.recrearte.data.remote.dto.StatesDto
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import edu.ucne.recrearte.data.remote.dto.UsersDto
import edu.ucne.recrearte.data.remote.dto.WishListDetailsDto
import edu.ucne.recrearte.data.remote.dto.WishListsDto
import edu.ucne.recrearte.data.remote.dto.WorksDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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

    @Multipart
    @POST("api/Works")
    suspend fun createWork(
        @Part("title") title: RequestBody,
        @Part("dimension") dimension: RequestBody,
        @Part("techniqueId") techniqueId: RequestBody,
        @Part("artistId") artistId: RequestBody,
        @Part("price") price: RequestBody,
        @Part("description") description: RequestBody,
        @Part("statusId") statusId: RequestBody,
        @Part("imageUrl") imageUrl: RequestBody, // <- Añadir este campo
        @Part image: MultipartBody.Part?
    ): Response<WorksDto>

    @Multipart
    @PUT("api/Works/{id}")
    suspend fun updateWork(
        @Path("id") id: Int,
        @Part("WorkId") workId: RequestBody,
        @Part("Title") title: RequestBody,
        @Part("Dimension") dimension: RequestBody,
        @Part("TechniqueId") techniqueId: RequestBody,
        @Part("ArtistId") artistId: RequestBody,
        @Part("Price") price: RequestBody,
        @Part("Description") description: RequestBody,
        @Part("StatusId") statusId: RequestBody,
        @Part("ImageUrl") imageUrl: RequestBody,
        @Part imageFile: MultipartBody.Part?
    ): Response<Unit>

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

    // WishList
    @GET("api/WishLists")
    suspend fun getWishLists(): List<WishListsDto>

    @GET("api/WishLists/{id}")
    suspend fun getWishListById(@Path("id") id: Int): Response<WishListsDto>

    @POST("api/WishLists")
    suspend fun createWishList(@Body wishList: WishListsDto): Response<WishListsDto>

    @PUT("api/WishLists/{id}")
    suspend fun updateWishList(
        @Path("id") id: Int,
        @Body wishList: WishListsDto
    ): Response<Unit>

    @DELETE("api/WishLists/{id}")
    suspend fun deleteWishList(@Path("id") id: Int): Response<Unit>

    @GET("api/WishLists/customer/{customerId}/works")
    suspend fun getWorksInWishlistByCustomer(@Path("customerId") customerId: Int): List<WorksDto>

    @POST("api/WishLists/customer/{customerId}/work/{workId}/toggle")
    suspend fun toggleWorkInWishlist(
        @Path("customerId") customerId: Int,
        @Path("workId") workId: Int
    ): Boolean

    @GET("api/WishLists/customer/{customerId}/work/{workId}/status")
    suspend fun isWorkInWishlist(
        @Path("customerId") customerId: Int,
        @Path("workId") workId: Int
    ): Boolean

    // WishListDetails endpoints
    @GET("api/WishListsDetails")
    suspend fun getWishListDetails(): List<WishListDetailsDto>

    //Shopping Carts
    @GET("api/ShoppingCarts/{customerId}")
    suspend fun getCart(@Path("customerId") customerId: Int): ShoppingCartsDto
    @POST("api/ShoppingCarts/{customerId}/add/{workId}")
    suspend fun addToCart(
        @Path("customerId") customerId: Int,
        @Path("workId") workId: Int
    )
    @DELETE("api/ShoppingCarts/remove/{itemId}")
    suspend fun removeFromCart(@Path("itemId") itemId: Int)
    @DELETE("api/ShoppingCarts/{customerId}/clear")
    suspend fun clearCart(@Path("customerId") customerId: Int)
    @POST("api/ShoppingCarts/{customerId}/checkout")
    suspend fun checkout(@Path("customerId") customerId: Int): BillsDto

    //Bills
    @GET("api/Bills")
    suspend fun getAllBills(): List<BillsDto>
    @GET("api/Bills/{id}")
    suspend fun getBillById(@Path("id") id: Int): BillsDto
    @GET("api/Bills/by-customer/{customerId}")
    suspend fun getBillsByCustomerId(@Path("customerId") customerId: Int): List<BillsDto>
    @POST("api/Bills")
    suspend fun createBill(@Body billDto: BillsDto): BillsDto
    @PUT("api/Bills/{id}")
    suspend fun updateBill(@Path("id") id: Int, @Body billDto: BillsDto)
    @DELETE("api/Bills/{id}")
    suspend fun deleteBill(@Path("id") id: Int)

    //States
    @GET("api/States")
    suspend fun getAllStates(): List<StatesDto>
    @GET("api/States/{id}")
    suspend fun getStateById(@Path("id") id: Int): StatesDto
}