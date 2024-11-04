package com.example.myopsc7312

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class Calculator : AppCompatActivity() {

    private lateinit var display: TextView
    private var firstNumber: Double = 0.0
    private var secondNumber: Double = 0.0
    private var operator: String = ""
    private var isNewOp = true
    private lateinit var userUid: String
    private lateinit var homeNav: FrameLayout
    private lateinit var converterNavigation: FrameLayout



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calculator_layout)

        display = findViewById(R.id.display)

        // Get the intent data
        val message = intent.getStringExtra("userUid")
        userUid = message.toString()

        // Set up number buttons
        val buttonIds = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7,
            R.id.btn8, R.id.btn9
        )
        for (id in buttonIds) {
            findViewById<Button>(id).setOnClickListener { onNumberClick((it as Button).text.toString()) }
        }

        // Set up operator buttons
        findViewById<Button>(R.id.btnAdd).setOnClickListener { onOperatorClick("+") }
        findViewById<Button>(R.id.btnSubtract).setOnClickListener { onOperatorClick("-") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { onOperatorClick("*") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { onOperatorClick("/") }

        // Set up special buttons
        findViewById<Button>(R.id.btnEquals).setOnClickListener { onEqualsClick() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { onClearClick() }

        setupButtonListeners()
    }

    private fun onNumberClick(number: String) {
        if (isNewOp) {
            display.text = ""
            isNewOp = false
        }
        display.append(number)
    }

    private fun onOperatorClick(op: String) {
        firstNumber = display.text.toString().toDoubleOrNull() ?: 0.0
        operator = op
        isNewOp = true
    }

    private fun onEqualsClick() {
        secondNumber = display.text.toString().toDoubleOrNull() ?: 0.0
        val result = when (operator) {
            "+" -> firstNumber + secondNumber
            "-" -> firstNumber - secondNumber
            "*" -> firstNumber * secondNumber
            "/" -> if (secondNumber != 0.0) firstNumber / secondNumber else "Error"
            else -> 0.0
        }
        display.text = result.toString()
        isNewOp = true
    }

    private fun onClearClick() {
        display.text = "0"
        firstNumber = 0.0
        secondNumber = 0.0
        operator = ""
        isNewOp = true
    }

    private fun setupButtonListeners() {
        //footer navigation
        converterNavigation = findViewById(R.id.ConverterNavigator)
        converterNavigation.setOnClickListener {
            val intent = Intent(this, CurrencyConverterAPI::class.java)
            intent.putExtra("userUid", userUid)
            startActivity(intent)
        }

        homeNav = findViewById(R.id.homeNavBtn)
        homeNav.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("userUid", userUid)
            startActivity(intent)
            finish()
        }

    }

}