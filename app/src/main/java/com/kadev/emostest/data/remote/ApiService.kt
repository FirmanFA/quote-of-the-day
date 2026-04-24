package com.kadev.emostest.data.remote

import com.kadev.emostest.data.response.QOTDResponse
import retrofit2.http.GET

interface ApiService {

    @GET("qotd")
    suspend fun getQuoteOfTheDay(): QOTDResponse

}