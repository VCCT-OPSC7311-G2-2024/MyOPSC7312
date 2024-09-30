package com.example.myopsc7312

import android.accounts.Account
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.myopsc7312.databinding.FragmentAddAccountBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddAccountFragment : Fragment() {

    private var binding: FragmentAddAccountBinding? = null
    private lateinit var accountName:EditText
    private lateinit var accountBalance:EditText
    private lateinit var accountType:AutoCompleteTextView
    private lateinit var database: DatabaseReference
    private lateinit var userUid: String // Store user UID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_account, container, false)

        // Retrieve the user's UID from arguments or a stored value
        userUid = requireActivity().intent.getStringExtra("USER_UID") ?: ""

        // Reference to AutoCompleteTextView
        val accountTypeDropdown: AutoCompleteTextView = view.findViewById(R.id.autoCompleteText)
        val accountTypes = resources.getStringArray(R.array.account_types)
        // Create an ArrayAdapter using the accountTypes list
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, accountTypes)
        // Set the adapter to the AutoCompleteTextView
        accountTypeDropdown.setAdapter(adapter)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("accounts").child(userUid) // Save under user's UID

        accountName = view.findViewById(R.id.EnterName)
        accountBalance = view.findViewById(R.id.enterBalance)
        accountType = view.findViewById(R.id.autoCompleteText)

        val btnCancel = view?.findViewById<Button>(R.id.btnCancel)
        btnCancel?.setOnClickListener {
            // Navigate to the Accounts page
            val fragment = AccountFragment()
            val transaction = fragmentManager?.beginTransaction()
            transaction?.replace(R.id.fragment_container, fragment)?.commit()
        }

        val btnAddAccount = view?.findViewById<Button>(R.id.btnAddAccount)
        btnAddAccount?.setOnClickListener {
            saveData()
        }
        return view
    }

    //Save data to Firebase Realtime Database
    private fun saveData() {
        val enterName = accountName.text.toString().trim()
        val enterBalance = accountBalance.text.toString().trim()
        val enterType = accountType.text.toString().trim()


        // Simple validation
        if (enterName.isEmpty() || enterBalance.isEmpty() || enterType.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (enterBalance.toDoubleOrNull() == null || enterBalance.toDouble() < 0) {
            Toast.makeText(requireContext(), "Please enter a valid balance", Toast.LENGTH_SHORT).show()
            return
        }
        // Navigate to the Accounts page
        val fragment = AccountFragment()
        val transaction = fragmentManager?.beginTransaction()
        transaction?.replace(R.id.fragment_container, fragment)?.commit()

        val accountId = database.push().key!!
        val account = Account(enterName, enterBalance, enterType)

        database.child(accountId).setValue(account)
            .addOnCompleteListener {
                // Success message
                Toast.makeText(requireContext(), "Account added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Failure message
                Toast.makeText(requireContext(), "Failed to add account", Toast.LENGTH_SHORT).show()
            }
    }
    data class Account( val name: String, val balance: String, val type: String)
}