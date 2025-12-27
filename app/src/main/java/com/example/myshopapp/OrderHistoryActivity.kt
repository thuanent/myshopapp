//package com.example.myshopapp
//
//import android.os.Bundle
//import android.view.View
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
//
//class OrderHistoryActivity : AppCompatActivity() {
//
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var emptyMessage: TextView
//    private lateinit var orderAdapter: OrderAdapter
//    private val orderList = mutableListOf<Order>()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_order_history)
//
//        recyclerView = findViewById(R.id.ordersRecyclerView)
//        emptyMessage = findViewById(R.id.emptyMessageTextView)
//
//        orderAdapter = OrderAdapter(orderList)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        recyclerView.adapter = orderAdapter
//
//        fetchOrdersFromFirestore()
//    }
//
//
//private fun fetchOrdersFromFirestore() {
//    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//    val db = FirebaseFirestore.getInstance()
//
//    db.collection("Users")
//        .document(userId)
//        .collection("Orders")
//        // THÊM DÒNG NÀY: Lọc theo các trạng thái bạn muốn hiển thị trong lịch sử mua hàng
//        .whereIn("status", listOf("approved", "shipping", "delivered", "completed", "cancelled", "delivery_failed", "payment_received"))
//        .orderBy("timestamp", Query.Direction.DESCENDING)
//        .get()
//        .addOnSuccessListener { documents ->
//            orderList.clear()
//            for (doc in documents) {
//                val order = doc.toObject(Order::class.java)
//                orderList.add(order)
//            }
//            orderAdapter.notifyDataSetChanged()
//            emptyMessage.visibility = if (orderList.isEmpty()) View.VISIBLE else View.GONE
//        }
//        .addOnFailureListener { e ->
//            Toast.makeText(this, "Lỗi khi tải đơn hàng.", Toast.LENGTH_SHORT).show()
//            emptyMessage.visibility = View.VISIBLE
//        }
//}
//}
package com.example.myshopapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyMessage: TextView
    private lateinit var orderAdapter: OrderAdapter
    private val orderList = mutableListOf<Order>()
    private val db = FirebaseFirestore.getInstance() // Khởi tạo db ở đây

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        recyclerView = findViewById(R.id.ordersRecyclerView)
        emptyMessage = findViewById(R.id.emptyMessageTextView)

        orderAdapter = OrderAdapter(orderList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = orderAdapter

        // Gọi hàm tải dữ liệu khi Activity được tạo
        fetchOrdersFromFirestore()
    }

    // THÊM onResume ĐỂ ĐẢM BẢO DỮ LIỆU ĐƯỢC TẢI LẠI KHI NGƯỜI DÙNG QUAY LẠI MÀN HÌNH NÀY
    override fun onResume() {
        super.onResume()
        fetchOrdersFromFirestore()
    }

    private fun fetchOrdersFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "Người dùng chưa đăng nhập.", Toast.LENGTH_SHORT).show()
            emptyMessage.visibility = View.VISIBLE
            return
        }

        db.collection("Users")
            .document(userId)
            .collection("Orders")
            // CẬP NHẬT DÒNG NÀY: THÊM "pending" VÀO DANH SÁCH TRẠNG THÁI
            .whereIn("status", listOf("pending", "approved", "shipping", "delivered", "completed", "cancelled", "delivery_failed", "payment_received"))
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get() // Vẫn sử dụng get()
            .addOnSuccessListener { documents ->
                orderList.clear()
                for (doc in documents) {
                    val order = doc.toObject(Order::class.java)
                    order?.let {
                        it.orderId = doc.id // Rất quan trọng: Gán ID của document
                        orderList.add(it)
                    }
                }
                orderAdapter.notifyDataSetChanged()
                emptyMessage.visibility = if (orderList.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi tải đơn hàng: ${e.message}", Toast.LENGTH_SHORT).show()
                emptyMessage.visibility = View.VISIBLE
            }
    }
}