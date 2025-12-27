package com.example.myshopapp

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date
// thống kê chi tiết đơn hàng
class OrderDetailsStatisticsActivity : AppCompatActivity() {

    private lateinit var totalRevenueTextView: TextView
    private lateinit var soldProductsRecyclerView: RecyclerView
    private lateinit var soldProductAdapter: SoldProductDetailsAdapter

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details_statistics)

        totalRevenueTextView = findViewById(R.id.totalRevenueTextView)
        soldProductsRecyclerView = findViewById(R.id.soldProductsRecyclerView)
        soldProductsRecyclerView.layoutManager = LinearLayoutManager(this)
        soldProductAdapter = SoldProductDetailsAdapter(emptyList())
        soldProductsRecyclerView.adapter = soldProductAdapter

        loadOrderDetailsStatistics()
    }

    private fun loadOrderDetailsStatistics() {
        db.collectionGroup("Orders")
            .whereEqualTo("status", "delivered")
            .get()
            .addOnSuccessListener { result ->
                var totalRevenue = 0.0
                val soldProductsMap = mutableMapOf<String?, SoldProductDetailsItem>()

                for (document in result) {
                    try {
                        val order = document.toObject(Order::class.java)
                        totalRevenue += order.totalAmount ?: 0.0
                        order.items?.forEach { cartItem ->
                            val productName = cartItem.productName
                            val quantity = cartItem.quantity ?: 0
                            val productPrice = cartItem.productPrice ?: 0.0
                            val totalProductRevenue = quantity * productPrice

                            if (soldProductsMap.containsKey(productName)) {
                                val existingItem = soldProductsMap[productName]!!
                                existingItem.quantitySold += quantity
                                existingItem.totalProductRevenue += totalProductRevenue
                            } else {
                                soldProductsMap[productName] = SoldProductDetailsItem(productName, quantity, totalProductRevenue)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("OrderDetailsStatsActivity", "Lỗi khi ánh xạ document đơn hàng: ${document.id}", e)
                    }
                }

                totalRevenueTextView.text = "Tổng doanh thu: ${String.format("%.0f", totalRevenue)} VND"

                val soldProductList = soldProductsMap.values.toList()
                soldProductAdapter.updateData(soldProductList)
                Log.d("OrderDetailsStatsActivity", "Thống kê chi tiết đơn hàng - Tổng doanh thu: $totalRevenue, Sản phẩm đã bán: $soldProductsMap")

            }
            .addOnFailureListener { e ->
                Log.e("OrderDetailsStatsActivity", "Lỗi khi tải dữ liệu thống kê chi tiết đơn hàng.", e)
                totalRevenueTextView.text = "Tổng doanh thu: Lỗi tải"
                Toast.makeText(this, "Lỗi khi tải thống kê chi tiết đơn hàng.", Toast.LENGTH_SHORT).show()
            }
    }
}