package com.example.myopsc7312

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check if the saved instance state is null to avoid adding the fragment again on rotation
        if (savedInstanceState == null) {
            val accountFragment = AccountFragment()
            // Get the userId passed from LoginActivity
            val userUid = intent.getStringExtra("userUid")

            // Create a Bundle and put the userId in it
            val bundle = Bundle()
            bundle.putString("userUid", userUid)

            // Set the arguments for the fragment
            accountFragment.arguments = bundle
            // Load the AccountsFragment when the app starts
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, accountFragment)
                .commit()
        }
    }
}