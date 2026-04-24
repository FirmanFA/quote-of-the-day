package com.kadev.emostest.data.response


import com.squareup.moshi.Json

data class QOTDResponse(
    @param:Json(name = "qotd_date")
    val qotdDate: String? = "",
    @param:Json(name = "quote")
    val quote: Quote? = Quote()
) {
    data class Quote(
        @param:Json(name = "author")
        val author: String? = "",
        @param:Json(name = "author_permalink")
        val authorPermalink: String? = "",
        @param:Json(name = "body")
        val body: String? = "",
        @param:Json(name = "dialogue")
        val dialogue: Boolean? = false,
        @param:Json(name = "downvotes_count")
        val downvotesCount: Int? = 0,
        @param:Json(name = "favorites_count")
        val favoritesCount: Int? = 0,
        @param:Json(name = "id")
        val id: Int? = 0,
        @param:Json(name = "private")
        val `private`: Boolean? = false,
        @param:Json(name = "tags")
        val tags: List<String?>? = listOf(),
        @param:Json(name = "upvotes_count")
        val upvotesCount: Int? = 0,
        @param:Json(name = "url")
        val url: String? = ""
    )
}