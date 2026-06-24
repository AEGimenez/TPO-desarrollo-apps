package com.example.sportshub.di

import com.example.sportshub.data.local.SportsDatabase
import com.example.sportshub.data.remote.RetrofitClient
import com.example.sportshub.data.repository.MatchRepository
import com.example.sportshub.data.repository.NewsRepository
import com.example.sportshub.data.repository.FavoritesRepository
import com.example.sportshub.ui.screens.home.HomeViewModel
import com.example.sportshub.ui.screens.home.DetailViewModel
import com.example.sportshub.ui.screens.login.LoginViewModel
import com.example.sportshub.ui.screens.onboarding.OnboardingViewModel
import com.example.sportshub.ui.screens.home.tabs.NewsViewModel
import com.example.sportshub.ui.screens.home.tabs.FavoritesViewModel
import com.example.sportshub.ui.screens.home.tabs.ChatViewModel
import com.example.sportshub.ui.screens.home.tabs.InicioViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single { SportsDatabase.getDatabase(get()) }
    single { get<SportsDatabase>().sportsDao() }
}

val networkModule = module {
    single { RetrofitClient.api }
    single { RetrofitClient.newsApi }
}

val repositoryModule = module {
    single { MatchRepository(get(), get()) }
    single { NewsRepository(get(), get()) }
    single { FavoritesRepository(get(), get()) }
}

val viewModelModule = module {
    viewModel { LoginViewModel() }
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { DetailViewModel(get(), get()) }
    viewModel { OnboardingViewModel(get(), get()) }
    viewModel { NewsViewModel(get()) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { ChatViewModel(get()) }
    viewModel { InicioViewModel(get(), get(), get(), get()) }
}
