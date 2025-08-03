package edu.ucne.recrearte.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import edu.ucne.recrearte.data.local.dao.ArtistDao
import edu.ucne.recrearte.data.local.dao.ArtistListDao
import edu.ucne.recrearte.data.local.dao.CustomerDao
import edu.ucne.recrearte.data.local.dao.LikeDao
import edu.ucne.recrearte.data.local.dao.PaymentMethodDao
import edu.ucne.recrearte.data.local.dao.RoleDao
import edu.ucne.recrearte.data.local.dao.TechniqueDao
import edu.ucne.recrearte.data.local.dao.UserDao
import edu.ucne.recrearte.data.local.dao.WishListDao
import edu.ucne.recrearte.data.local.dao.WishListDetailsDao
import edu.ucne.recrearte.data.local.dao.WorkDao
import edu.ucne.recrearte.data.local.entities.ArtistsEntity
import edu.ucne.recrearte.data.local.entities.ArtistsListEntity
import edu.ucne.recrearte.data.local.entities.CustomersEntity
import edu.ucne.recrearte.data.local.entities.LikesEntity
import edu.ucne.recrearte.data.local.entities.PaymentMethodsEntity
import edu.ucne.recrearte.data.local.entities.RolesEntity
import edu.ucne.recrearte.data.local.entities.TechniquesEntity
import edu.ucne.recrearte.data.local.entities.UsersEntity
import edu.ucne.recrearte.data.local.entities.WishListDetailsEntity
import edu.ucne.recrearte.data.local.entities.WishListsEntity
import edu.ucne.recrearte.data.local.entities.WorksEntity
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Database(
    entities = [
        ArtistsEntity::class,
        ArtistsListEntity::class,
        CustomersEntity::class,
        LikesEntity::class,
        PaymentMethodsEntity::class,
        RolesEntity::class,
        TechniquesEntity::class,
        UsersEntity::class,
        WorksEntity::class,
        WishListsEntity::class,
        WishListDetailsEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RecreArteDb : RoomDatabase() {
    abstract fun ArtistDao(): ArtistDao
    abstract fun PaymentMethodDao(): PaymentMethodDao
    abstract fun RoleDao(): RoleDao
    abstract fun TechniqueDao(): TechniqueDao
    abstract fun UserDao(): UserDao
    abstract fun WorkDao(): WorkDao
    abstract fun LikeDao(): LikeDao
    abstract fun CustomerDao(): CustomerDao
    abstract fun ArtistListDao(): ArtistListDao
    abstract fun WishListDao(): WishListDao
    abstract fun WishListDetailsDao(): WishListDetailsDao
}
