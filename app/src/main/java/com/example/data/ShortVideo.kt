package com.example.data

import kotlinx.serialization.Serializable

@Serializable
data class ShortVideo(
    val id: Int,
    val username: String,
    val userImg: String,
    val videoUrl: String,
    val posterUrl: String,
    val likes: String,
    val rating: String,
    val shares: String,
    val product: Product,
    val category: String = "Male"
)
