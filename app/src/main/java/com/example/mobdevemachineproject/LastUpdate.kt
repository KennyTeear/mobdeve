package com.example.mobdevemachineproject

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class LastUpdate(
    @Id var id: Long = 0,
    var timestamp: Long
)
