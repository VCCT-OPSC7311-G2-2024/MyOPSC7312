package com.example.myopsc7312

import android.accounts.Account
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AccountFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var accountListContainer: LinearLayout
    private lateinit var userUid: String // Store user UID
    private lateinit var assetsValueTextView: TextView // TextView for displaying total assets

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        // Initialize Firebase Database
        userUid = requireActivity().intent.getStringExtra("USER_UID") ?: ""
        database = FirebaseDatabase.getInstance().getReference("accounts").child(userUid)

        // Reference to the LinearLayout that will contain account details
        accountListContainer = view.findViewById(R.id.AccountListContainer)

        // Find the FrameLayout by its ID
        val addAccountFrame: FrameLayout = view.findViewById(R.id.AddAccountLayout)
        // Reference to the TextView for total assets value
        assetsValueTextView = view.findViewById(R.id.AssetsValue)

        // Set the OnClickListener for the FrameLayout
        addAccountFrame.setOnClickListener {
            // Perform fragment transaction to navigate to AddAccountFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddAccountFragment())
                .addToBackStack(null)  // Add to back stack so that user can return to AccountsFragment
                .commit()
        }
        // Load and display the accounts for the logged-in user
        loadAccounts()
        return view
    }

    private fun loadAccounts() {
        // Retrieve accounts from Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the container first
                accountListContainer.removeAllViews()
                var totalBalance = 0.0 // Variable to store the total balance
                // Loop through the children of the snapshot
                for (accountSnapshot in snapshot.children) {
                    // Get the Account object from Firebase
                    val account = accountSnapshot.getValue(Account::class.java)
                    val accountId = accountSnapshot.key

                    // If account is not null, inflate the account_item.xml layout
                    if (account != null) {
                        val accountItemView = layoutInflater.inflate(R.layout.account_item, null)

                        // Find and set the TextViews for account name and balance
                        val accountNameText = accountItemView.findViewById<TextView>(R.id.SavingsText)
                        val accountBalanceText = accountItemView.findViewById<TextView>(R.id.SavingsValue)

                        accountNameText.text = account.name
                        accountBalanceText.text = "R " + account.balance // Assuming the balance is in Rands

                        // Set an OnClickListener for the account item
                        accountItemView.setOnClickListener {
                            if (accountId != null) {
                                openBudgetFragment(accountId)
                            }
                        }

                        // Add the inflated view to the LinearLayout
                        accountListContainer.addView(accountItemView)
                        // Add the balance to the totalBalance
                        totalBalance += account.balance.toDoubleOrNull() ?: 0.0
                    }
                }
                // Display the total balance in the AssetsValue TextView
                assetsValueTextView.text = "R %.2f".format(totalBalance)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
            }
        })
    }

    private fun openBudgetFragment(accountId: String) {
        val fragment = budget()

        // Create a bundle to pass the accountId to the fragment
        val bundle = Bundle()
        bundle.putString("accountId", accountId)
        fragment.arguments = bundle

        // Navigate to the fragment_budget
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Replace with your container ID
            .addToBackStack(null)
            .commit()
    }


    // Account data class
    data class Account(val name: String = "", val balance: String = "", val type: String = "")
}