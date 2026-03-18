package com.garitawatch.app.di

import com.garitawatch.app.data.remote.CbpApiService
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideTikXml(): TikXml {
        return TikXml.Builder()
            .exceptionOnUnreadXml(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, tikXml: TikXml): Retrofit {
        return Retrofit.Builder()
            .baseUrl(CbpApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(TikXmlConverterFactory.create(tikXml))
            .build()
    }

    @Provides
    @Singleton
    fun provideCbpApiService(retrofit: Retrofit): CbpApiService {
        return retrofit.create(CbpApiService::class.java)
    }
}
