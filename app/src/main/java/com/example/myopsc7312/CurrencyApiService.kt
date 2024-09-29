package com.example.myopsc7312


data class CurrencyResponse(
    val rates: Map<String, Double>,
    val base: String,
    val date: String
)