package com.example.myopsc7312

data class User(
    val email: String,
    val password: String,
    val accounts: MutableList<Account> = mutableListOf<Account>() // List of accounts associated with the user
)
