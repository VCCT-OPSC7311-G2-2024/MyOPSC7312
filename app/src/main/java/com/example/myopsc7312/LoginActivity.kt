package com.example.myopsc7312

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*


class LoginActivity : AppCompatActivity() {

    // Declare variables for UI components
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpTextView: TextView
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_login)

        // Initialize UI components
        emailEditText = findViewById(R.id.et_email)
        passwordEditText = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.regButton)
        signUpTextView = findViewById(R.id.textView3)
        forgotPasswordTextView = findViewById(R.id.textView4)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference.child("users")

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
                            // Store the user ID (uid)
                            val uid = userSnapshot.key // Assuming the key is the UID
                            Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                            // Navigate to Home or Dashboard
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java).apply {
                                putExtra("USER_UID", uid) // Pass the UID to the Home Activity
                            }
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

    data class User(val email: String = "", val password: String = "")
}