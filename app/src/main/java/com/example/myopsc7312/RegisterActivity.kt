package com.example.myopsc7312

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var regButton: Button
    private lateinit var myTextView: TextView
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_regsiter)


        // Initialize views
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.editTextPassword)
        etConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        regButton = findViewById(R.id.regButton)
        myTextView = findViewById(R.id.myTextView)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Set up register button click listener
        regButton.setOnClickListener {
            registerUser()
        }

        // Set up sign in TextView click listener
        myTextView.setOnClickListener {
            // Navigate to SignInActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun registerUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Simple validation
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            return
        }


        if (!password.matches(Regex(".*[A-Z].*"))) {
            Toast.makeText(this, "Password must contain at least one uppercase letter", Toast.LENGTH_SHORT).show()
            return
        }

        if (!password.matches(Regex(".*[0-9].*"))) {
            Toast.makeText(this, "Password must contain at least one digit", Toast.LENGTH_SHORT).show()
            return
        }

        if (!password.matches(Regex(".*[!@#\$%^&*].*"))) {
            Toast.makeText(this, "Password must contain at least one special character (!@#\$%^&*)", Toast.LENGTH_SHORT).show()
            return
        }

        // Save user to Firebase Realtime Database
        val userId = database.push().key
        val user = User(email, password)
        if (userId != null) {
            database.child("users").child(userId).setValue(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    // Clear all fields
                    etEmail.text.clear()
                    etPassword.text.clear()
                    etConfirmPassword.text.clear()

                    // Navigate to new activity
                    val intent = Intent(this, HomeActivity::class.java) // Change to Accounts Page
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // Show success message
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
    }
    data class User(val email: String, val password: String)
}
