package com.example.myshopapp

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Product(
    @DocumentId var id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val stock: Int = 0,
    val description: String = "",
    val sizes: List<String> = emptyList(),
    @ServerTimestamp
    val timestamp: Date? = null,

    val soldCount: Long = 0 // Mặc định là 0


) {
    constructor() : this("", "", 0.0, "", 0, "", emptyList(), null, 0L)
}