package com.kadev.emostest

import android.app.Application
import com.kadev.emostest.di.appModule
import com.kadev.emostest.di.databaseModule
import com.kadev.emostest.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule, databaseModule, networkModule)
        }
    }

}