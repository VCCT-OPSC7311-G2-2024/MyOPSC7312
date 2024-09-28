package com.example.myopsc7312

data class Account(
    val accountId: String,
    val accountName: String,
    val accountType: String,
    val accountBalance: Double,
    val accounts: MutableList<Category> = mutableListOf<Category>() // List of categories for the account
)
