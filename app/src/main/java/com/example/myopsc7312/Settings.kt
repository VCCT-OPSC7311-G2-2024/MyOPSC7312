package com.example.myopsc7312

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

class Settings : Fragment() {

    //Database reference and shared preferences
    var currentUserId ="";
    val database = Firebase.database
    val Ref = database.getReference("users")
    // Variables to hold the original email and password
    var originalEmail = ""
    var originalPassword = ""

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
    //Nav buttons
    private lateinit var converterNavBtn: ImageButton
    private lateinit var homeNavBtn: ImageButton
    private lateinit var settingsNavBtn: ImageButton

    //Holds constants for fragment
    companion object {
        private const val ACTION_WIRELESS_SETTINGS = "android.settings.WIRELESS_SETTINGS"
        private const val PREFS_NAME = "MyAppPreferences"
        private const val KEY_NOTIFICATIONS_ENABLED = "notificationsEnabled"
        private const val KEY_ONLINE_MODE = "onlineMode"
        private const val KEY_DARK_MODE = "darkModeEnabled"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

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
        languageChange()
        setupDarkModeToggle()
        loadDarkModePreference()

        currentUserId = arguments?.getString("userUid").toString()
        Toast.makeText(requireContext(), currentUserId, Toast.LENGTH_SHORT).show()
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

                //store original data
                originalEmail = username.toString()
                originalPassword = password.toString()

                // Set the retrieved data to the EditText fields
                usernameField.setText(username)
                passwordField.setText(password)

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
        //notificationCheckBox = view.findViewById(R.id.notificationCheckBox)
        onlineCheckBox = view.findViewById(R.id.offlineCheckBox)
        languageSpinner = view.findViewById(R.id.languageSpinner)

        //nav buttons
        //converterNavBtn = view.findViewById(R.id.currencyNavBtn)
        homeNavBtn = view.findViewById(R.id.homeNavBtn)

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Load saved preferences for checkboxes
    private fun loadPreferences() {
        //notificationCheckBox.isChecked = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        onlineCheckBox.isChecked = sharedPreferences.getBoolean(KEY_ONLINE_MODE, false)
    }

    // Set up button click listeners
    private fun setupButtonListeners() {
        signOutBtn.setOnClickListener { signOut() }
        saveBtn.setOnClickListener { saveChanges() }
        editBtn.setOnClickListener { enableEditing() }

        //navigation functions
        /*converterNavBtn.setOnClickListener {
            val intent = Intent(requireContext(), CurrencyConverterAPI::class.java)
            intent.putExtra("userUid", currentUserId)
            startActivity(intent)
        }*/
        homeNavBtn.setOnClickListener {
            // Create a Bundle to pass data
            val bundle = Bundle()
            bundle.putString("userUid", currentUserId)
            val accountFragment = AccountFragment()
            accountFragment.arguments = bundle
            // Perform fragment transaction to navigate to AddAccountFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, accountFragment)
                .addToBackStack(null)  // Add to back stack so that user can return to AccountsFragment
                .commit()
        }
    }

    private fun setupDarkModeToggle() {
        val darkModeSwitch = view?.findViewById<Switch>(R.id.darkModeSwitch)
        darkModeSwitch?.isChecked = isDarkModeEnabled()

        darkModeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            saveDarkModePreference(isChecked)
        }
    }

    private fun saveDarkModePreference(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isEnabled).apply()
    }

    private fun loadDarkModePreference() {
        val isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        if (isDarkMode && !isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else if (!isDarkMode && isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun isDarkModeEnabled(): Boolean {
        val currentMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return currentMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    private fun languageChange() {
        // Array of languages
        val languages = arrayOf("English", "Tsonga", "Afrikaans")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter


        // Load the saved language from SharedPreferences
        val savedLanguageCode = sharedPreferences.getString("languageCode", "en")
        val savedLanguagePosition = when (savedLanguageCode) {
            "en" -> 0
            "ts" -> 1
            "afaf-rZA" -> 2
            else -> 0
        }
        languageSpinner.setSelection(savedLanguagePosition)


        // Set up listener for language selection
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> setLocale("en")  // English
                    1 -> setLocale("ts")  // Tsonga
                    2 -> setLocale("af-rZA")  // Afrikaans
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }
    }
    // Method to change the locale
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)

        // Save the selected language to SharedPreferences
        with(sharedPreferences.edit()) {
            putString("languageCode", languageCode)
            apply()
        }

        // Restart the fragment to apply the language change
        parentFragmentManager.beginTransaction().detach(this).attach(this).commit()
    }


    // Set up checkbox change listeners
    private fun setupCheckboxListeners() {
        //notificationCheckBox.setOnCheckedChangeListener { _, isChecked -> handleNotificationChange(isChecked) }
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
        saveBtn.isEnabled = true
        editBtn.visibility = View.GONE
        usernameField.isEnabled = true
        passwordField.isEnabled = true
        //notificationCheckBox.isEnabled = true
        onlineCheckBox.isEnabled = true
    }

    private fun validateNewInputs(username:String ,password: String): Boolean {

        if(username.isEmpty()|| password.isEmpty()){

            Toast.makeText(requireContext(), "Username  and password field empty", Toast.LENGTH_SHORT).show()
            return false

        }else if(username.equals(originalEmail) && password.equals(originalPassword) ) {

            Toast.makeText(requireContext(), "No changes made", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // Save changes to the user profile in firebase
    private fun saveChanges() {
        signOutBtn.visibility = View.VISIBLE
        saveBtn.visibility = View.GONE
        usernameField.isEnabled = false
        passwordField.isEnabled = false
        //notificationCheckBox.isEnabled = false
        onlineCheckBox.isEnabled = false

        // Save the changes to the database
        val username = usernameField.text.toString()
        val password = passwordField.text.toString()
        val newLanguage = languageSpinner.selectedItem.toString()

        if(validateNewInputs(username, password) == true || newLanguage != getCurrentLanguage())
        {
            //val notifications = notificationCheckBox.isChecked
            val onlineMode = onlineCheckBox.isChecked


            // Get the user reference
            val userRef = database.getReference("users").child(currentUserId)

            //packaging the data to be updated
            val updates = mapOf(
                "email" to username,
                "password" to password
            )

            // Update the user data in the database
            userRef.updateChildren(updates).addOnSuccessListener {
                // Update successful
                Toast.makeText(requireContext(), "User data updated successfully", Toast.LENGTH_SHORT).show()
                //send notification
               /* if (notifications == true) {
                    val message = "Your profile has been updated"
                    Log.d("Notification", message)
                    val notificationHelper = NotificationHelper(this.requireContext())
                    notificationHelper.createNotification("Profile Update", message)
                    //NotificationUtils.sendNotification("all", "Profile Update", message)
                }*/

            }.addOnFailureListener { e ->
                // Update failed
                Toast.makeText(requireContext(), "Failed to update user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            // Save the changes to SharedPreferences
            //savePreference(KEY_NOTIFICATIONS_ENABLED, notifications)
            savePreference(KEY_ONLINE_MODE, onlineMode)

        }
    }

    private fun getCurrentLanguage(): String {
        return when (Locale.getDefault().language) {
            "en" -> "English"
            "ts" -> "Tsonga"
            "af-rZA" -> "Afrikaans"
            else -> "English"
        }
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
