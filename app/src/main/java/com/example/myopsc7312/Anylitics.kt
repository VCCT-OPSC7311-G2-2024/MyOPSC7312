package com.example.myopsc7312

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
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
    private lateinit var database: DatabaseReference
    private lateinit var accountBalance: TextView


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

        return view
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

    private fun openExpenseFragment(accountId: String) {
        val fragment = Expenses()

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
