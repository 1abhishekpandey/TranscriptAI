package com.abhishek.summaryai.di

import com.abhishek.summaryai.data.repository.SubtitleRepositoryImpl
import com.abhishek.summaryai.domain.repository.SubtitleRepository
import com.abhishek.summaryai.util.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module for dependency injection
 * Provides application-wide dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides OkHttp client with logging interceptor
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        Logger.logD("AppModule: Creating OkHttpClient")

        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Logger.logV("OkHttp: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides Retrofit instance
     * Base URL will be updated when actual YouTube API integration is implemented
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        Logger.logD("AppModule: Creating Retrofit instance")

        // TODO: Update base URL when implementing YouTube InnerTube API
        // For now using a placeholder
        return Retrofit.Builder()
            .baseUrl("https://www.youtube.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides SubtitleRepository implementation
     */
    @Provides
    @Singleton
    fun provideSubtitleRepository(): SubtitleRepository {
        Logger.logD("AppModule: Providing SubtitleRepository")
        return SubtitleRepositoryImpl()
    }
}
