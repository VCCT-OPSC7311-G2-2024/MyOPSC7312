package com.example.myopsc7312

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.example.myopsc7312.AccountFragment.Account
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Anylitics : Fragment() {

    private lateinit var statsBlock: FrameLayout
    private lateinit var budgetBlock: FrameLayout
    private lateinit var accountUid: String // Store account UID
    private lateinit var userUid: String
    private lateinit var database: DatabaseReference
    private lateinit var accountBalance: TextView
    private lateinit var homeBtn: FrameLayout
    private lateinit var converterLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_anylitics, container, false)

        // Retrieve the accountId from arguments
        accountUid = arguments?.getString("accountId").toString()
        userUid = arguments?.getString("userUid").toString()

        // Initialize Firebase Database reference for this account
        database = FirebaseDatabase.getInstance().getReference("accounts").child(accountUid)

        // Initialize views
        accountBalance = view.findViewById(R.id.txtBalanceValue)

        // Load the account details
        loadAccountDetails()

        // Find the BudgetBlock and set an OnClickListener
        budgetBlock = view.findViewById(R.id.BudgetBlock)
        budgetBlock.setOnClickListener {
            openBudgetFragment(accountUid)
        }
        //Set onClickListener for StatsBlock
        statsBlock = view.findViewById(R.id.StatsBlock)
        statsBlock.setOnClickListener {
            openExpenseFragment(accountUid)
        }

        //Set onClickListener for HomeButton
        homeBtn = view.findViewById(R.id.HomeNavigator)
        homeBtn.setOnClickListener {

            // Create a Bundle to pass data
            val bundle = Bundle()
            bundle.putString("userUid", userUid)
            val accountFragment = AccountFragment()
            accountFragment.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, accountFragment)
                .addToBackStack(null)
                .commit()
        }

        converterLayout = view.findViewById(R.id.ConverterNavigator)
        converterLayout.setOnClickListener {
            val intent = Intent(requireContext(), CurrencyConverterAPI::class.java)
            intent.putExtra("userUid", userUid)
            startActivity(intent)
        }

        val profileFragment: FrameLayout = view.findViewById(R.id.ProfileNavigator)
        //profileLayout = view.findViewById(R.id.Profileravigator)
        profileFragment.setOnClickListener {
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

        return view
    }

    private fun openBudgetFragment(accountId: String, ) {
        val fragment = budget()

        // Create a bundle to pass the accountId to the fragment
        val bundle = Bundle()
        bundle.putString("accountId", accountId)
        bundle.putString("userUid", userUid)
        fragment.arguments = bundle

        // Navigate to the fragment_budget
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Replace with your container ID
            .addToBackStack(null)
            .commit()
    }

    private fun openExpenseFragment(accountId: String) {
        val fragment = Expenses()

        // Create a bundle to pass the accountId to the fragment
        val bundle = Bundle()
        bundle.putString("accountId", accountId)
        bundle.putString("userUid", userUid)
        fragment.arguments = bundle

        // Navigate to the fragment_budget
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Replace with your container ID
            .addToBackStack(null)
            .commit()
    }

    private fun loadAccountDetails() {
        // Retrieve the account data from Firebase
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val account = snapshot.getValue(Account::class.java)

                // If account data exists, update the views
                account?.let {
                    accountBalance.text = "Balance: R " + account.balance
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
            }
        })
    }
    // Define the Account data class (if necessary)
    data class Account(val name: String = "", val balance: String = "", val type: String = "")
}
