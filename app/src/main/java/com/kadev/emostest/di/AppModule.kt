package com.kadev.emostest.di

import com.kadev.emostest.data.repository.QuoteRepository
import com.kadev.emostest.ui.screen.favorite.FavoriteQuoteViewModel
import com.kadev.emostest.ui.screen.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {


    single{ QuoteRepository(get(), get()) }

    viewModel { HomeViewModel(get()) }
    viewModel { FavoriteQuoteViewModel(get()) }


}
