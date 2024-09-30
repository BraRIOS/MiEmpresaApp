package com.brios.miempresa.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey
    val id: String,
    val name: String,
    val selected: Boolean,
)