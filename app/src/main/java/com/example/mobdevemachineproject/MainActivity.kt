package com.example.mobdevemachineproject

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var firstConversion: EditText
    private lateinit var secondConversion: EditText
    private lateinit var spinnerFromCurrency: Spinner
    private lateinit var spinnerToCurrency: Spinner
    private lateinit var swapButton: ImageButton
    private lateinit var lastUpdatedTextView: TextView

    var baseCurrency = "USD"
    var convertedToCurrency = "EUR"
    var conversionRate = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Find the TextView by its ID
        lastUpdatedTextView = findViewById(R.id.textView4)

        // Initialize EditText views
        firstConversion = findViewById(R.id.first_Conversion)
        secondConversion = findViewById(R.id.second_Conversion)

        // Find the spinners and button by their IDs
        spinnerFromCurrency = findViewById(R.id.spinnerFromCurrency)
        spinnerToCurrency = findViewById(R.id.spinnerToCurrency)
        swapButton = findViewById(R.id.imageButton)

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.currency_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinners
            spinnerFromCurrency.adapter = adapter
            spinnerToCurrency.adapter = adapter
        }

        // Set listeners for spinners to update currencies and fetch new rates
        spinnerFromCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                baseCurrency = parent?.getItemAtPosition(position).toString()
                getApiResult()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        spinnerToCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                convertedToCurrency = parent?.getItemAtPosition(position).toString()
                getApiResult()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Add text change listener to the first conversion EditText
        firstConversion.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                getApiResult()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Set click listener for the swap button
        swapButton.setOnClickListener {
            swapCurrencies()
        }
    }

    private fun swapCurrencies() {
        // Swap the currencies
        val temp = baseCurrency
        baseCurrency = convertedToCurrency
        convertedToCurrency = temp

        // Update the spinners to reflect the swapped currencies
        spinnerFromCurrency.setSelection((spinnerFromCurrency.adapter as ArrayAdapter<String>).getPosition(baseCurrency))
        spinnerToCurrency.setSelection((spinnerToCurrency.adapter as ArrayAdapter<String>).getPosition(convertedToCurrency))

        // Trigger the conversion
        getApiResult()
    }

    private fun getApiResult() {
        if (firstConversion.text.isNotEmpty() && firstConversion.text.isNotBlank()) {
            val apiUrl = "https://api.exchangerate-api.com/v4/latest/$baseCurrency"

            if (baseCurrency == convertedToCurrency) {
                Toast.makeText(applicationContext, "Please pick a different currency to convert", Toast.LENGTH_SHORT).show()
            } else {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val apiResult = URL(apiUrl).readText()
                        Log.d("API Result", apiResult) // Log the API result
                        val jsonObject = JSONObject(apiResult)
                        conversionRate = jsonObject.getJSONObject("rates").getDouble(convertedToCurrency).toFloat()
                        val lastUpdated = jsonObject.getString("time_last_updated")

                        Log.d("Main", "$conversionRate")
                        Log.d("Main", apiResult)

                        withContext(Dispatchers.Main) {
                            if (firstConversion.text.isNotEmpty() && firstConversion.text.isNotBlank()) {
                                val text = ((firstConversion.text.toString().toFloat()) * conversionRate).toString()
                                secondConversion.setText(text)
                            } else {
                                secondConversion.setText("")
                            }
                            updateLastUpdatedTime(lastUpdatedTextView, lastUpdated)
                        }
                    } catch (e: Exception) {
                        Log.e("Main", "$e")
                    }
                }
            }
        } else {
            secondConversion.setText("")
        }
    }

    private fun updateLastUpdatedTime(textView: TextView, lastUpdated: String) {
        val sdf = SimpleDateFormat("h:mma", Locale.getDefault())
        val date = Date(lastUpdated.toLong() * 1000)
        val formattedTime = sdf.format(date)
        textView.text = "Last updated at $formattedTime"
    }
}