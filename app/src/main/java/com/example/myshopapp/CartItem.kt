package com.example.myshopapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(

    var id: String = "",
    val userId: String = "",
    val productId: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0,
    val productImageUrl: String = "",
    var quantity: Int = 1,
    val selectedSize: String = ""
) : Parcelable {
    constructor() : this("", "", "", "", 0.0, "", 1, "")
}