package com.example.myopsc7312

import android.os.Bundle
import android.text.InputFilter
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
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
    private lateinit var accountUid: String // Store user UID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_budget, container, false)
        name = view.findViewById(R.id.enterName)
        amount = view.findViewById(R.id.enterBudget)
        budgetListContainer = view.findViewById(R.id.budgetListContainer)

        // Retrieve the accountId from arguments
        accountUid = arguments?.getString("accountId").toString()
        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("accounts/$accountUid/budgets")

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

        val accountId = database.push().key!!
        val account = Budget(enterName, enterAmount, )

        database.child(accountId).setValue(account)
            .addOnCompleteListener {
                // Success message
                Toast.makeText(requireContext(), "Budget added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Failure message
                Toast.makeText(requireContext(), "Failed to add budget", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadBudgets() {
        // Retrieve accounts from Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the container first
                budgetListContainer.removeAllViews()
                // Loop through the children of the snapshot
                for (budgetSnapshot in snapshot.children) {
                    // Get the Account object from Firebase
                    val budget = budgetSnapshot.getValue(Budget2::class.java)

                    // If account is not null, inflate the account_item.xml layout
                    if (budget != null) {
                        val budgetItemView = layoutInflater.inflate(R.layout.budget_item, null)

                        // Find and set the TextViews for account name and balance
                        val budgetNameText = budgetItemView.findViewById<TextView>(R.id.BudgetName)
                        val budgetBalanceText = budgetItemView.findViewById<TextView>(R.id.BudgetValue)

                        budgetNameText.text = budget.name
                        budgetBalanceText.text = "R " + budget.amount // Assuming the balance is in Rands

                        // Add the inflated view to the LinearLayout
                        budgetListContainer.addView(budgetItemView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
            }
        })
    }

    // Account data class
    data class Budget2(val name: String = "", val amount: String = "")
    data class Budget( val name: String, val amount: String)
}