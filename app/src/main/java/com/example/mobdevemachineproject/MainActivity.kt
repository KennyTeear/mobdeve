package com.example.mobdevemachineproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
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
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var firstConversion: EditText
    private lateinit var secondConversion: EditText
    private lateinit var spinnerFromCurrency: Spinner
    private lateinit var spinnerToCurrency: Spinner
    private lateinit var swapButton: ImageButton
    private lateinit var lastUpdatedTextView: TextView
    private lateinit var exchangeRatesButton: Button

    private lateinit var exchangeRateBox: Box<ExchangeRate>
    private lateinit var lastUpdateBox: Box<LastUpdate>
    private lateinit var app: App

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var baseCurrency: String
    private lateinit var convertedToCurrency: String

    var conversionRate = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        sharedPreferences = getSharedPreferences("com.example.mobdevemachineproject", Context.MODE_PRIVATE)

        baseCurrency = sharedPreferences.getString("currency1", "USD").toString()
        convertedToCurrency = sharedPreferences.getString("currency2", "EUR").toString()

        // Find the TextView by its ID
        lastUpdatedTextView = findViewById(R.id.textView4)

        // Initialize EditText views
        firstConversion = findViewById(R.id.first_Conversion)
        secondConversion = findViewById(R.id.second_Conversion)

        // Find the spinners and button by their IDs
        spinnerFromCurrency = findViewById(R.id.spinnerFromCurrency)
        spinnerToCurrency = findViewById(R.id.spinnerToCurrency)
        swapButton = findViewById(R.id.imageButton)

        app = application as App
        exchangeRateBox = app.boxStore.boxFor()
        lastUpdateBox = app.boxStore.boxFor()
        //-----------------------------------------------------------

        // todo: please remove later or soon, hard coded network
        val haveNetwork = true
        if (haveNetwork) {
            fetchExchangeRates()
        }
        val lastUpdated = getLastUpdatedTime()
        updateLastUpdatedTime(lastUpdatedTextView, lastUpdated)


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
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                baseCurrency = parent?.getItemAtPosition(position).toString()
                sharedPreferences.edit().putString("currency1", baseCurrency).apply()
                getApiResult()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerToCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                convertedToCurrency = parent?.getItemAtPosition(position).toString()
                sharedPreferences.edit().putString("currency2", convertedToCurrency).apply()
                getApiResult()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
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

        exchangeRatesButton = findViewById(R.id.button)
        exchangeRatesButton.setOnClickListener {
            val intent = Intent(this, ExchangeRatesActivity::class.java)
            startActivity(intent)
        }

        spinnerFromCurrency.setSelection((spinnerFromCurrency.adapter as ArrayAdapter<String>).getPosition(baseCurrency.toString()))
        spinnerToCurrency.setSelection((spinnerToCurrency.adapter as ArrayAdapter<String>).getPosition(convertedToCurrency.toString()))
    }
// -----------------------------------------------------------------------------------------------------------


    private fun swapCurrencies() {
        // Swap the currencies
        val temp = baseCurrency
        baseCurrency = convertedToCurrency
        convertedToCurrency = temp

        // Update the spinners to reflect the swapped currencies
        spinnerFromCurrency.setSelection(
            (spinnerFromCurrency.adapter as ArrayAdapter<String>).getPosition(
                baseCurrency
            )
        )
        spinnerToCurrency.setSelection(
            (spinnerToCurrency.adapter as ArrayAdapter<String>).getPosition(
                convertedToCurrency
            )
        )
        // Trigger the conversion
        getApiResult()
    }

    private fun getApiResult() {
        if (firstConversion.text.isNotEmpty() && firstConversion.text.isNotBlank()) {
            val apiUrl = "https://api.exchangerate-api.com/v4/latest/$baseCurrency"

            if (baseCurrency == convertedToCurrency) {
                Toast.makeText(
                    applicationContext,
                    "Please pick a different currency to convert",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val apiResult = URL(apiUrl).readText()
                        Log.d("API Result", apiResult) // Log the API result
                        val jsonObject = JSONObject(apiResult)
                        conversionRate =
                            jsonObject.getJSONObject("rates").getDouble(convertedToCurrency)
                                .toFloat()
                        val lastUpdated = jsonObject.getString("time_last_updated")

                        Log.d("Main", "$conversionRate")
                        Log.d("Main", apiResult)

                        withContext(Dispatchers.Main) {
                            if (firstConversion.text.isNotEmpty() && firstConversion.text.isNotBlank()) {
                                val text = ((firstConversion.text.toString()
                                    .toFloat()) * conversionRate).toString()
                                secondConversion.setText(text)
                            } else {
                                secondConversion.setText("")
                            }
//                            updateLastUpdatedTime(lastUpdatedTextView, lastUpdated)
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

    private fun updateLastUpdatedTime(textView: TextView, lastUpdated: Long) {
        val sdf = SimpleDateFormat("MMMM d, yyyy h:mma", Locale.getDefault())
        val date = Date(lastUpdated * 1000) // Convert Unix timestamp to milliseconds
        val formattedTime = sdf.format(date)
        textView.text = "Last updated at $formattedTime"
    }

    private fun getLastUpdatedTime(): Long {
        val lastUpdate = lastUpdateBox.all.maxByOrNull { it.timestamp }?.timestamp
        return lastUpdate ?: 0
    }


    private fun fetchExchangeRates() {
        val apiUrl = "https://open.er-api.com/v6/latest/USD"
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val apiResult = URL(apiUrl).readText()
                Log.d("API Result", apiResult) // Log the API result
                val jsonObject = JSONObject(apiResult)
                val allRates = jsonObject.getJSONObject("rates")
                val lastUpdated = jsonObject.getLong("time_last_update_unix")

                Log.d("apireturn", "$allRates")

                val exchangeRates = mutableListOf<ExchangeRate>()
                allRates.keys().forEach { key ->
                    val rate = allRates.getDouble(key)
                    exchangeRates.add(ExchangeRate(currency = key, rate = rate))
                }
                withContext(Dispatchers.Main) {
                    // Update ObjectBox with new exchange rates
                    exchangeRateBox.removeAll()
                    exchangeRateBox.put(exchangeRates)

                    // Update last updated timestamp
                    val lastUpdate = LastUpdate(0, lastUpdated)
                    lastUpdateBox.removeAll()
                    lastUpdateBox.put(lastUpdate)

                    Toast.makeText(
                        this@MainActivity,
                        "Exchange rates updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Log the saved exchange rates
                    val savedRates = exchangeRateBox.all
                    savedRates.forEach { exchangeRate ->
                        println("Saved Currency: ${exchangeRate.currency}, Rate: ${exchangeRate.rate}")
                    }
                }
            }
            catch (e: Exception) {
                Log.e("Main", "$e")
            }
        }
    }

















}




