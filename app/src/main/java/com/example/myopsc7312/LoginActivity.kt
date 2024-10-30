package com.example.myopsc7312

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import java.util.concurrent.Executor


class LoginActivity : AppCompatActivity() {

    // Declare variables for UI components
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpTextView: TextView
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private  lateinit var biometricsBtn: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_login)

        // Initialize UI components
        emailEditText = findViewById(R.id.et_email)
        passwordEditText = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.regButton)
        signUpTextView = findViewById(R.id.textView3)
        forgotPasswordTextView = findViewById(R.id.textView4)
        biometricsBtn = findViewById(R.id.imageButton2)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference.child("users")

        // Check if biometric is supported and set it up
        if (checkBiometricSupport()) {
            setUpBiometricPrompt()
        } else {
            Toast.makeText(this, "Biometric authentication not supported", Toast.LENGTH_SHORT).show()
        }

        // Handle login button click
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            // Validate inputs
            if (TextUtils.isEmpty(email)) {
                emailEditText.error = "Username is required"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                passwordEditText.error = "Password is required"
                return@setOnClickListener
            }

            // Perform login by checking credentials in Firebase Database
            loginUser(email, password)
        }


        // Handle sign up click
        signUpTextView.setOnClickListener {
            // Navigate to Sign Up Activity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Handle forgot password click (optional)
        forgotPasswordTextView.setOnClickListener {
            // Navigate to Forgot Password Activity (if applicable)
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }

        // Handle biometrics button click
        biometricsBtn.setOnClickListener {
            showBiometricPrompt()
        }
    }

    private fun loginUser(email: String, password: String) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var userFound = false
                for (userSnapshot in dataSnapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) {
                        if (user.email == email && user.password == password) {
                            userFound = true
                            val userUId = userSnapshot.key // This retrieves the user's ID from Firebase

                            Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                            // Navigate to Home or Dashboard and pass the userId
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            intent.putExtra("userUid", userUId) // Pass userId to the next activity
                            startActivity(intent)
                            finish() // Optionally close login screen
                            break
                        }
                    }
                }
                if (!userFound) {
                    Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@LoginActivity, "Login failed: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setUpBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                // User successfully authenticated
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()
                loginUser(email, password) // Call your login method
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Toast.makeText(this@LoginActivity, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                Toast.makeText(this@LoginActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showBiometricPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun checkBiometricSupport(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }


   // data class User(val email: String = "", val password: String = "")
}