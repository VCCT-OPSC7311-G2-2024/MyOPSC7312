package com.example.myopsc7312

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class CurrencyConverterAPI : AppCompatActivity() {

    private lateinit var amountInput: EditText
    private lateinit var fromCurrency: Spinner
    private lateinit var toCurrency: Spinner
    private lateinit var resultView: TextView
    private lateinit var convertButton: Button

    // Expanded list of global currencies
    private val currencyList = arrayOf(
        "USD", "EUR", "GBP", "INR", "JPY", "AUD", "CAD", "CHF", "CNY", "HKD",
        "KRW", "NZD", "RUB", "SGD", "ZAR", "BRL", "IDR", "MXN", "THB", "TRY"
    )

    // API Key
    private val apiKey = "cc8dd0af722fc6cdcc4a1a1f"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.currency_converter)

        // Initialize UI components
        amountInput = findViewById(R.id.amountInput)
        fromCurrency = findViewById(R.id.fromCurrency)
        toCurrency = findViewById(R.id.toCurrency)
        resultView = findViewById(R.id.resultView)
        convertButton = findViewById(R.id.convertButton)

        // Set up spinners with currency list
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fromCurrency.adapter = adapter
        toCurrency.adapter = adapter

        // Conversion action
        convertButton.setOnClickListener {
            val amount = amountInput.text.toString()
            val from = fromCurrency.selectedItem.toString()
            val to = toCurrency.selectedItem.toString()

            if (amount.isNotEmpty()) {
                getExchangeRateAndConvert(amount.toDouble(), from, to)
            } else {
                resultView.text = "Please enter an amount."
            }
        }
    }

    private fun getExchangeRateAndConvert(amount: Double, from: String, to: String) {
        val url = "https://v6.exchangerate-api.com/v6/$apiKey/latest/$from"

        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("CurrencyConverterAPI", "API call failed: ${e.message}")
                runOnUiThread {
                    resultView.text = "Error: Unable to fetch exchange rate."
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("CurrencyConverterAPI", "Unexpected response: ${response.message}")
                    runOnUiThread {
                        resultView.text = "Error: Unable to fetch exchange rate."
                    }
                    return
                }

                response.body?.let {
                    try {
                        val jsonResponse = JSONObject(it.string())
                        val rates = jsonResponse.getJSONObject("conversion_rates")
                        val rate = rates.getDouble(to)

                        val convertedAmount = amount * rate

                        runOnUiThread {
                            resultView.text = "Converted Amount: $convertedAmount $to"
                        }
                    } catch (e: Exception) {
                        Log.e("CurrencyConverterAPI", "Error parsing JSON response: ${e.message}")
                        runOnUiThread {
                            resultView.text = "Error: Unable to parse exchange rate."
                        }
                    }
                } ?: run {
                    Log.e("CurrencyConverterAPI", "Response body is null")
                    runOnUiThread {
                        resultView.text = "Error: Unable to fetch exchange rate."
                    }
                }
            }
        })
    }
}