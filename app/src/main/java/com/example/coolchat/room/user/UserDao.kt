package com.example.coolchat.room.user

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userCacheEntity: UserCacheEntity): Long

    @Query("SELECT * FROM user_data_table")
    fun get(): Flow<List<UserCacheEntity>>
}
//fun get():LiveData<List<UserCacheEntity>>
