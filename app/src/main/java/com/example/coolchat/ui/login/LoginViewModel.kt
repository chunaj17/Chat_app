package com.example.coolchat.ui.login

import android.app.Activity
import android.content.Intent
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.coolchat.model.User
import com.example.coolchat.databinding.LoginActivityBinding
import com.example.coolchat.ui.user.UserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class LoginViewModel : ViewModel() {
    fun authUser(
        email: String,
        password: String,
        binding: LoginActivityBinding,
        activity: Activity
    ) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.loading.visibility = ProgressBar.GONE
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
//                    if (firebaseUser.isEmailVerified) {
                        val intent = Intent(Intent(activity, UserActivity::class.java))
                        activity.startActivity(intent)
                        activity.finish()
//                    } else {
//                        firebaseUser.sendEmailVerification()
//                            .addOnSuccessListener {
//                                binding.email.error = "Email not verified check your email"
//                                binding.loginBtn.isEnabled = true
//                                binding.signupBtn.isEnabled = true
//                            }
//                            .addOnFailureListener {
//                                Toast.makeText(
//                                    activity,
//                                    "error: ${it.message.toString()}",
//                                    Toast.LENGTH_LONG
//                                ).show()
//                                binding.loginBtn.isEnabled = true
//                                binding.signupBtn.isEnabled = true
//                            }
//                    }
                } else {
                    Toast.makeText(
                        activity,
                        "error: ${task.exception!!.message.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.loginBtn.isEnabled = true
                    binding.signupBtn.isEnabled = true
                }
            }
    }

     fun saveUserToFirebaseDb(
        username: String?,
        photoUri: String?,
        binding: LoginActivityBinding,
        activity: Activity
    ) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val uid = firebaseAuth.uid ?: ""
         val user = User(uid, photoUri!!, username!!)
         val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")
         println("refLog:$ref")
        ref.setValue(user)
            .addOnSuccessListener {
                println("user saved to firebaseDb")
                binding.loading.visibility = ProgressBar.GONE
                Toast.makeText(
                    activity,
                    "successfully registered!!",
                    Toast.LENGTH_SHORT
                ).show()
                Intent(activity, UserActivity::class.java).also {
                    activity.startActivity(it)
                }
                activity.finish()
            }
    }
}
