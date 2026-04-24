package com.kadev.emostest.data.mapper

import com.kadev.emostest.data.local.FavoriteQuoteEntity
import com.kadev.emostest.data.response.QOTDResponse
import com.kadev.emostest.domain.model.QuoteOfTheDay


fun QOTDResponse.toDomain(): QuoteOfTheDay{
    return QuoteOfTheDay(
        id = quote?.id?.toLong() ?: 0L,
        quote = quote?.body ?: "",
        author = quote?.author ?: ""
    )
}

fun FavoriteQuoteEntity.toDomain(): QuoteOfTheDay{
    return QuoteOfTheDay(
        id = id,
        quote = quote,
        author = author
    )
}

fun QuoteOfTheDay.toEntity(): FavoriteQuoteEntity{
    return FavoriteQuoteEntity(
        id = this.id,
        quote = quote,
        author = author,
        timestamp = System.currentTimeMillis(),
    )
}