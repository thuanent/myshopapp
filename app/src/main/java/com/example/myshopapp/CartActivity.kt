package com.example.myshopapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList // Cần import ArrayList để sử dụng putParcelableArrayListExtra

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var totalTextView: TextView
    private lateinit var payButton: Button

    private val db = FirebaseFirestore.getInstance()
    private lateinit var userId: String

    private val cartList = mutableListOf<CartItem>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        userId = intent.getStringExtra("USER_ID") ?: ""
        if (userId.isEmpty()) {
            Log.e("CartActivity", "Không nhận được userId, thoát...")
            Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("CartActivity", "UserID hiện tại: $userId")



        recyclerView = findViewById(R.id.cartRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        totalTextView = findViewById(R.id.totalPriceTextView)
        payButton = findViewById(R.id.payButton)

        // Khởi tạo CartAdapter, truyền cartList và các lambda xử lý sự kiện
        cartAdapter = CartAdapter(cartList,
            onItemRemoved = { item -> removeItemFromCart(item) },
            onQuantityChanged = { item -> updateItemQuantity(item) }
        )
        recyclerView.adapter = cartAdapter

        // Tải dữ liệu giỏ hàng khi Activity được tạo
        loadCartItems()

        // Thiết lập OnClickListener cho nút Thanh toán
        payButton.setOnClickListener {
            // Tính tổng tiền từ danh sách cartList
            val totalAmount = cartList.sumOf { it.productPrice * it.quantity }


            val intent = Intent(this, PaymentActivity::class.java) // Đảm bảo PaymentActivity tồn tại và được khai báo trong Manifest

            // Truyền dữ liệu cần thiết qua Intent
            intent.putExtra("TOTAL_AMOUNT", totalAmount)
            intent.putParcelableArrayListExtra("CART_ITEMS", ArrayList(cartList)) // cartList cần chứa các đối tượng Parcelable (CartItem đã là Parcelable)

            startActivity(intent)
        }
    }

    private fun loadCartItems() {
        db.collection("Users").document(userId).collection("Cart")
            .get()
            .addOnSuccessListener { result ->
                cartList.clear()
                for (document in result) {
                    val cartItem = document.toObject(CartItem::class.java)
                    cartItem.id = document.id
                    cartList.add(cartItem)
                }
                cartAdapter.notifyDataSetChanged()
                updateTotalAmount()
            }
            .addOnFailureListener { e ->
                Log.e("CartActivity", "Lỗi khi tải giỏ hàng", e)
                Toast.makeText(this, "Lỗi khi tải giỏ hàng.", Toast.LENGTH_SHORT).show()
            }
    }

    // Hiển thị số tiền
    private fun updateTotalAmount() {
        val total = cartList.sumOf { it.productPrice * it.quantity }
        totalTextView.text = "Tổng cộng: ${total} VND" // Format hiển thị tổng tiền tùy ý
    }


    private fun removeItemFromCart(item: CartItem) {
        Toast.makeText(this, "Xóa ${item.productName} khỏi giỏ hàng...", Toast.LENGTH_SHORT).show()
        db.collection("Users").document(userId).collection("Cart").document(item.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "${item.productName} đã xóa khỏi giỏ hàng.", Toast.LENGTH_SHORT).show()
                // Cập nhật danh sách cục bộ và thông báo Adapter
                cartList.remove(item)
                cartAdapter.notifyDataSetChanged()
                updateTotalAmount()
            }
            .addOnFailureListener { e ->
                Log.e("CartActivity", "Lỗi khi xóa item giỏ hàng", e)
                Toast.makeText(this, "Lỗi xóa ${item.productName}.", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateItemQuantity(item: CartItem) {
        // Bước 1: Lấy thông tin sản phẩm từ Firestore để kiểm tra tồn kho
        db.collection("Products").document(item.productId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val product = document.toObject(Product::class.java)
                    if (product != null) {
                        if (item.quantity > product.stock) {
                            // Số lượng yêu cầu vượt quá tồn kho
                            Toast.makeText(this, "${product.name} chỉ còn ${product.stock} sản phẩm trong kho.", Toast.LENGTH_SHORT).show()
                            loadCartItems()

                        } else {
                            db.collection("Users").document(userId).collection("Cart").document(item.id)
                                .update("quantity", item.quantity) // item.quantity đã được cập nhật bởi Adapter
                                .addOnSuccessListener {
                                    Log.d("CartActivity", "Đã cập nhật số lượng cho ${item.productName} trong Firestore.")
                                    updateTotalAmount()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("CartActivity", "Lỗi khi cập nhật số lượng trong Firestore", e)
                                    Toast.makeText(this, "Lỗi cập nhật số lượng ${item.productName}.", Toast.LENGTH_SHORT).show()
                                    loadCartItems()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Sản phẩm này không còn tồn tại.", Toast.LENGTH_SHORT).show()
                        removeItemFromCart(item)
                    }
                } else {
                    Toast.makeText(this, "Sản phẩm này không còn tồn tại.", Toast.LENGTH_SHORT).show()
                    removeItemFromCart(item)
                }
            }
            .addOnFailureListener { e ->
                Log.e("CartActivity", "Lỗi khi lấy thông tin sản phẩm để kiểm tra tồn kho", e)
                Toast.makeText(this, "Lỗi kiểm tra tồn kho cho ${item.productName}.", Toast.LENGTH_SHORT).show()
                loadCartItems()
            }
    }



}