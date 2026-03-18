package com.garitawatch.app.di

import android.content.Context
import com.garitawatch.app.data.analytics.FirebaseAnalyticsProvider
import com.garitawatch.app.domain.analytics.AnalyticsProvider
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsProvider(
        firebaseAnalyticsProvider: FirebaseAnalyticsProvider
    ): AnalyticsProvider

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
            return FirebaseAnalytics.getInstance(context)
        }
    }
}
