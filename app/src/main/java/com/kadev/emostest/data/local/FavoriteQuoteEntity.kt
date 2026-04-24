package com.kadev.emostest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fav_quote")
data class FavoriteQuoteEntity(
    @PrimaryKey val id: Long,
    val quote: String,
    val author: String,
    val timestamp: Long,
)