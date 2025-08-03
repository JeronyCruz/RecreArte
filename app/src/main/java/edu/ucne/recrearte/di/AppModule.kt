package edu.ucne.recrearte.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.ucne.recrearte.data.local.database.RecreArteDb
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    @Provides
    @Singleton
    fun provideRecreArteDb(@ApplicationContext appContext: Context) =
        Room.databaseBuilder(
            appContext,
            RecreArteDb::class.java,
            "RecreArte.db"
        ).fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideArtistDao(recreArteDb: RecreArteDb) = recreArteDb.ArtistDao()
    @Provides
    fun providePaymentMethodDao(recreArteDb: RecreArteDb) = recreArteDb.PaymentMethodDao()
    @Provides
    fun provideRoleDao(recreArteDb: RecreArteDb) = recreArteDb.RoleDao()
    @Provides
    fun provideTechniqueDao(recreArteDb: RecreArteDb) = recreArteDb.TechniqueDao()
    @Provides
    fun provideUserDao(recreArteDb: RecreArteDb) = recreArteDb.UserDao()
    @Provides
    fun provideWorkDao(recreArteDb: RecreArteDb) = recreArteDb.WorkDao()
    @Provides
    fun provideLikeDao(recreArteDb: RecreArteDb) = recreArteDb.LikeDao()
    @Provides
    fun provideCustomerDao(recreArteDb: RecreArteDb) = recreArteDb.CustomerDao()
    @Provides
    fun provideArtistListDao(recreArteDb: RecreArteDb) = recreArteDb.ArtistListDao()
}