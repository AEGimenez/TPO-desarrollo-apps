package com.example.sportshub

import android.app.Application
import com.example.sportshub.di.databaseModule
import com.example.sportshub.di.networkModule
import com.example.sportshub.di.repositoryModule
import com.example.sportshub.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SportsHubApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@SportsHubApplication)
            modules(
                listOf(
                    databaseModule,
                    networkModule,
                    repositoryModule,
                    viewModelModule
                )
            )
        }
    }
}
