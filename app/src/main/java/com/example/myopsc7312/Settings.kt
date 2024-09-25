package com.example.myopsc7312

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Settings.newInstance] factory method to
 * create an instance of this fragment.
 */
class Settings : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val database = Firebase.database
    val myRef = database.getReference("message")
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
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

        // Buttons
        val signOutBtn = view.findViewById<Button>(R.id.sign_outBtn)
        val editBtn = view.findViewById<Button>(R.id.editProfileBtn)
        val saveBtn = view.findViewById<Button>(R.id.saveChangesBtn)
        //edit text fields
        val usernamefield = view.findViewById<EditText>(R.id.usernameTxt)
        val passwordField = view.findViewById<EditText>(R.id.passwordTxt)
        //checkboxes
        val notiCheckBox = view.findViewById<CheckBox>(R.id.notificationCheckBox)
        val onlineCheckbox = view.findViewById<CheckBox>(R.id.offlineCheckBox)






        sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)

        // Load saved checkbox state
        val notiChecked = sharedPreferences.getBoolean("notificationsEnabled", false)
        notiCheckBox.isChecked = notiChecked

        val isOnline = sharedPreferences.getBoolean("onlineMode", false)
        onlineCheckbox.isChecked = isOnline

        // Sign out button
        signOutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            // Navigate to login fragment
            //testPage(Login())
        }

        // Save changes button
        saveBtn.setOnClickListener{

        }

        // Edit profile button allowing fields to change
        editBtn.setOnClickListener {
            signOutBtn.visibility = View.GONE
            saveBtn.visibility = View.VISIBLE
            // Enable fields
            usernamefield.isEnabled = true
            passwordField.isEnabled = true
            notiCheckBox.isEnabled = true
            onlineCheckbox.isEnabled = true
        }


        // Notification checkbox functionality
        notiCheckBox.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationPreference(isChecked)
            if (isChecked) {
                enableNotifications()
            } else {
                disableNotifications()
            }
        }

        // Online checkbox functionality
        onlineCheckbox.setOnClickListener {
            if (onlineCheckbox.isChecked){
                // Enable online mode
            } else {
                // Disable online mode
            }
        }

    }

    // Save notification preference to SharedPreferences
    private fun saveNotificationPreference(isEnabled: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("notificationsEnabled", isEnabled)
        editor.apply()
    }

    // Enable notifications by subscribing to Firebase topic
    private fun enableNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Subscribed to notifications
                }
            }
    }

    // Disable notifications by unsubscribing from Firebase topic
    private fun disableNotifications() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("all")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Unsubscribed from notifications
                }
            }
    }




    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Settings.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Settings().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}