package com.example.myshopapp

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreManager(private val userId: String) {
    private val db = FirebaseFirestore.getInstance()

    fun addToCart(product: Product, selectedSize: String, callback: (Boolean, String?) -> Unit) {
        if (userId.isEmpty()) {
            callback(false, "User not logged in")
            return
        }

        val cartRef = db.collection("Users").document(userId).collection("Cart")
        val query = cartRef.whereEqualTo("productId", product.id).whereEqualTo("selectedSize", selectedSize)

        query.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Item chưa có trong giỏ hàng với size này, thêm mới
                    val newCartItem = CartItem(
                        userId = userId,
                        productId = product.id,
                        productName = product.name,
                        productPrice = product.price,
                        productImageUrl = product.imageUrl,
                        quantity = 1,
                        selectedSize = selectedSize // **Lưu size đã chọn**
                    )
                    cartRef.add(newCartItem)
                        .addOnSuccessListener {
                            callback(true, "${product.name} (Size: $selectedSize) đã được thêm vào giỏ hàng.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreManager", "Lỗi khi thêm sản phẩm vào giỏ hàng", e)
                            callback(false, "Lỗi khi thêm ${product.name} vào giỏ hàng.")
                        }
                } else {
                    // Item đã có trong giỏ hàng với cùng size này, cập nhật số lượng
                    val existingItem = documents.documents[0]
                    val currentQuantity = existingItem.getLong("quantity")?.toInt() ?: 0
                    val newQuantity = currentQuantity + 1
                    cartRef.document(existingItem.id).update("quantity", newQuantity)
                        .addOnSuccessListener {
                            callback(true, "${product.name} (Size: $selectedSize) số lượng đã cập nhật trong giỏ hàng.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreManager", "Lỗi khi cập nhật số lượng sản phẩm trong giỏ hàng", e)
                            callback(false, "Lỗi khi cập nhật số lượng ${product.name} trong giỏ hàng.")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreManager", "Lỗi khi kiểm tra giỏ hàng", e)
                callback(false, "Lỗi khi kiểm tra giỏ hàng.")
            }
    }

    fun getCart(callback: (List<CartItem>?, String?) -> Unit) {
        if (userId.isEmpty()) {
            callback(null, "User not logged in")
            return
        }
        db.collection("Users").document(userId).collection("Cart")
            .get()
            .addOnSuccessListener { result ->
                val cartItems = mutableListOf<CartItem>()
                for (document in result) {
                    val cartItem = document.toObject(CartItem::class.java)
                    cartItem.id = document.id // Gán Document ID vào trường id của CartItem
                    cartItems.add(cartItem)
                }
                callback(cartItems, null)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreManager", "Lỗi khi lấy giỏ hàng", e)
                callback(null, "Lỗi khi tải giỏ hàng.")
            }
    }

}