package com.abhishek.summaryai.di

import android.content.Context
import com.abhishek.summaryai.data.repository.SubtitleRepositoryImpl
import com.abhishek.summaryai.domain.repository.SubtitleRepository
import com.abhishek.summaryai.util.Logger
import com.abhishek.youtubesubtitledownloader.YouTubeSubtitleDownloader
import com.abhishek.youtubesubtitledownloader.util.LogLevel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
     * Provides YouTubeSubtitleDownloader instance
     * This is the extension that handles YouTube subtitle downloading
     */
    @Provides
    @Singleton
    fun provideYouTubeSubtitleDownloader(@ApplicationContext context: Context): YouTubeSubtitleDownloader {
        Logger.logD("AppModule: Creating YouTubeSubtitleDownloader instance")

        // Set log level to INFO to reduce verbosity (can be changed to VERBOSE for debugging)
        YouTubeSubtitleDownloader.setLogLevel(LogLevel.INFO)

        return YouTubeSubtitleDownloader.getInstance(context)
    }

    /**
     * Provides SubtitleRepository implementation
     */
    @Provides
    @Singleton
    fun provideSubtitleRepository(
        youtubeSubtitleDownloader: YouTubeSubtitleDownloader
    ): SubtitleRepository {
        Logger.logD("AppModule: Providing SubtitleRepository with YouTubeSubtitleDownloader")
        return SubtitleRepositoryImpl(youtubeSubtitleDownloader)
    }
}
