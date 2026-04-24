package com.kadev.emostest.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteQuoteDao {

    @Query("SELECT * FROM fav_quote")
    fun getFavQuote(): Flow<List<FavoriteQuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFav(fav: FavoriteQuoteEntity): Long?

    @Delete
    suspend fun deleteFav(fav: FavoriteQuoteEntity): Int?

}