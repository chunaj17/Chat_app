package com.example.coolchat.ui.main

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.coolchat.R
import com.example.coolchat.databinding.ActivityMainBinding
import com.example.coolchat.ui.login.LoginActivity
import com.example.coolchat.ui.signup.SignUpActivity
import com.example.coolchat.ui.user.UserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    lateinit var auth: FirebaseAuth
    var firebaseUser: FirebaseUser? = null
    lateinit var viewModel: MainActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firebaseUser = auth.currentUser
        _binding = ActivityMainBinding.inflate(this.layoutInflater)
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        if (viewModel.checkForInternet(this)) {
            if (firebaseUser != null && firebaseUser!!.isEmailVerified) {
                val intent = Intent(Intent(this, UserActivity::class.java))
                startActivity(intent)
                finish()
            }
            binding.loginBtn.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            binding.signupBtn.setOnClickListener {
                val intent = Intent(this, SignUpActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            val alertBuilder = AlertDialog.Builder(this)
            alertBuilder.setTitle("Exit")
            alertBuilder.setIcon(R.drawable.ic_warning)
            alertBuilder.setMessage("No internet connection. please check your internet connection!!")
            alertBuilder.setPositiveButton("ok,sure"){
                    Dialog,which ->
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            alertBuilder.setNegativeButton("No") {
                    Dialog,which ->
                finish()
            }
            val createBuild = alertBuilder.create()
            createBuild.show()
        }
        setContentView(binding.root)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}