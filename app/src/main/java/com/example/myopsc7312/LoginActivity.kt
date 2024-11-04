package com.example.myopsc7312

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class LoginActivity : AppCompatActivity() {

    // Declare variables for UI components
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpTextView: TextView
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {

        // Initialize DatabaseHelper and insert user data
        val dbHelper = DatabaseHelper(this)
        dbHelper.insertUser("Umara2003@gmail.com", "UAhmed@123")

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
        if (NetworkUtil.isNetworkAvailable(this)) {
        //database.addListenerForSingleValueEvent(object : ValueEventListener
            database.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var userFound = false
                for (userSnapshot in dataSnapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && user.password == password && user.email == email) {
                            userFound = true
                            val userUId = userSnapshot.key // This retrieves the user's ID from Firebase

                            // Store user data locally
                            val dbHelper = DatabaseHelper(this@LoginActivity)
                            dbHelper.insertUser(user.email, user.password)

                            Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                            // Navigate to Home or Dashboard and pass the userId
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            intent.putExtra("userUid", userUId) // Pass userId to the next activity
                            startActivity(intent)
                            finish() // Optionally close login screen
                            break
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
        } else {
            // Check local credentials
            val dbHelper = DatabaseHelper(this)
            if (dbHelper.checkUserCredentials(email, password)) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }
    }

   // data class User(val email: String = "", val password: String = "")
}