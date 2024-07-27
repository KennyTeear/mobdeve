package com.example.mobdevemachineproject

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExchangeRatesActivity : AppCompatActivity() {

    private lateinit var exchangeRateBox: Box<ExchangeRate>
    private lateinit var recyclerView: RecyclerView
    private lateinit var lastUpdateBox: Box<LastUpdate>
    private lateinit var lastUpdatedTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exchange_rates_activity)

        // Initialize ObjectBox
        val app = application as App
        exchangeRateBox = app.boxStore.boxFor()
        lastUpdateBox = app.boxStore.boxFor()

        // Find RecyclerView and set its layout manager
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        lastUpdatedTextView = findViewById(R.id.lastUpdatedTextView)

        // Fetch exchange rates from ObjectBox
        val exchangeRates = exchangeRateBox.all

        // Log the exchange rates
        exchangeRates.forEach { exchangeRate ->
            Log.d("ExchangeRatesActivity", "Currency: ${exchangeRate.currency}, Rate: ${exchangeRate.rate}")
        }

        // Check if exchangeRates is empty
        if (exchangeRates.isEmpty()) {
            Log.d("ExchangeRatesActivity", "No exchange rates found in ObjectBox")
        } else {
            Log.d("ExchangeRatesActivity", "Found ${exchangeRates.size} exchange rates in ObjectBox")
        }

        // Set up the adapter with the fetched data
        recyclerView.adapter = ExchangeRatesAdapter(exchangeRates)

        val lastUpdated = getLastUpdatedTime()
        updateLastUpdatedTime(lastUpdatedTextView, lastUpdated)




    }
    private fun getLastUpdatedTime(): Long {
        val lastUpdate = lastUpdateBox.all.maxByOrNull { it.timestamp }?.timestamp
        return lastUpdate ?: 0
    }
    private fun updateLastUpdatedTime(textView: TextView, lastUpdated: Long) {
        val sdf = SimpleDateFormat("MMMM d, yyyy h:mma", Locale.getDefault())
        val date = Date(lastUpdated * 1000) // Convert Unix timestamp to milliseconds
        val formattedTime = sdf.format(date)
        textView.text = "Last updated at $formattedTime"
    }


}
