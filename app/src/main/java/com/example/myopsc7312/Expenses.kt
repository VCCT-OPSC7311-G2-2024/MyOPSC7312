package com.example.myopsc7312

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Expenses : Fragment() {
    //variables for the fragment
    var currentUserId = "";
    private var currentAccountId = "";

    //Firebase database reference
    private lateinit var database: DatabaseReference

    //UI components
    private lateinit var namefield: EditText
    private lateinit var amountfield: EditText
    private lateinit var expenseListContainer: LinearLayout
    private lateinit var submitBtn: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return  inflater.inflate(R.layout.fragment_expenses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //initializing the UI components
        namefield = view.findViewById(R.id.namePrompt)
        amountfield = view.findViewById(R.id.expenseField)
        expenseListContainer = view.findViewById(R.id.expenseListContainer)
        submitBtn = view.findViewById(R.id.submitBtn)

        // Retrieve the accountId from arguments
        currentAccountId = arguments?.getString("accountId").toString()
        //currentUserId = arguments?.getString("userId").toString()

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("accounts/$currentAccountId/expenses")

        //setting the onclick listener for the submit button
        submitBtn.setOnClickListener {
            saveData()
            loadExpenses()
        }
    }

    private fun saveData() {
        val name = namefield.text.toString().trim()
        val amount = amountfield.text.toString().trim()

        // Simple validation
        if (name.isEmpty() || amount.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        //creates key  for expense
        val expenseId = database.push().key!!
        val expenseObj = Expense(name, amount)

        database.child(expenseId).setValue(expenseObj)
            .addOnCompleteListener {
                // Success message
                Toast.makeText(requireContext(), "Expense added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Failure message
                Toast.makeText(requireContext(), "Failed to add expense", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadExpenses() {
        // Retrieve expense from Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the container first
                expenseListContainer.removeAllViews()
                // Loop through the children of the snapshot
                for (expenseSnapshot in snapshot.children) {
                    // Get the expense object from Firebase
                    val expense = expenseSnapshot.getValue(Expense::class.java)

                    // If expense is not null, inflate the expense_item.xml layout
                    if (expense != null) {
                        val expenseItemView = layoutInflater.inflate(R.layout.expense_item, expenseListContainer, false)

                        val expenseId = expenseSnapshot.key
                        // Find and set the TextViews for expense name and balance
                        val expenseNameText = expenseItemView.findViewById<TextView>(R.id.SavingsText)
                        val expenseBalanceText = expenseItemView.findViewById<TextView>(R.id.SavingsValue)
                        val binImageView = expenseItemView.findViewById<ImageView>(R.id.expenseBin)

                        expenseNameText.text = expense.name
                        expenseBalanceText.text = expense.amount

                        // Set an OnClickListener for the bin ImageView to delete the expense
                        binImageView.setOnClickListener {
                            if (expenseId != null) {
                                // Confirm and delete the expense from the Firebase database
                                deleteExpense(expenseId)
                            }
                        }
                        // Add the inflated view to the LinearLayout
                        expenseListContainer.addView(expenseItemView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
            }
        })
    }
    private fun deleteExpense(accountId: String) {
        // Reference to the expense in the database
        val expenseRef = database.child(accountId)

        // Remove the budget from the database
        expenseRef.removeValue().addOnSuccessListener {
            // Budget successfully deleted, you can display a success message or toast
            showToast("Expense deleted successfully")
        }.addOnFailureListener {
            // Failed to delete the expense, handle the error
            showToast("Failed to delete expense")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Expenses().apply {
                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
                }
                }
}
    data class Expense( val name: String = "", val amount: String ="")
}