package edu.ucne.recrearte.di

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.ucne.recrearte.data.remote.NetworkMonitor
import edu.ucne.recrearte.data.remote.RecreArteingApi
import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.util.TokenManager
import edu.ucne.recrearte.data.repository.AuthInterceptor
import edu.ucne.recrearte.util.TokenManagement
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ApiModule {
    const val BASE_URL = "https://recreartev1.azurewebsites.net/"

    @Provides
    @Singleton
    fun providesMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(DateAdapter()).build()

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun providesRecreArteApi(
        moshi: Moshi,
        okHttpClient: OkHttpClient
    ): RecreArteingApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // AQUI AÑADES EL CLIENTE
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RecreArteingApi::class.java)
    }



    @Provides
    @Singleton
    fun provideRemoteDataSource(api: RecreArteingApi): RemoteDataSource {
        return RemoteDataSource(api)
    }

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManagement(context)
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitor(context).apply {
            startMonitoring()
        }
    }
}