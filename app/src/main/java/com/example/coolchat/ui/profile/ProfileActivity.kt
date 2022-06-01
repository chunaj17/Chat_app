package com.example.coolchat.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import com.bumptech.glide.Glide
import com.example.coolchat.model.User
import com.example.coolchat.databinding.ActivityProfileBinding
import com.example.coolchat.room.user.UserCacheEntity
import com.example.coolchat.ui.login.LoginActivity
import com.example.coolchat.ui.user.UserActivity
import com.example.coolchat.ui.user.UserViewModel
import com.example.coolchat.util.DataState
import com.example.coolchat.util.MainStateEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!
    lateinit var firebaseUser: FirebaseUser
    private var selectedPhotoUri: Uri? = null
    private val viewModel: UserViewModel by viewModels()
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = FirebaseStorage.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = storage.reference
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        _binding = ActivityProfileBinding.inflate(this.layoutInflater)
        subscribeObservers()
        viewModel.setStateEvent(MainStateEvent.GetUserEvents)
        setContentView(binding.root)
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(this) { dataState ->
            when (dataState) {
                is DataState.Success<List<UserCacheEntity>> -> {
                    displayProgressbar(false)
                        userData(dataState.data)
                }
                is DataState.Error -> {
                    displayProgressbar(false)
                    displayError(dataState.exception.message)
                }
                is DataState.Loading -> {
                    displayProgressbar(true)
                }
            }
        }
    }
    private fun displayError(message: String?) {
        if (message != null) {
            println("error: $message")
        } else {
            println("Unknown error")
        }
    }
    private fun userData(data: List<UserCacheEntity>) {
        for (it in data) {
            if (it.uid == firebaseUser.uid) {
                binding.username.setText(it.username)
                Glide.with(binding.profileImage.context)
                    .load(it.photoUri)
                    .into(binding.profileImage)
                Glide.with(binding.navProfileImage.context)
                    .load(it.photoUri)
                    .into(binding.navProfileImage)
            }
        }
    }

    private fun displayProgressbar(isDisplayed: Boolean) {
        binding.loading.visibility = if (isDisplayed) View.VISIBLE else View.GONE
    }

    override fun onStart() {
        super.onStart()
        binding.imageBackBtn.setOnClickListener {
            val intent = Intent(Intent(this, UserActivity::class.java))
            startActivity(intent)
            finish()
        }
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(Intent(this, LoginActivity::class.java))
            startActivity(intent)
            finish()
        }
        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    selectedPhotoUri = result.data?.data
                    binding.profileImage.setImageURI(selectedPhotoUri)
                }
            }
       binding.profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            resultLauncher.launch(intent)
            binding.saveButton.visibility = View.VISIBLE
        }
        binding.saveButton.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            upLoadImg()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun upLoadImg() {
        if (selectedPhotoUri != null) {
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        println("username ${binding.username.text}")
                        println("image saved")
                        saveUserToFirebaseDb(it.toString())
                    }
                }
                .addOnFailureListener {
                    binding.loading.visibility = View.GONE
                    Toast.makeText(applicationContext, "Failed" + it.message, Toast.LENGTH_LONG)
                        .show()

                }
        } else {
            Toast.makeText(applicationContext, "select image", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun saveUserToFirebaseDb(photoUri: String?) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")
        val userValue = User(uid, photoUri!!, binding.username.text.toString())
        val user = mapOf<String, String>(
            "uid" to userValue.uid,
            "photoUri" to userValue.photoUri,
            "username" to userValue.username
        )
        ref.updateChildren(user).addOnSuccessListener {
            binding.loading.visibility = View.GONE
            Toast.makeText(this, "Successfully Updated", Toast.LENGTH_SHORT).show()
            binding.saveButton.visibility = View.GONE
        }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to Update", Toast.LENGTH_SHORT).show()
            }
    }
}