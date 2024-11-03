package com.example.myopsc7312

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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

class budget : Fragment() {

    private lateinit var name: EditText
    private lateinit var amount: EditText
    private lateinit var database: DatabaseReference
    private lateinit var budgetListContainer: LinearLayout
    private lateinit var accountUid: String
    private lateinit var userUid: String
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var totalBudget: Double = 0.0
        val view = inflater.inflate(R.layout.fragment_budget, container, false)
        name = view.findViewById(R.id.enterName)
        amount = view.findViewById(R.id.enterBudget)
        budgetListContainer = view.findViewById(R.id.budgetListContainer)

        // Retrieve the accountId from arguments
        accountUid = arguments?.getString("accountId").toString()
        userUid = arguments?.getString("userUid").toString()

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("accounts/$accountUid/budgets")

        // Initialize DatabaseHelper
        databaseHelper = DatabaseHelper(requireContext())

        val btnBack = view.findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {

            // Create a Bundle to pass data
            val bundle = Bundle()
            bundle.putString("userUid", userUid)
            bundle.putString("accountId", accountUid)
            val anyFragment = Anylitics()
            anyFragment.arguments = bundle
            // Perform fragment transaction to navigate to AddAccountFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, anyFragment)
                .addToBackStack(null)  // Add to back stack so that user can return to AccountsFragment
                .commit()

        }
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        btnSubmit.setOnClickListener {
            saveData()
        }
        val filter = InputFilter { source, _, _, _, _, _ ->
            for (i in source.indices) {
                if (!source[i].isLetter()) {
                    return@InputFilter ""  // Block anything that is not a letter
                }
            }
            null
        }
        name.filters = arrayOf(filter)

        // Load budgets for this account
        loadBudgets()
        return view
    }

    //Save data to Firebase Realtime Database
    private fun saveData() {
        val enterName = name.text.toString().trim()
        val enterAmount = amount.text.toString().trim()

        // Simple validation
        if (enterName.isEmpty() || enterAmount.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (enterAmount.toDoubleOrNull() == null || enterAmount.toDouble() < 1) {
            Toast.makeText(requireContext(), "Please enter a number greater than 0", Toast.LENGTH_SHORT).show()
            return
        }

        val budgetId = database.push().key!!
        val budget = Budget(enterName, enterAmount)

        if (NetworkUtil.isNetworkAvailable(requireContext())) {
            // Save to Firebase
            database.child(budgetId).setValue(budget)
                .addOnCompleteListener {
                    Toast.makeText(requireContext(), "Budget added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add budget", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Save to SQLite
            val values = ContentValues().apply {
                put("budget_id", budgetId)
                put("account_id", accountUid)
                put("amount", enterAmount.toDouble())
                put("name", enterName)
                put("synced", 0)
            }
            databaseHelper.insertBudget(budgetId, accountUid, enterAmount.toDouble(), enterName)
            Toast.makeText(requireContext(), "Budget saved locally", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBudgets() {
        // Retrieve budget from Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the container first
                budgetListContainer.removeAllViews()
                try {
                    // Loop through the children of the snapshot
                    for (budgetSnapshot in snapshot.children) {
                        // Get the budget object from Firebase
                        val budget = budgetSnapshot.getValue(Budget2::class.java)

                        // If budget is not null, inflate the budget_item.xml layout
                        if (budget != null) {
                            val budgetItemView = layoutInflater.inflate(R.layout.budget_item, null)
                            val budgetId = budgetSnapshot.key
                            // Find and set the TextViews for budget name and balance
                            val budgetNameText =
                                budgetItemView.findViewById<TextView>(R.id.BudgetName)
                            val budgetBalanceText =
                                budgetItemView.findViewById<TextView>(R.id.BudgetValue)
                            val binImageView = budgetItemView.findViewById<ImageView>(R.id.bin)

                            budgetNameText.text = budget.name
                            budgetBalanceText.text = budget.amount

                            // Set an OnClickListener for the bin ImageView to delete the budget
                            binImageView.setOnClickListener {
                                if (budgetId != null) {
                                    // Confirm and delete the budget from the Firebase database
                                    deleteBudget(budgetId)
                                }
                            }
                            // Add the inflated view to the LinearLayout
                            budgetListContainer.addView(budgetItemView)
                        }
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error loading budget item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
            }
        })
    }

    private fun deleteBudget(accountId: String) {
        // Reference to the budget in the database
        val budgetRef = database.child(accountId)

        // Remove the budget from the database
        budgetRef.removeValue().addOnSuccessListener {
            // Budget successfully deleted, you can display a success message or toast
            showToast("Budget deleted successfully")
        }.addOnFailureListener {
            // Failed to delete the budget, handle the error
            showToast("Failed to delete budget")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


    // Sync unsynced budgets when network is available
    private fun syncUnsyncedBudgets() {
        val syncManager = SyncManager(requireContext())
        syncManager.syncSQLiteToFirebase(requireContext())
    }

    override fun onResume() {
        super.onResume()
        syncUnsyncedBudgets()
    }

    // Account data class
    data class Budget2(val name: String = "", val amount: String = "")
    data class Budget( val name: String, val amount: String)
}