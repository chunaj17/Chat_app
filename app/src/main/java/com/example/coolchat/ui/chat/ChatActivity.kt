package com.example.coolchat.ui.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.coolchat.adapter.ChatListAdapter
import com.example.coolchat.databinding.ActivityChatBinding
import com.example.coolchat.model.Chat
import com.example.coolchat.room.user.UserCacheEntity
import com.example.coolchat.ui.profile.ProfileActivity
import com.example.coolchat.ui.user.UserActivity
import com.example.coolchat.ui.user.UserViewModel
import com.example.coolchat.util.DataState
import com.example.coolchat.util.MainStateEvent
import com.example.coolchat.util.NotificationClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity : AppCompatActivity(), ChatListAdapter.Interaction {
    private var _binding: ActivityChatBinding? = null
    private lateinit var firebaseUser: FirebaseUser
    private val userViewModel: UserViewModel by viewModels()
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var notificationClass: NotificationClass
    private val binding get() = _binding!!
    private var chatList:ArrayList<Chat>? = null
    private lateinit var chatListAdapter: ChatListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityChatBinding.inflate(this.layoutInflater)
        createNotificationChannel()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        notificationClass = NotificationClass(this.applicationContext)
        val userId = intent.getStringExtra("userId")
        initRecyclerView()
        userObservers()
        userViewModel.setStateEvent(MainStateEvent.GetUserEvents)
        binding.imageBackBtn.setOnClickListener {
            val intent = Intent(Intent(this, UserActivity::class.java))
            startActivity(intent)
            finish()
        }
        binding.profileImage.setOnClickListener {
            val intent = Intent(Intent(this, ProfileActivity::class.java))
            startActivity(intent)
            finish()
        }
        binding.writeEditText.addTextChangedListener {
            it?.let {
                if (it.isEmpty()) {
                    binding.sendBtn.visibility = View.GONE
                    binding.chatList.scrollToPosition(chatList!!.size - 1)
                } else {
                    binding.sendBtn.visibility = View.VISIBLE
                    binding.chatList.scrollToPosition(chatList!!.size - 1)
                }
            }
        }
        viewModel.readMessage(userId!!,firebaseUser.uid,binding)
        viewModel.mutableChatList.observe(this) {
            chatList = it
            chatListAdapter.submitList(it)
            binding.chatList.scrollToPosition(it.size - 1)
        }
        binding.sendBtn.setOnClickListener {
            val msg = binding.writeEditText.text.toString()
            binding.writeEditText.setText("")
            viewModel.sendMessage(firebaseUser.uid, userId, msg)
            binding.writeEditText.setText("")
        }
        setContentView(binding.root)
    }
    private fun userData(data: List<UserCacheEntity>) {
        for (it in data) {
            if (it.uid == firebaseUser.uid) {
                Glide.with(binding.profileImage.context)
                    .load(it.photoUri.toUri())
                    .into(binding.profileImage)
            }
        }
    }
    private fun displayError(message: String?) {
        if (message != null) {
            println(message)
        } else {
            println("Unknown error")
        }
    }
    private fun userObservers() {
        userViewModel.dataState.observe(this) { dataState ->
            when (dataState) {
                is DataState.Success<List<UserCacheEntity>>-> {
                    displayProgressBar(false)
                        userData(dataState.data)
                }
                is DataState.Error -> {
                    displayProgressBar(false)
                   displayError(dataState.exception.message)
                }
                is DataState.Loading -> {
                    displayProgressBar(true)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.mutableChatList.observe(this){
            if (it.isNotEmpty()){
                intent.putExtra("userId", it.last().senderId)
                notificationClass.notificationBuilder.setContentTitle("message")
                notificationClass.notificationBuilder.setContentText(it.last().message)
                notificationClass.showNotification()
            }
        }
    }

    private fun displayProgressBar(isDisplayed: Boolean) {
        binding.loading.visibility = if (isDisplayed) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initRecyclerView() {
        binding.chatList.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            chatListAdapter = ChatListAdapter(this@ChatActivity)
            adapter = chatListAdapter
        }
    }

    override fun onItemSelected(position: Int, item: Chat) {
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Test Channel Name"
            val descriptionText = "Channel Description "
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(NotificationClass.Channel_ID, name, importance).apply {
                    description = descriptionText
                }

            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}