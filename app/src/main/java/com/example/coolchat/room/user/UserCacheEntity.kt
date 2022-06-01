package com.example.coolchat.room.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_data_table")
data class UserCacheEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val uid:String,
    @ColumnInfo(name = "image")
    val photoUri: String,
    @ColumnInfo(name = "username")
    val username:String
)
