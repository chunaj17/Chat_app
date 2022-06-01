package com.example.coolchat.di

import com.example.coolchat.repository.UserRepository
import com.example.coolchat.room.user.CacheMapper
import com.example.coolchat.room.user.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object UserRepositoryModule {
    @Singleton
    @Provides
    fun providesUserRepository (
        userDao: UserDao,
        cacheMapper: CacheMapper
    ):UserRepository {
        return UserRepository(userDao,cacheMapper)
    }
}
//        chatDao: ChatDao,
//        chatCacheMapper: ChatCacheMapper,