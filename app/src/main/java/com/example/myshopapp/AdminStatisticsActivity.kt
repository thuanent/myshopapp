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
import java.text.SimpleDateFormat

class AdminStatisticsActivity : AppCompatActivity() {

    private lateinit var totalProductsTextView: TextView
    private lateinit var lowStockProductsRecyclerView: RecyclerView
    private lateinit var lowStockProductAdapter: LowStockProductAdapter

//    private lateinit var totalSoldProductsTextView: TextView
//    private lateinit var totalRevenueTextView: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_statistics)

        totalProductsTextView = findViewById(R.id.totalProductsTextView)
        lowStockProductsRecyclerView = findViewById(R.id.lowStockProductsRecyclerView)
        lowStockProductsRecyclerView.layoutManager = LinearLayoutManager(this)
        lowStockProductAdapter = LowStockProductAdapter(emptyList())
        lowStockProductsRecyclerView.adapter = lowStockProductAdapter

//        totalSoldProductsTextView = findViewById(R.id.totalSoldProductsTextView)
//        totalRevenueTextView = findViewById(R.id.totalRevenueTextView)

        loadProductStatistics()
//        loadOrderStatisticsForToday()
    }

    private fun loadProductStatistics() {
        db.collection("Products").get()
            .addOnSuccessListener { result ->
                val productList = mutableListOf<Product>()
                for (document in result) {
                    try {
                        val product = document.toObject(Product::class.java)
                        product.id = document.id
                        productList.add(product)
                    } catch (e: Exception) {
                        Log.e("AdminStatsActivity", "Failed to map document ${document.id} to Product.", e)
                    }
                }

                val totalProducts = productList.size
                val lowStockProducts = productList.filter { it.stock < 10 }

                totalProductsTextView.text = "Tổng số sản phẩm: $totalProducts"
                lowStockProductAdapter.updateData(lowStockProducts)

                Log.d("AdminStatsActivity", "Đã tải ${totalProducts} sản phẩm. Có ${lowStockProducts.size} sản phẩm sắp hết hàng.")
            }
            .addOnFailureListener { e ->
                Log.e("AdminStatsActivity", "Lỗi khi tải dữ liệu thống kê sản phẩm.", e)
                totalProductsTextView.text = "Tổng số sản phẩm: Lỗi tải dữ liệu"
                Toast.makeText(this, "Lỗi khi tải thống kê sản phẩm.", Toast.LENGTH_SHORT).show()
            }
    }

//    private fun loadOrderStatisticsForToday() {
//        val calendar = Calendar.getInstance()
//        calendar.set(Calendar.HOUR_OF_DAY, 0)
//        calendar.set(Calendar.MINUTE, 0)
//        calendar.set(Calendar.SECOND, 0)
//        calendar.set(Calendar.MILLISECOND, 0)
//        val startOfDay = calendar.time
//
//        calendar.add(Calendar.DAY_OF_YEAR, 1)
//        val endOfDay = calendar.time
//
//        db.collectionGroup("Orders") // Truy vấn tất cả các đơn hàng
//            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
//            .whereLessThan("timestamp", endOfDay)
//            .whereEqualTo("status", "delivered") // Lọc theo trạng thái "delivered" hoặc trạng thái hoàn thành của bạn
//            .get()
//            .addOnSuccessListener { result ->
//                var totalSold = 0
//                var totalRevenue = 0.0
//                for (document in result) {
//                    try {
//                        val order = document.toObject(Order::class.java)
//                        if (order.items != null) {
//                            for (item in order.items) {
//                                totalSold += item.quantity ?: 0
//                            }
//                        }
//                        totalRevenue += order.totalAmount ?: 0.0
//                    } catch (e: Exception) {
//                        Log.e("AdminStatsActivity", "Lỗi khi ánh xạ document đơn hàng: ${document.id}", e)
//                    }
//                }
//                totalSoldProductsTextView.text = "Tổng sản phẩm đã bán hôm nay: $totalSold"
//                totalRevenueTextView.text = "Tổng doanh thu hôm nay: ${String.format("%.0f", totalRevenue)} VND"
//                Log.d("AdminStatsActivity", "Thống kê đơn hàng hôm nay - Tổng sản phẩm đã bán: $totalSold, Tổng doanh thu: $totalRevenue")
//            }
//            .addOnFailureListener { e ->
//                Log.e("AdminStatsActivity", "Lỗi khi tải dữ liệu thống kê đơn hàng.", e)
//                totalSoldProductsTextView.text = "Tổng sản phẩm đã bán hôm nay: Lỗi tải"
//                totalRevenueTextView.text = "Tổng doanh thu hôm nay: Lỗi tải"
//                Toast.makeText(this, "Lỗi khi tải thống kê đơn hàng.", Toast.LENGTH_SHORT).show()
//            }
//    }
}