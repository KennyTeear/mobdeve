package com.example.mobdevemachineproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExchangeRatesAdapter(private val exchangeRates: List<ExchangeRate>) :
    RecyclerView.Adapter<ExchangeRatesAdapter.ExchangeRateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExchangeRateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exchange_rate, parent, false)
        return ExchangeRateViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExchangeRateViewHolder, position: Int) {
        val exchangeRate = exchangeRates[position]
        holder.currencyTextView.text = exchangeRate.currency
        holder.rateTextView.text = exchangeRate.rate.toString()
    }

    override fun getItemCount(): Int = exchangeRates.size

    inner class ExchangeRateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val currencyTextView: TextView = itemView.findViewById(R.id.currencyTextView)
        val rateTextView: TextView = itemView.findViewById(R.id.rateTextView)
    }
}
