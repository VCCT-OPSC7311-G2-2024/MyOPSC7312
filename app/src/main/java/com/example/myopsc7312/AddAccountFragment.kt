package com.example.myopsc7312

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button

class AddAccountFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_account, container, false)
        // Reference to AutoCompleteTextView
        val accountTypeDropdown: AutoCompleteTextView = view.findViewById(R.id.autoCompleteText)
        val accountTypes = resources.getStringArray(R.array.acount_types)
        // Create an ArrayAdapter using the accountTypes list
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, accountTypes)
        // Set the adapter to the AutoCompleteTextView
        accountTypeDropdown.setAdapter(adapter)

        val btnCancel = view?.findViewById<Button>(R.id.btnCancel)
        btnCancel?.setOnClickListener {
            // Navigate to the Accounts page
            val fragment = AccountFragment()
            val transaction = fragmentManager?.beginTransaction()
            transaction?.replace(R.id.fragment_container, fragment)?.commit()
        }

        return view
    }

    fun onCreated(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}