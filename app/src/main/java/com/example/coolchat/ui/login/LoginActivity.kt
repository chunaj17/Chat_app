package com.example.coolchat.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.size
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.coolchat.R
import com.example.coolchat.databinding.LoginActivityBinding
import com.example.coolchat.ui.chat.ChatActivity
import com.example.coolchat.ui.forgot.Forgot
import com.example.coolchat.ui.signup.SignUpActivity
import com.example.coolchat.ui.user.UserActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private var _binding: LoginActivityBinding? = null
    private val binding get() = _binding!!
    lateinit var viewModel: LoginViewModel
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = LoginActivityBinding.inflate(this.layoutInflater)
        auth = FirebaseAuth.getInstance()
        binding.loading.visibility = View.GONE
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        val emailEditText = binding.email.editText
        val passwordEditText = binding.password.editText
        val loginButton = binding.loginBtn
        val loadingProgressBar = binding.loading
        loginButton.setOnClickListener {
            when {
                emailEditText!!.text.isEmpty() -> emailEditText.error = "Enter Email"
                passwordEditText!!.text.isEmpty() -> passwordEditText.error = "Enter password"
                else -> {
                    val email = emailEditText.text.toString().trim { it <= ' ' }
                    val password = passwordEditText.text.toString().trim { it <= ' ' }
                    loadingProgressBar.visibility = ProgressBar.VISIBLE
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.authUser(email, password, binding, this@LoginActivity)
                    }
                    binding.loginBtn.isEnabled = false
                    binding.signupBtn.isEnabled = false
                }
            }
        }
        binding.forgotPsw.setOnClickListener {
            Intent(this, Forgot::class.java).also {
                startActivity(it)
            }
            finish()
        }
        binding.signupBtn.setOnClickListener {
            Intent(this, SignUpActivity::class.java).also {
                startActivity(it)
            }
            finish()
        }
        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).result
                    account?.let {
                        googleAuthForFirebase(it)
                    }
                }
            }
        binding.googleLoginBtn.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            binding.loginBtn.isEnabled = false
            binding.signupBtn.isEnabled = false
            binding.googleLoginBtn.isEnabled = false
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.webclient_id))
                .requestEmail()
                .requestProfile()
                .build()
            val signInClient = GoogleSignIn.getClient(this, options)
            signInClient.signInIntent.also {
                resultLauncher.launch(it)
            }
        }
    }

    private fun googleAuthForFirebase(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signInWithCredential(credentials)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                                viewModel.saveUserToFirebaseDb(getUserName(),getPhotoUrl(),binding,this@LoginActivity)
                        }
                    }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        "error: ${e.message.toString()}",
                        Toast.LENGTH_LONG
                    ).show()

                    binding.loading.visibility = View.VISIBLE
                    binding.loginBtn.isEnabled = true
                    binding.signupBtn.isEnabled = true
                    binding.googleLoginBtn.isEnabled = true
                }
            }
        }
    }
    private fun getPhotoUrl(): String? {
        val user = auth.currentUser
        return user?.photoUrl?.toString()
    }

    private fun getUserName(): String? {
        val user = auth.currentUser
        return if (user != null) {
            user.displayName
        } else ANONYMOUS
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    companion object {
        const val ANONYMOUS = "anonymous"
    }
}
