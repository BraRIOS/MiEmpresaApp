package com.brios.miempresa.data

import androidx.room.Entity

@Entity(tableName = "companies")
data class Company(
    val id: String,
    val name: String,
    val selected: Boolean,
)