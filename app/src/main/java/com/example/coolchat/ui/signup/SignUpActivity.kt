package com.example.coolchat.ui.signup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.coolchat.R
import com.example.coolchat.databinding.SignupActivityBinding
import com.example.coolchat.ui.login.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var _binding: SignupActivityBinding? = null
    private val binding get() = _binding!!
    private var selectedPhotoUri: Uri? = null
    private lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        _binding = SignupActivityBinding.inflate(this.layoutInflater)
        binding.loading.visibility = View.GONE
        viewModel = ViewModelProvider(this)[SignUpViewModel::class.java]
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        val usernameEditText = binding.username.editText
        val passwordEditText = binding.password.editText
        val confirmPasswordEditText = binding.confirmPassword.editText
        val emailEditText = binding.emailText.editText
        val loginBtn = binding.loginBtn
        val signUpBtn = binding.signupBtn
        val loadingProgressBar = binding.loading
        val selectPhotoBtn = binding.photoSelector
        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    selectedPhotoUri = result.data?.data
                    binding.profileImage.setImageURI(selectedPhotoUri)
                    selectPhotoBtn.alpha = 0f
                    binding.addProfilePic.visibility = View.GONE
                }
            }
        selectPhotoBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            resultLauncher.launch(intent)
        }
        signUpBtn.setOnClickListener {
            when {
                usernameEditText!!.text.isEmpty() -> usernameEditText.error = "Enter name"
                emailEditText!!.text.isEmpty() -> emailEditText.error = "Enter Email"
                passwordEditText!!.text.isEmpty() -> passwordEditText.error = "Enter password"
                confirmPasswordEditText!!.text.isEmpty() -> confirmPasswordEditText.error =
                    "Confirm Password"
                passwordEditText.text.toString() != confirmPasswordEditText.text.toString() -> confirmPasswordEditText.error =
                    "password don't match"
                else -> {
                    binding.apply {
                        confirmPasswordEditText.addTextChangedListener {
                            confirmPasswordEditText.error = null
                        }
                        passwordEditText.addTextChangedListener {
                            passwordEditText.error = null
                        }
                        username.editText?.addTextChangedListener {
                            username.error = null
                        }
                        emailText.editText?.addTextChangedListener {
                            emailText.error = null
                        }
                    }
                    val username = usernameEditText.text.toString()
                    val email = emailEditText.text.toString().trim { it <= ' ' }
                    val userPwd = passwordEditText.text.toString().trim { it <= ' ' }
                    loadingProgressBar.visibility = ProgressBar.VISIBLE
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.createUser(
                            username,
                            binding,
                            selectedPhotoUri,
                            this@SignUpActivity,
                            email,
                            userPwd
                        )
                    }
                    binding.signupBtn.isEnabled = false
                    binding.loginBtn.isEnabled = false

                }
            }
        }
        loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}