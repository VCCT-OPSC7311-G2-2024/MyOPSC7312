package com.example.myopsc7312

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


class Settings : Fragment() {

    //Database reference and shared preferences
    var currentUserId ="";
    val database = Firebase.database
    val Ref = database.getReference("users")
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth

    // UI components
    private lateinit var signOutBtn: Button
    private lateinit var editBtn: Button
    private lateinit var saveBtn: Button
    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var notificationCheckBox: CheckBox
    private lateinit var onlineCheckBox: CheckBox
    private lateinit var languageSpinner: Spinner

    //Holds constants for fragment
    companion object {
        private const val ACTION_WIRELESS_SETTINGS = "android.settings.WIRELESS_SETTINGS"
        private const val PREFS_NAME = "MyAppPreferences"
        private const val KEY_NOTIFICATIONS_ENABLED = "notificationsEnabled"
        private const val KEY_ONLINE_MODE = "onlineMode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialiseUI(view)
        loadPreferences()
        setupButtonListeners()
        setupCheckboxListeners()

        currentUserId = arguments?.getString("USER_ID").toString()
        if (currentUserId != null) {
            // Now you have the userId, use it to populate user data
            populateUserData(currentUserId)
        } else {
            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateUserData(userId: String) {
        val userRef = database.getReference("users").child(userId)
        // Attach a listener to read the data at the specified user reference
        userRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                // Get the username and password from the snapshot
                val username = dataSnapshot.child("email").getValue(String::class.java)
                val password = dataSnapshot.child("password").getValue(String::class.java)

                // Set the retrieved data to the EditText fields
                usernameField.setText(username)
                passwordField.setText(password)
                Toast.makeText(requireContext(), "${usernameField.text},1", Toast.LENGTH_SHORT).show()
                Toast.makeText(requireContext(), "${passwordField.text},2", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
        }
    }

    //Initialise UI components
    private fun initialiseUI( view: View){
        signOutBtn = view.findViewById(R.id.sign_outBtn)
        editBtn = view.findViewById(R.id.editProfileBtn)
        saveBtn = view.findViewById(R.id.saveChangesBtn)
        usernameField = view.findViewById(R.id.usernameTxt)
        passwordField = view.findViewById(R.id.passwordTxt)
        notificationCheckBox = view.findViewById(R.id.notificationCheckBox)
        onlineCheckBox = view.findViewById(R.id.offlineCheckBox)
        languageSpinner = view.findViewById(R.id.languageSpinner)

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Load saved preferences for checkboxes
    private fun loadPreferences() {
        notificationCheckBox.isChecked = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
        onlineCheckBox.isChecked = sharedPreferences.getBoolean(KEY_ONLINE_MODE, false)
    }

    // Set up button click listeners
    private fun setupButtonListeners() {
        signOutBtn.setOnClickListener { signOut() }
        saveBtn.setOnClickListener { saveChanges() }
        editBtn.setOnClickListener { enableEditing() }
    }

    private fun languageChange(){


    }


    // Set up checkbox change listeners
    private fun setupCheckboxListeners() {
        notificationCheckBox.setOnCheckedChangeListener { _, isChecked -> handleNotificationChange(isChecked) }
        onlineCheckBox.setOnCheckedChangeListener { _, isChecked -> handleOnlineModeChange(isChecked) }

    }

    // Handle sign out action
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    // Handle enabling profile editing
    private fun enableEditing() {
        signOutBtn.visibility = View.GONE
        saveBtn.visibility = View.VISIBLE
        usernameField.isEnabled = true
        passwordField.isEnabled = true
        notificationCheckBox.isEnabled = true
        onlineCheckBox.isEnabled = true
    }

    // Save changes (this can be expanded with more specific logic)
    private fun saveChanges() {
        // Logic to save user changes
        // ...
    }



    // Handle online mode checkbox change
    private fun handleOnlineModeChange(isChecked: Boolean) {
        savePreference(KEY_ONLINE_MODE, isChecked)
        // Logic for enabling or disabling online mode
        if (isChecked) {
            // Enable online mode
            connectToNetwork()
        } else {
            // Disable online mode
            disconnectFromNetwork()
        }
    }

    // Check if the device is connected to a network
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    // Open the network settings screen
    private fun connectToNetwork() {
        val intent = Intent(ACTION_WIRELESS_SETTINGS)
        startActivity(intent)
    }

    // Open the network settings screen
    private fun disconnectFromNetwork() {
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        startActivity(intent)
    }

    // Save a preference in SharedPreferences
    private fun savePreference(key: String, value: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    // Handle notification checkbox change
    private fun handleNotificationChange(isChecked: Boolean) {
        savePreference(KEY_NOTIFICATIONS_ENABLED, isChecked)
        if (isChecked) {
            enableNotifications()
        } else {
            disableNotifications()
        }
    }

    // Enable notifications by subscribing to a Firebase topic
    private fun enableNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Subscribed to notifications
                    Toast.makeText(requireContext(), "Notifications enabled", Toast.LENGTH_SHORT).show()
                }else {
                    Toast.makeText(requireContext(), "Couldn't enable notifications", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Disable notifications by unsubscribing from a Firebase topic
    private fun disableNotifications() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("all")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Unsubscribed from notifications
                    Toast.makeText(requireContext(), "Notifications disabled", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(requireContext(), "Couldn't disable notifications", Toast.LENGTH_SHORT).show()
                }
            }
    }

}