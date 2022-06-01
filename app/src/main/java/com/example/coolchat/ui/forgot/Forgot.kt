package com.example.coolchat.ui.forgot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import com.example.coolchat.R
import com.example.coolchat.databinding.ActivityForgotBinding
import com.example.coolchat.ui.login.LoginActivity
import com.example.coolchat.ui.signup.SignUpActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Forgot : AppCompatActivity() {
    private var _binding: ActivityForgotBinding? = null
    val binding get() = _binding!!
    lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        _binding = ActivityForgotBinding.inflate(this.layoutInflater)
        binding.loading.visibility = View.GONE
        binding.emailText.addTextChangedListener {
            binding.emailText.error = null
        }
        binding.submit.setOnClickListener {
            val email = binding.emailText.text.toString().trim { it <= ' ' }
            if (email.isEmpty()) {
                binding.emailText.error = "Email is empty"
            } else {
                binding.loading.visibility = View.VISIBLE
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            binding.loading.visibility = View.GONE
                            val alertBuilder = AlertDialog.Builder(this)
                            alertBuilder.setTitle("Message")
                            alertBuilder.setMessage("password reset link have been sent to your email. please check your email to reset your password!!")
                            alertBuilder.setPositiveButton("ok,sure") { Dialog, which ->
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            alertBuilder.setNegativeButton("No") { Dialog, which ->
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            val createBuild = alertBuilder.create()
                            createBuild.show()
                        }else {
                            binding.loading.visibility = View.GONE
                            val alertBuilder = AlertDialog.Builder(this)
                            alertBuilder.setTitle("Warning")
                            alertBuilder.setIcon(R.drawable.ic_warning)
                            alertBuilder.setMessage("error: ${task.exception!!.message.toString()}")
                            alertBuilder.setPositiveButton("retry") { Dialog, which ->

                            }
                            val createBuild = alertBuilder.create()
                            createBuild.show()
                        }
                    }
            }
        }
        binding.backToLoginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}