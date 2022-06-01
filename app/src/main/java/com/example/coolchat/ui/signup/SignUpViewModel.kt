package com.example.coolchat.ui.signup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.coolchat.model.User
import com.example.coolchat.databinding.SignupActivityBinding
import com.example.coolchat.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.util.*

class SignUpViewModel : ViewModel() {
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    fun createUser(
        username: String?,
        binding: SignupActivityBinding,
        selectedPhotoUri: Uri?,
        activity: Activity,
        email: String,
        password: String
    ) {
        firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                      uploadImageToFirebase(username,selectedPhotoUri, binding, activity)
                } else {
                    binding.loading.visibility = ProgressBar.GONE
                    binding.emailText.error = task.exception!!.message.toString()
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.signupBtn.isEnabled = true
                        binding.loginBtn.isEnabled = true
                    }
                }
            }
    }

    private fun uploadImageToFirebase(
        username:String?,
        selectedPhotoUri: Uri?,
        binding: SignupActivityBinding,
        activity: Activity
    ) {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        if (selectedPhotoUri != null) {
            println("selectedPhotoUri :$selectedPhotoUri")
            ref.putFile(selectedPhotoUri)
                .addOnSuccessListener {
                    println("image saved")
                    ref.downloadUrl.addOnSuccessListener { image ->
                        saveUserToFirebaseDb(username,image.toString(), binding, activity)
                    }
                }
        } else {
            val defaultUserImg =
                FirebaseStorage.getInstance().reference.child("images/defaultUser.png")
            defaultUserImg.downloadUrl.addOnSuccessListener {
                saveUserToFirebaseDb(username,it.toString(), binding, activity)
            }
        }

    }
    private fun saveUserToFirebaseDb(
        username:String?,
        photoUri: String?,
        binding: SignupActivityBinding,
        activity: Activity
    ) {
        val uid = firebaseAuth.uid ?: ""
        println("username : $username")
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")
        val user = User(uid, photoUri!!, binding.username.editText!!.text.toString())
        ref.setValue(user)
            .addOnSuccessListener {
                println("user saved to firebaseDb")
                binding.loading.visibility = ProgressBar.GONE
                Toast.makeText(
                    activity,
                    "successfully registered!!",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(Intent(activity, LoginActivity::class.java))
                activity.startActivity(intent)
                activity.finish()
            }
    }
}