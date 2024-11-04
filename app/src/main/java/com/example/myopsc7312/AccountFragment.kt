package com.example.myopsc7312

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Profile
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var converterLayout: FrameLayout
    private lateinit var profileLayout: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        // Initialize Firebase Database
        userUid = requireActivity().intent.getStringExtra("userUid") ?: ""
        Toast.makeText(requireContext(), userUid, Toast.LENGTH_SHORT).show()
        database = FirebaseDatabase.getInstance().getReference("accounts").child(userUid)

        // Reference to the LinearLayout that will contain account details
        accountListContainer = view.findViewById(R.id.AccountListContainer)

        // Find the FrameLayout by its ID
        val addAccountFrame: FrameLayout = view.findViewById(R.id.AddAccountLayout)
        val profileFragment: FrameLayout = view.findViewById(R.id.Profileravigator)
        // Reference to the TextView for total assets value
        assetsValueTextView = view.findViewById(R.id.AssetsValue)

        // Set the OnClickListener for the FrameLayout
        addAccountFrame.setOnClickListener {

            // Create a Bundle to pass data
            val bundle = Bundle()
            bundle.putString("userUid", userUid)
            val addAccountFragment = AddAccountFragment()
            addAccountFragment.arguments = bundle
            // Perform fragment transaction to navigate to AddAccountFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, addAccountFragment)
                .addToBackStack(null)  // Add to back stack so that user can return to AccountsFragment
                .commit()
        }

        converterLayout = view.findViewById(R.id.ConverterNavigator)
        converterLayout.setOnClickListener {
            val intent = Intent(requireContext(), CurrencyConverterAPI::class.java)
            intent.putExtra("userUid", userUid)
            startActivity(intent)
        }
        //profileLayout = view.findViewById(R.id.Profileravigator)
        profileFragment.setOnClickListener {
            Toast.makeText(requireContext(), "${userUid} is the userUid", Toast.LENGTH_SHORT).show()
            // Create a Bundle to pass data
            val bundle = Bundle()
            bundle.putString("userUid", userUid)
            val settingsFragment = Settings()
            settingsFragment.arguments = bundle
            // Perform fragment transaction to navigate to AddAccountFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, settingsFragment)
                .addToBackStack(null)  // Add to back stack so that user can return to AccountsFragment
                .commit()
        }
        // Load and display the accounts for the logged-in user
        loadAccounts()
        return view
    }

    private fun loadAccounts() {
        if (NetworkUtil.isNetworkAvailable(requireContext())) {
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
                        try {
                            val accountItemView =
                                layoutInflater.inflate(R.layout.expense_item, accountListContainer,false)

                            // Find and set the TextViews for account name and balance
                            val accountNameText =
                                accountItemView.findViewById<TextView>(R.id.SavingsText)
                            val accountBalanceText =
                                accountItemView.findViewById<TextView>(R.id.SavingsValue)
                            val binImageView = accountItemView.findViewById<ImageView>(R.id.expenseBin)

                            accountNameText.text = account.name
                            accountBalanceText.text =
                                account.balance // Assuming the balance is in Rands

                            // Set an OnClickListener for the account item
                            accountItemView.setOnClickListener {
                                if (accountId != null) {
                                    openAnyliticsFragment(accountId)
                                }
                            }

                            // Set an OnClickListener for the bin ImageView to delete the account
                            binImageView.setOnClickListener {
                                if (accountId != null) {
                                    // Confirm and delete the account from the Firebase database
                                    deleteAccount(accountId)
                                }
                            }

                            // Add the inflated view to the LinearLayout
                            accountListContainer.addView(accountItemView)
                            // Add the balance to the totalBalance
                            totalBalance += account.balance.toDoubleOrNull() ?: 0.0
                        }catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(requireContext(), "Error loading account item: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                // Display the total balance in the AssetsValue TextView
                assetsValueTextView.text = "R %.2f".format(totalBalance)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
            }
        })
        } else {
            // Load accounts from SQLite
            val dbHelper = DatabaseHelper(requireContext())
            val accounts = dbHelper.getAccounts(userUid)
            accountListContainer.removeAllViews()
            var totalBalance = 0.0

            for (account in accounts) {
                try {
                    val accountItemView = layoutInflater.inflate(R.layout.expense_item, accountListContainer, false)
                    val accountNameText = accountItemView.findViewById<TextView>(R.id.SavingsText)
                    val accountBalanceText = accountItemView.findViewById<TextView>(R.id.SavingsValue)
                    val binImageView = accountItemView.findViewById<ImageView>(R.id.expenseBin)

                    accountNameText.text = account.accountName
                    accountBalanceText.text = account.accountBalance.toString()

                    binImageView.setOnClickListener {
                        deleteAccount(account.accountId)
                    }

                    accountListContainer.addView(accountItemView)
                    totalBalance += account.accountBalance
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error loading account item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            assetsValueTextView.text = "R %.2f".format(totalBalance)
        }
    }

    private fun createAccountOffline(accountId: String, userId: String, balance: Double, name: String, type: String) {
        val dbHelper = DatabaseHelper(requireContext())
        dbHelper.insertAccount(accountId, userId, balance, name, type, 0) // Mark as unsynced
    }

    private fun openAnyliticsFragment(accountId: String) {
        val fragment = Anylitics()

        // Create a bundle to pass the accountId to the fragment
        val bundle = Bundle()
        bundle.putString("accountId", accountId)
        bundle.putString("userUid", userUid)
        fragment.arguments = bundle

        // Navigate to the fragment
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Replace with your container ID
            .addToBackStack(null)
            .commit()
    }


    private fun deleteAccount(accountId: String) {
        // Reference to the account in the database
        val accountRef = database.child(accountId)

        // Remove the account from the database
        accountRef.removeValue().addOnSuccessListener {
            // Account successfully deleted, you can display a success message or toast
            showToast("Account deleted successfully")
        }.addOnFailureListener {
            // Failed to delete the account, handle the error
            showToast("Failed to delete account")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }



    // Account data class
    data class Account( val balance: String = "", val name: String = "", val type: String = "")
}