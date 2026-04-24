package com.kadev.emostest.data.repository

import com.kadev.emostest.data.local.FavoriteQuoteDao
import com.kadev.emostest.data.mapper.toDomain
import com.kadev.emostest.data.mapper.toEntity
import com.kadev.emostest.data.remote.ApiService
import com.kadev.emostest.data.remote.safeApiCall
import com.kadev.emostest.domain.model.QuoteOfTheDay

class QuoteRepository(
    val apiService: ApiService,
    val quoteDao: FavoriteQuoteDao
) {

    suspend fun getQuoteOfTheDay():Result<QuoteOfTheDay> =
        safeApiCall { apiService.getQuoteOfTheDay().toDomain() }

    suspend fun insertQuoteToFavorite(quoteOfTheDay: QuoteOfTheDay): Long?{
        return quoteDao.insertFav(quoteOfTheDay.toEntity())
    }

    suspend fun getAllFavorite(): List<QuoteOfTheDay>{
        return quoteDao.getFavQuote().map { it.toDomain() }
    }

    suspend fun deleteFav(quoteOfTheDay: QuoteOfTheDay): Int?{
        return quoteDao.deleteFav(quoteOfTheDay.toEntity())
    }
}