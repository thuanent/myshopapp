package com.example.myshopapp

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val id: String = "",
    val email: String = "",
    val role: String = "buyer",
    val fullName: String = "",
    val phoneNumber: String = "",
    val address: String = ""
)