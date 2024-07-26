package com.example.mobdevemachineproject

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.objectbox.Box
import io.objectbox.kotlin.boxFor

class ExchangeRatesActivity : AppCompatActivity() {

    private lateinit var exchangeRateBox: Box<ExchangeRate>
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exchange_rates_activity)

        // Initialize ObjectBox
        val app = application as App
        exchangeRateBox = app.boxStore.boxFor()

        // Find RecyclerView and set its layout manager
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

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
    }
}
