package com.example.myshopapp

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import kotlinx.android.parcel.RawValue

data class Order(
    var orderId: String = "",

    var userId: String = "",

    val fullName: String = "",

    val address: String = "",

    @PropertyName("phoneNumber")
    val phoneNumber: String = "",

    val paymentMethod: String = "bank_transfer",

    val status: String = "pending",

    @PropertyName("totalAmount")
    val totalAmount: Double = 0.0,

    val items: List<CartItem> = emptyList(),

    val timestamp: Timestamp = Timestamp.now(),
    val paymentInfo: @RawValue Map<String, Any>? = null

) {
}