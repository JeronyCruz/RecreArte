package edu.ucne.recrearte.data.remote

import com.google.gson.Gson
import edu.ucne.recrearte.data.remote.dto.ArtistsDto
import edu.ucne.recrearte.data.remote.dto.BillsDto
import edu.ucne.recrearte.data.remote.dto.ChangePasswordDto
import edu.ucne.recrearte.data.remote.dto.CustomersDto
import edu.ucne.recrearte.data.remote.dto.ImagesDto
import edu.ucne.recrearte.data.remote.dto.LikesDto
import edu.ucne.recrearte.data.remote.dto.LoginRequestDto
import edu.ucne.recrearte.data.remote.dto.LoginResponseDto
import edu.ucne.recrearte.data.remote.dto.PaymentMethodsDto
import edu.ucne.recrearte.data.remote.dto.RolesDto
import edu.ucne.recrearte.data.remote.dto.ShoppingCartsDto
import edu.ucne.recrearte.data.remote.dto.StatesDto
import edu.ucne.recrearte.data.remote.dto.TechniquesDto
import edu.ucne.recrearte.data.remote.dto.UsersDto
import edu.ucne.recrearte.data.remote.dto.WishListDetailsDto
import edu.ucne.recrearte.data.remote.dto.WishListsDto
import edu.ucne.recrearte.data.remote.dto.WorksDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val recreArteingApi: RecreArteingApi,
){
    // Login
    // En RemoteDataSource.kt
    suspend fun loginUser(loginRequest: LoginRequestDto): LoginResponseDto {
        return try {
            val response = recreArteingApi.loginUser(loginRequest)
            response // Retorna directamente el DTO si no hay error
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> throw InvalidCredentialsException("Correo o contraseña incorrectos")
                else -> throw e // Re-lanza otras excepciones HTTP
            }
        } catch (e: Exception) {
            throw e // Re-lanza otras excepciones
        }
    }
    suspend fun logoutUser(): Boolean {
        return try {
            recreArteingApi.logoutUser().isSuccessful
        } catch (e: Exception) {
            false
        }
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

    //Roles
    suspend fun getRole() = recreArteingApi.getAllRole()
    suspend fun getRoleById(id: Int): RolesDto {
        val response = recreArteingApi.getByIdRole(id)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Role not found")
        } else {
            throw HttpException(response)
        }
    }

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

    // Users
    suspend fun changePassword(
        userId: Int,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Boolean {
        return try {
            val response = recreArteingApi.changePassword(
                ChangePasswordDto(
                    userId = userId,
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )
            )
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    //Works
    suspend fun getWorks() = recreArteingApi.getWorks()
    suspend fun getByIdWork(id: Int): WorksDto{
        val response = recreArteingApi.getByIdWork(id)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Work not found")
        } else {
            throw HttpException(response)
        }
    }
    suspend fun createWork(
        title: String,
        dimension: String,
        techniqueId: Int,
        artistId: Int,
        price: Double,
        description: String,
        imageFile: File?
    ): Response<WorksDto> {
        return try {
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val dimensionPart = dimension.toRequestBody("text/plain".toMediaTypeOrNull())
            val techniqueIdPart = techniqueId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val artistIdPart = artistId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val pricePart = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val statusIdPart = "1".toRequestBody("text/plain".toMediaTypeOrNull()) // Default status
            val imageUrlPart = "temp_url".toRequestBody("text/plain".toMediaTypeOrNull()) // String vacío

            val imagePart = imageFile?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("imageFile", it.name, requestFile)
            }

            recreArteingApi.createWork(
                title = titlePart,
                dimension = dimensionPart,
                techniqueId = techniqueIdPart,
                artistId = artistIdPart,
                price = pricePart,
                description = descriptionPart,
                statusId = statusIdPart,
                imageUrl = imageUrlPart,
                image = imagePart
            )
        } catch (e: Exception) {
            Response.error(500, "Error creating work: ${e.message}".toResponseBody())
        }
    }

    suspend fun updateWork(
        workId: Int,
        title: String,
        dimension: String,
        techniqueId: Int,
        artistId: Int,
        price: Double,
        description: String,
        statusId: Int,
        imageFile: File?
    ): Response<Unit> {
        return try {
            // 1. Obtener la obra existente para conservar la URL de la imagen actual
            val existingWork = getByIdWork(workId)
            val currentImageUrl = existingWork.imageUrl ?: ""

            // 2. Crear todas las partes del formulario
            val workIdPart = workId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val dimensionPart = dimension.toRequestBody("text/plain".toMediaTypeOrNull())
            val techniqueIdPart = techniqueId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val artistIdPart = artistId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val pricePart = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val statusIdPart = statusId.toString().toRequestBody("text/plain".toMediaTypeOrNull()) // Asumiendo status 1 como default
            val imageUrlPart = currentImageUrl.toRequestBody("text/plain".toMediaTypeOrNull())

            // 3. Manejar la imagen (si se proporciona)
            val imagePart = imageFile?.let {
                MultipartBody.Part.createFormData(
                    "imageFile",
                    it.name,
                    it.asRequestBody("image/*".toMediaTypeOrNull())
                )
            }

            // 4. Hacer la llamada API
            recreArteingApi.updateWork(
                id = workId,
                workId = workIdPart,
                title = titlePart,
                dimension = dimensionPart,
                techniqueId = techniqueIdPart,
                artistId = artistIdPart,
                price = pricePart,
                description = descriptionPart,
                statusId = statusIdPart,
                imageUrl = imageUrlPart,
                imageFile = imagePart
            )
        } catch (e: Exception) {
            Response.error(500, "Error updating work: ${e.message}".toResponseBody())
        }
    }

    fun WorksDto.toJson(): String {
        return Gson().toJson(this)
    }

    suspend fun deleteWork(id: Int) = recreArteingApi.deleteWork(id)
    suspend fun getWorksByTechnique(techniqueId: Int): List<WorksDto> {
        return recreArteingApi.getWorksByTechnique(techniqueId)
    }
    suspend fun getWorksByArtist(artistId: Int): List<WorksDto> {
        return recreArteingApi.getWorksByArtist(artistId)
    }


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

    // Likes
    suspend fun getLikes(): List<LikesDto> {
        return recreArteingApi.getLikes()
    }
    suspend fun getLikeById(id: Int): LikesDto {
        val response = recreArteingApi.getLikeById(id)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Like not found")
        } else {
            throw HttpException(response)
        }
    }
    suspend fun createLike(like: LikesDto): LikesDto {
        val response = recreArteingApi.createLike(like)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Failed to create like")
        } else {
            throw HttpException(response)
        }
    }
    suspend fun updateLike(id: Int, like: LikesDto) {
        val response = recreArteingApi.updateLike(id, like)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }
    suspend fun deleteLike(id: Int) {
        val response = recreArteingApi.deleteLike(id)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }
    suspend fun getWorksLikedByCustomer(customerId: Int): List<WorksDto> {
        return recreArteingApi.getWorksLikedByCustomer(customerId)
    }
    suspend fun toggleLike(customerId: Int, workId: Int): Boolean {
        return recreArteingApi.toggleLike(customerId, workId)
    }
    suspend fun hasCustomerLikedWork(customerId: Int, workId: Int): Boolean {
        return recreArteingApi.hasCustomerLikedWork(customerId, workId)
    }
    suspend fun getLikeCountForWork(workId: Int): Int {
        return recreArteingApi.getLikeCountForWork(workId)
    }
    suspend fun getTop10MostLikedWorks(): List<WorksDto> {
        return recreArteingApi.getTop10MostLikedWorks()
    }

    // WishLists
    suspend fun getWishLists(): List<WishListsDto> {
        return recreArteingApi.getWishLists()
    }

    suspend fun getWishListById(id: Int): WishListsDto {
        val response = recreArteingApi.getWishListById(id)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("WishList not found")
        } else {
            throw HttpException(response)
        }
    }

    suspend fun createWishList(wishListDto: WishListsDto): WishListsDto {
        val response = recreArteingApi.createWishList(wishListDto)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Failed to create wishlist")
        } else {
            throw HttpException(response)
        }
    }

    suspend fun updateWishList(id: Int, wishListDto: WishListsDto) {
        val response = recreArteingApi.updateWishList(id, wishListDto)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }

    suspend fun deleteWishList(id: Int) {
        val response = recreArteingApi.deleteWishList(id)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }

    suspend fun getWorksInWishlistByCustomer(customerId: Int): List<WorksDto> {
        return recreArteingApi.getWorksInWishlistByCustomer(customerId)
    }

    suspend fun toggleWorkInWishlist(customerId: Int, workId: Int): Boolean {
        return recreArteingApi.toggleWorkInWishlist(customerId, workId)
    }

    suspend fun isWorkInWishlist(customerId: Int, workId: Int): Boolean {
        return recreArteingApi.isWorkInWishlist(customerId, workId)
    }

    // WishListDetails
    suspend fun getWishListDetails(): List<WishListDetailsDto> {
        return recreArteingApi.getWishListDetails()
    }
    
    //Shopping Carts
    suspend fun getCart(customerId: Int): ShoppingCartsDto{
        return recreArteingApi.getCart(customerId)
    }
    suspend fun addToCart(customerId: Int, workId: Int) = recreArteingApi.addToCart(customerId, workId)
    suspend fun removeFromCart(itemId: Int) = recreArteingApi.removeFromCart(itemId)
    suspend fun clearCart(customerId: Int) = recreArteingApi.clearCart(customerId)
    suspend fun checkout(customerId: Int): BillsDto = recreArteingApi.checkout(customerId)

    //Bills
    suspend fun getAllBills(): List<BillsDto> = recreArteingApi.getAllBills()
    suspend fun getBillById(id: Int): BillsDto = recreArteingApi.getBillById(id)
    suspend fun getBillsByCustomerId(customerId: Int): List<BillsDto> = recreArteingApi.getBillsByCustomerId(customerId)
    suspend fun createBill(billDto: BillsDto): BillsDto = recreArteingApi.createBill(billDto)
    suspend fun updateBill(id: Int, billDto: BillsDto) = recreArteingApi.updateBill(id, billDto)
    suspend fun deleteBill(id: Int) = recreArteingApi.deleteBill(id)

    //States
    suspend fun getAllStates(): List<StatesDto> = recreArteingApi.getAllStates()
    suspend fun getStateById(id: Int): StatesDto = recreArteingApi.getStateById(id)
}