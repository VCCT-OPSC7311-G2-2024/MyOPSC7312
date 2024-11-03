package com.example.myopsc7312

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
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
    private lateinit var userUid: String
    private var currentAccountId = "";
    private lateinit var databaseHelper: DatabaseHelper

    //Firebase database reference
    private lateinit var database: DatabaseReference

    //UI components
    private lateinit var namefield: EditText
    private lateinit var amountfield: EditText
    private lateinit var expenseListContainer: LinearLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_expenses, container, false)
        //initializing the UI components
        namefield = view.findViewById(R.id.namePrompt)
        amountfield = view.findViewById(R.id.expenseField)
        expenseListContainer = view.findViewById(R.id.expenseListContainer)
        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("accounts/$currentAccountId/expenses")

        // Initialize SQLite Database Helper
        databaseHelper = DatabaseHelper(requireContext())

        // Retrieve the accountId from arguments
        currentAccountId = arguments?.getString("accountId").toString()
        userUid = arguments?.getString("userUid").toString()
        //currentUserId = arguments?.getString("userId").toString()

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("accounts/$currentAccountId/expenses")

        val submitBtn = view.findViewById<Button>(R.id.submitBtn)
        //setting the onclick listener for the submit button
        submitBtn.setOnClickListener {
            saveData()

        }
        val btnBack = view.findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {

            // Create a Bundle to pass data
            val bundle = Bundle()
            bundle.putString("userUid", userUid)
            bundle.putString("accountId", currentAccountId)
            val anyFragment = Anylitics()
            anyFragment.arguments = bundle
            // Perform fragment transaction to navigate to AddAccountFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, anyFragment)
                .addToBackStack(null)  // Add to back stack so that user can return to AccountsFragment
                .commit()

        }

        val filter = InputFilter { source, _, _, _, _, _ ->
            for (i in source.indices) {
                if (!source[i].isLetter()) {
                    return@InputFilter ""  // Block anything that is not a letter
                }
            }
            null
        }
        namefield.filters = arrayOf(filter)
        loadExpenses()
        return view
    }

    private fun saveData() {
        val name = namefield.text.toString().trim()
        val amount = amountfield.text.toString().trim()

        // Simple validation
        if (name.isEmpty() || amount.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if(amount == "0" || amount.toLong() < 0){
            Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        //creates key  for expense
        val expenseId = database.push().key!!
        val expenseObj = Expense(amount.toLong(), name)

        if (NetworkUtil.isNetworkAvailable(requireContext())) {
            // Save to Firebase
        database.child(expenseId).setValue(expenseObj)
            .addOnCompleteListener {
                // Success message
                Toast.makeText(requireContext(), "Expense added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Failure message
                Toast.makeText(requireContext(), "Failed to add expense", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Save to SQLite
            val values = ContentValues().apply {
                put("expense_id", expenseId)
                put("account_id", "some_account_id") // Replace with actual account ID
                put("amount", amount.toLong())
                put("name", name)
                put("synced", 0)
            }
            databaseHelper.insertExpense(expenseId, currentAccountId, amount.toDouble(), name)
            Toast.makeText(requireContext(), "Expense saved locally", Toast.LENGTH_SHORT).show()
        }
    }

    // Sync unsynced expenses when network is available
    private fun syncUnsyncedExpenses() {
        val syncManager = SyncManager(requireContext().applicationContext)
        syncManager.syncSQLiteToFirebase(requireContext())
    }

    override fun onResume() {
        super.onResume()
        syncUnsyncedExpenses()
    }

    // Function to load expenses from Firebase and display them
    private fun loadExpenses() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                expenseListContainer.removeAllViews()

                for (expenseSnapshot in snapshot.children) {
                    // Log the snapshot to debug
                    Log.d("FirebaseData", expenseSnapshot.toString())
                    try{
                    // Attempt to deserialize into the Expense class
                    val expense = expenseSnapshot.getValue(Expense::class.java)

                    if (expense != null) {
                        try {
                            // Inflate the layout for the expense item
                            val expenseItemView = layoutInflater.inflate(R.layout.expense_item, expenseListContainer, false)
                            val expenseId = expenseSnapshot.key
                            // Find and bind the views
                            val expenseNameText = expenseItemView.findViewById<TextView>(R.id.SavingsText)
                            val expenseAmountText = expenseItemView.findViewById<TextView>(R.id.SavingsValue)
                            val binImageView = expenseItemView.findViewById<ImageView>(R.id.expenseBin)

                            // Set the expense data to the views
                            expenseNameText.text = expense.name
                            expenseAmountText.text = expense.amount.toString()

                            // Set an OnClickListener for the bin ImageView to delete the budget
                            binImageView.setOnClickListener {
                                if (expenseId != null) {
                                    // Confirm and delete the budget from the Firebase database
                                    deleteExpense(expenseId)
                                }
                            }

                            // Add the view to the container
                            expenseListContainer.addView(expenseItemView)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(requireContext(), "Error loading expense item: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("FirebaseData", "Expense is null or not deserialized correctly")
                        Toast.makeText(requireContext(), "Invalid expense data", Toast.LENGTH_SHORT).show()
                    }
                }catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Error loading expense item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load expenses", Toast.LENGTH_SHORT).show()
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
    data class Expense( val amount:Long=0,val name: String = "")
}