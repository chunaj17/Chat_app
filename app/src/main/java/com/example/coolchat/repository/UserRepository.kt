package com.example.coolchat.repository

import com.example.coolchat.model.User
import com.example.coolchat.room.user.CacheMapper
import com.example.coolchat.room.user.UserDao
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class UserRepository constructor(
    private val userDao: UserDao,
    private val cacheMapper: CacheMapper,
) {
    suspend fun getUser(): Flow<Boolean> = callbackFlow {
        lateinit var postReference: DatabaseReference
        try {
            postReference = FirebaseDatabase.getInstance().getReference("Users")
            val postListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapShot: DataSnapshot in snapshot.children) {
                        val user = dataSnapShot.getValue(User::class.java)
                        user?.let {
                            CoroutineScope(Dispatchers.IO).launch {
                                userDao.insert(cacheMapper.mapToEntity(it))
                            }
                        }
                    }
                    this@callbackFlow.trySend(true)
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Post cancelled: $error")
                }
            }
            postReference.addValueEventListener(postListener)
        } catch (e: Exception) {
            this@callbackFlow.trySend(false)
        }

        awaitClose {  }
    }
}