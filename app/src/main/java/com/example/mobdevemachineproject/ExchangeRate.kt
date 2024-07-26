package com.example.mobdevemachineproject
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class ExchangeRate (
    @Id var id: Long = 0,
    var currency: String,
    var rate: Double,
)