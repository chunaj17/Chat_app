package com.example.coolchat.room.user

import com.example.coolchat.model.User
import com.example.coolchat.util.EntityMapper
import javax.inject.Inject

class CacheMapper
@Inject
constructor() : EntityMapper<UserCacheEntity, User> {
    override fun mapFromEntity(entity: UserCacheEntity): User {
        return User(
            uid = entity.uid,
            photoUri = entity.photoUri,
            username = entity.username
        )
    }

    override fun mapToEntity(domainModel: User): UserCacheEntity {
        return UserCacheEntity(
            uid = domainModel.uid,
            photoUri = domainModel.photoUri,
            username = domainModel.username
        )
    }

    fun mapFromEntityList(entities: List<UserCacheEntity>): List<User> {
        return entities.map {
            mapFromEntity(it) }
    }
}