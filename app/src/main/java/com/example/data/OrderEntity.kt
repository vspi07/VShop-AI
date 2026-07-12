package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val date: String,
    val status: String,
    val itemsCount: Int,
    val total: Double,
    val imageUrl: String,
    val isHistory: Boolean
)
