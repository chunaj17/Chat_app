package com.example.coolchat.ui.chat


import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coolchat.databinding.ActivityChatBinding
import com.example.coolchat.model.Chat
import com.google.firebase.database.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

lateinit var databaseReference: DatabaseReference

@HiltViewModel
class ChatViewModel
@Inject
constructor() : ViewModel() {
    var mutableChatList: MutableLiveData<ArrayList<Chat>> = MutableLiveData()
    var chatList = ArrayList<Chat>()
    fun sendMessage(senderId: String, receiverId: String, message: String) {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference
        val hashMap: HashMap<String, String> = HashMap()
        hashMap["senderId"] = senderId
        hashMap["receiverId"] = receiverId
        hashMap["message"] = message
        viewModelScope.launch {
            reference.child("Chats").push().setValue(hashMap)
        }
    }

    fun readMessage(senderId: String, receiverId: String, binding: ActivityChatBinding) {
        viewModelScope.launch {
            val databaseReference: DatabaseReference =
                FirebaseDatabase.getInstance().getReference("Chats")
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatList.clear()
                    for (dataSnapshot: DataSnapshot in snapshot.children) {
                        val chat = dataSnapshot.getValue(Chat::class.java)
                        chat?.let {
                            if (it.senderId == senderId && it.receiverId == receiverId || it.senderId == receiverId && it.receiverId == senderId) {
                                chatList.add(it)
                            }
                        }
                    }
                    mutableChatList.postValue(chatList)
                    binding.loading.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i("firebase db cancelled", ":$error")
                }

            })
        }

    }

}