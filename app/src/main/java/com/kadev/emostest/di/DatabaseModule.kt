package com.kadev.emostest.di

import androidx.room.Room
import com.kadev.emostest.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "quote_database"
        ).fallbackToDestructiveMigration(true).build()
    }

    single { get<AppDatabase>().favoriteQuoteDao() }

}
