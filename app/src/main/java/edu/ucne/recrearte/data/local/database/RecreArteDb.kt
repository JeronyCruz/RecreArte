package edu.ucne.recrearte.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import edu.ucne.recrearte.data.local.dao.ArtistDao
import edu.ucne.recrearte.data.local.dao.PaymentMethodDao
import edu.ucne.recrearte.data.local.dao.RoleDao
import edu.ucne.recrearte.data.local.dao.TechniqueDao
import edu.ucne.recrearte.data.local.dao.UserDao
import edu.ucne.recrearte.data.local.dao.WorkDao
import edu.ucne.recrearte.data.local.entities.ArtistsEntity
import edu.ucne.recrearte.data.local.entities.PaymentMethodsEntity
import edu.ucne.recrearte.data.local.entities.RolesEntity
import edu.ucne.recrearte.data.local.entities.TechniquesEntity
import edu.ucne.recrearte.data.local.entities.UsersEntity
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
        PaymentMethodsEntity::class,
        RolesEntity::class,
        TechniquesEntity::class,
        UsersEntity::class,
        WorksEntity::class
    ],
    version = 1,
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
}
