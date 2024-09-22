package com.example.myopsc7312

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Register : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var regButton: Button
    private lateinit var myTextView: TextView
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.user_regsiter, container, false)

        // Initialize views
        etName = view.findViewById(R.id.et_name)
        etUsername = view.findViewById(R.id.et_username)
        etEmail = view.findViewById(R.id.et_email)
        etPassword = view.findViewById(R.id.editTextPassword)
        etConfirmPassword = view.findViewById(R.id.editTextConfirmPassword)
        regButton = view.findViewById(R.id.regButton)
        myTextView = view.findViewById(R.id.myTextView)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Set up register button click listener
        regButton.setOnClickListener {
            registerUser()
        }

        // Set up sign in TextView click listener
        myTextView.setOnClickListener {
            // Navigate to SignInActivity
            val intent = Intent(activity, Login::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Simple validation
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        //logic to save the user to a database or authenticate

        // Save user to Firebase Realtime Database
        val userId = database.push().key
        val user = User(name, username, email, password)
        if (userId != null) {
            database.child("users").child(userId).setValue(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // Show success message
        Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
    }
}
data class User(val name: String, val username: String, val email: String, val password: String)