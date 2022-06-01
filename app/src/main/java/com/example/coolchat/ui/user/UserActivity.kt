package com.example.coolchat.ui.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.coolchat.adapter.UserListAdapter
import com.example.coolchat.databinding.ActivityUserBinding
import com.example.coolchat.room.user.UserCacheEntity
import com.example.coolchat.ui.chat.ChatActivity
import com.example.coolchat.ui.chat.ChatViewModel
import com.example.coolchat.ui.profile.ProfileActivity
import com.example.coolchat.util.DataState
import com.example.coolchat.util.MainStateEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserActivity : AppCompatActivity(), UserListAdapter.Interaction {
    private var _binding: ActivityUserBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by viewModels()
    private val userList = ArrayList<UserCacheEntity>()
    private lateinit var auth: FirebaseAuth
    val firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    lateinit var userListAdapter: UserListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityUserBinding.inflate(this.layoutInflater)
        auth = Firebase.auth
        initRecyclerView()
        subscribeObservers()
        viewModel.setStateEvent(MainStateEvent.GetUserEvents)
        binding.profileImage.setOnClickListener {
            val intent = Intent(Intent(this, ProfileActivity::class.java))
            startActivity(intent)
            finish()
        }
        setContentView(binding.root)
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(this) { dataState ->
            when (dataState) {
                is DataState.Success<List<UserCacheEntity>> -> {
                    displayProgressBar(false)
                    userdata(dataState.data)
                    userListAdapter.submitList(userList)
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

    private fun userdata(data: List<UserCacheEntity>) {
        userList.clear()
        data.forEach {
            if (it.uid != firebase.uid) {
                userList.add(it)
            } else {
                Glide.with(binding.profileImage.context)
                    .load(it.photoUri.toUri())
                    .into(binding.profileImage)
            }
        }
    }

    private fun displayError(message: String?) {
        if (message != null) {
            println("error : $message")
        } else {
            println("unknownError")
        }
    }

    private fun displayProgressBar(isDisplayed: Boolean) {
        binding.loading.visibility = if (isDisplayed) View.VISIBLE else View.GONE
    }

    private fun initRecyclerView() {
        binding.userList.apply {
            layoutManager = LinearLayoutManager(this@UserActivity)
            userListAdapter = UserListAdapter(this@UserActivity)
            adapter = userListAdapter
        }
    }

    override fun onItemSelected(position: Int, item: UserCacheEntity) {
        val intent = Intent(Intent(this, ChatActivity::class.java))
        intent.putExtra("userId", item.uid)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onBackPressed() {
        finish()
    }
}