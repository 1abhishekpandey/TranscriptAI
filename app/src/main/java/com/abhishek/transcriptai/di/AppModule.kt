package com.abhishek.transcriptai.di

import android.content.Context
import com.abhishek.transcriptai.data.local.database.AppDatabase
import com.abhishek.transcriptai.data.local.database.dao.PromptDao
import com.abhishek.transcriptai.data.local.preferences.SummariserPreferences
import com.abhishek.transcriptai.data.repository.PromptRepositoryImpl
import com.abhishek.transcriptai.data.repository.SubtitleCacheRepository
import com.abhishek.transcriptai.data.repository.SubtitleRepositoryImpl
import com.abhishek.transcriptai.data.repository.SummariserConfigRepositoryImpl
import com.abhishek.transcriptai.domain.repository.PromptRepository
import com.abhishek.transcriptai.domain.repository.SubtitleRepository
import com.abhishek.transcriptai.domain.repository.SummariserConfigRepository
import com.abhishek.transcriptai.util.Logger
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

    /**
     * Provides AppDatabase instance
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        Logger.logD("AppModule: Creating AppDatabase instance")
        return AppDatabase.getDatabase(context)
    }

    /**
     * Provides PromptDao from AppDatabase
     */
    @Provides
    @Singleton
    fun providePromptDao(database: AppDatabase): PromptDao {
        Logger.logD("AppModule: Providing PromptDao")
        return database.promptDao()
    }

    /**
     * Provides PromptRepository implementation
     */
    @Provides
    @Singleton
    fun providePromptRepository(promptDao: PromptDao): PromptRepository {
        Logger.logD("AppModule: Providing PromptRepository")
        return PromptRepositoryImpl(promptDao)
    }

    /**
     * Provides SummariserPreferences instance
     * Manages AI summariser configuration using SharedPreferences
     */
    @Provides
    @Singleton
    fun provideSummariserPreferences(
        @ApplicationContext context: Context
    ): SummariserPreferences {
        Logger.logD("AppModule: Providing SummariserPreferences")
        return SummariserPreferences(context)
    }

    /**
     * Provides SummariserConfigRepository implementation
     * Handles configuration for AI summariser feature
     */
    @Provides
    @Singleton
    fun provideSummariserConfigRepository(
        preferences: SummariserPreferences
    ): SummariserConfigRepository {
        Logger.logD("AppModule: Providing SummariserConfigRepository")
        return SummariserConfigRepositoryImpl(preferences)
    }

    /**
     * Provides SubtitleCacheRepository
     * In-memory cache for sharing subtitle data between screens
     */
    @Provides
    @Singleton
    fun provideSubtitleCacheRepository(): SubtitleCacheRepository {
        Logger.logD("AppModule: Providing SubtitleCacheRepository")
        return SubtitleCacheRepository()
    }
}
