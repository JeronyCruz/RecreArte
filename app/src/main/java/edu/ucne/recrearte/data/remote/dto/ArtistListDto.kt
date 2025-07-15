package edu.ucne.recrearte.data.remote.dto

import java.util.Date

data class ArtistListDto (
    val artistId : Int?,
    val artStyle : String,
    val socialMediaLinks : String,
    val firstName : String,
    val lastName : String,
    val email : String,
    val password : String,
    val userName : String,
    val phoneNumber : String,
    val documentNumber : String,
    val updateAt : Date,
    val roleId : Int,
    val description : String?
)