package com.garitawatch.app.di

import android.content.Context
import androidx.room.Room
import com.garitawatch.app.data.local.GaritaWatchDatabase
import com.garitawatch.app.data.local.dao.AlertDao
import com.garitawatch.app.data.local.dao.MonitoredPortDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GaritaWatchDatabase {
        return Room.databaseBuilder(
            context,
            GaritaWatchDatabase::class.java,
            "garitawatch_db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideMonitoredPortDao(database: GaritaWatchDatabase): MonitoredPortDao {
        return database.monitoredPortDao()
    }

    @Provides
    fun provideAlertDao(database: GaritaWatchDatabase): AlertDao {
        return database.alertDao()
    }
}
