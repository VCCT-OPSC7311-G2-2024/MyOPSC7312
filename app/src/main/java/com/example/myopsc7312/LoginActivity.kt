package com.example.myopsc7312

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
private lateinit var auth: FirebaseAuth


class LoginActivity : AppCompatActivity() {

    // Declare variables for UI components
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpTextView: TextView
    private lateinit var forgotPasswordTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_login)

        // Initialize UI components
        emailEditText = findViewById(R.id.et_email)
        passwordEditText = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.regButton)
        signUpTextView = findViewById(R.id.textView3)
        forgotPasswordTextView = findViewById(R.id.textView4)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

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


            // Perform login with Firebase Auth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Login successful, navigate to DashboardActivity
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java) // Change to Accounts Page
                        startActivity(intent)
                    } else {
                        // Login failed, show error message
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                    }
                }
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
    }
}