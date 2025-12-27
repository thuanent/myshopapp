package com.example.myshopapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.WriteBatch

class AdminOrderProcessingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminOrderAdapter // Tái sử dụng Adapter
    private val orderList = mutableListOf<Order>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_order_processing)

        recyclerView = findViewById(R.id.adminProcessingOrdersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AdminOrderAdapter(
            orderList,
            onApproveClick = { order, snapshot ->

                Log.d("AdminOrderProcessing", "onApproveClick: Click nút Đánh dấu ở màn hình xử lý/giao cho đơn hàng ${order.orderId}, trạng thái hiện tại: ${order.status}")
                when (order.status) {
                    "approved", "processing" -> { // Nếu trạng thái là đã duyệt/đang xử lý
                        // Nhấn nút "Đánh dấu Đang giao" -> chuyển trạng thái sang "shipping"
                        updateOrderStatus(order, snapshot, "shipping")
                    }
                    "shipping" -> { // Nếu trạng thái là đang giao
                        // Nhấn nút "Đánh dấu Đã giao hàng" -> chuyển trạng thái sang "delivered" (hoặc "completed")
                        updateOrderStatus(order, snapshot, "delivered") // <-- Cập nhật trạng thái thành "delivered"
                    }
                    else -> {
                        Log.w("AdminOrderProcessing", "onApproveClick: Click nút Đánh dấu ở trạng thái không mong muốn trên màn hình xử lý/giao: ${order.status}")
                    }
                }
            },
            onItemClick = { order, snapshot ->
                // Logic khi click vào toàn bộ item
                Log.d("AdminOrderProcessing", "onItemClick: Click vào item đơn hàng ${order.orderId}")
            },
            onCancelClick = { order, snapshot ->
                Log.d("AdminOrderProcessing", "onCancelClick: Click nút Đánh dấu giao hàng thất bại cho đơn hàng ${order.orderId}, trạng thái hiện tại: ${order.status}")
                if (order.status == "approved" || order.status == "processing" || order.status == "shipping") {
                    handleDeliveryFailed(order, snapshot)
                } else {
                    Log.w("AdminOrderProcessing", "onCancelClick: Click nút Đánh dấu giao hàng thất bại ở trạng thái không mong muốn: ${order.status}")
                }
            },
            screenType = "processing"
        )
        recyclerView.adapter = adapter
        Log.d("AdminOrderProcessing", "onCreate: AdminOrderProcessingActivity được tạo.")
    }

    private fun loadProcessingOrders() {
        Log.d("AdminOrderProcessing", "loadProcessingOrders: Bắt đầu truy vấn các đơn hàng đang xử lý (approved, processing, shipping).")
        firestore.collection("Orders")
            // Lọc theo các trạng thái cần hiển thị trên màn hình này
            .whereIn("status", listOf("approved", "processing", "shipping"))
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                Log.d("AdminOrderProcessing", "loadProcessingOrders: Truy vấn thành công. Tìm thấy ${result.size()} document.")

                val ordersToDisplay = mutableListOf<Order>()
                val snapshotsToDisplay = mutableListOf<QueryDocumentSnapshot>()

                if (result.isEmpty) {
                    Log.d("AdminOrderProcessing", "loadProcessingOrders: Không tìm thấy đơn hàng nào đang xử lý.")
                }

                for (document in result) {
                    try {
                        val order = document.toObject(Order::class.java)
                        ordersToDisplay.add(order)
                        snapshotsToDisplay.add(document)
                        Log.d("AdminOrderProcessing", "loadProcessingOrders: Đã map đơn hàng ${document.id}. Trạng thái: ${order.status}, TT: ${order.paymentMethod}")
                    } catch (e: Exception) {
                        Log.e("AdminOrderProcessing", "loadProcessingOrders: LỖI KHI MAP DOCUMENT. ID Document: ${document.id}, Dữ liệu: ${document.data}", e)
                    }
                }
                adapter.setOrdersAndSnapshots(ordersToDisplay, snapshotsToDisplay)
                Log.d("AdminOrderProcessing", "loadProcessingOrders: Adapter đã được thông báo cập nhật. Tổng số đơn hàng trong danh sách: ${ordersToDisplay.size}")
            }
            .addOnFailureListener { e ->
                Log.e("AdminOrderProcessing", "loadProcessingOrders: Lỗi tải đơn hàng đang xử lý:", e)
                Toast.makeText(this, "Lỗi tải đơn hàng: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    // Hàm cập nhật trạng thái đơn hàng
// Hàm cập nhật trạng thái đơn hàng
    private fun updateOrderStatus(order: Order, snapshot: QueryDocumentSnapshot, newStatus: String) {
        Log.d("AdminOrderProcessing", "updateOrderStatus: Cập nhật đơn hàng ${order.orderId} từ ${order.status} sang $newStatus.")

        // Tạo một Write Batch để thực hiện nhiều thao tác ghi cùng lúc
        val batch = firestore.batch()

        // 1. Cập nhật trạng thái của đơn hàng trong collection "Orders" cấp gốc
        val globalOrderRef = firestore.collection("Orders").document(order.orderId ?: "")
        batch.update(globalOrderRef, "status", newStatus)
        Log.d("AdminOrderProcessing", "updateOrderStatus: Thêm thao tác cập nhật status '${newStatus}' cho đơn hàng ${order.orderId} (Global) vào batch.")

        // 2. Cập nhật trạng thái của đơn hàng trong collection con dưới Users
        // Đảm bảo order.userId không null
        if (order.userId != null) {
            val userOrderRef = firestore.collection("Users").document(order.userId!!).collection("Orders").document(order.orderId ?: "")
            batch.update(userOrderRef, "status", newStatus)
            Log.d("AdminOrderProcessing", "updateOrderStatus: Thêm thao tác cập nhật status '${newStatus}' cho đơn hàng ${order.orderId} (User ${order.userId}) vào batch.")
        } else {
            Log.e("AdminOrderProcessing", "updateOrderStatus: LỖI: User ID của đơn hàng ${order.orderId} là null. Không thể cập nhật bản sao của người dùng.")
        }

        // Commit batch để thực hiện cả hai thao tác ghi
        batch.commit()
            .addOnSuccessListener {
                Log.d("AdminOrderProcessing", "updateOrderStatus: Đơn hàng ${order.orderId} đã được cập nhật trạng thái thành $newStatus thành công cho cả 2 vị trí.")
                Toast.makeText(this, "Đã cập nhật trạng thái đơn hàng thành '$newStatus'", Toast.LENGTH_SHORT).show()
                loadProcessingOrders() // Tải lại danh sách để cập nhật UI
            }
            .addOnFailureListener { e ->
                Log.e("AdminOrderProcessing", "updateOrderStatus: Lỗi khi commit batch cập nhật trạng thái đơn hàng ${order.orderId}:", e)
                Toast.makeText(this, "Lỗi cập nhật trạng thái: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun handleDeliveryFailed(order: Order, snapshot: QueryDocumentSnapshot) {
        Log.d("AdminOrderProcessing", "handleDeliveryFailed: Xử lý đơn hàng không thành công: ${order.orderId}, trạng thái: ${order.status}")

        val batch = firestore.batch()

        // 1. Cập nhật trạng thái của đơn hàng trong collection "Orders" cấp gốc
        val globalOrderRef = firestore.collection("Orders").document(order.orderId ?: "")
        batch.update(globalOrderRef, "status", "delivery_failed") // Hoặc trạng thái bạn muốn dùng
        Log.d("AdminOrderProcessing", "handleDeliveryFailed: Thêm thao tác cập nhật status 'delivery_failed' cho đơn hàng ${order.orderId} (Global) vào batch.")


        // 2. Cập nhật trạng thái của đơn hàng trong collection con dưới Users
        if (order.userId != null) {
            val userOrderRef = firestore.collection("Users").document(order.userId!!).collection("Orders").document(order.orderId ?: "")
            batch.update(userOrderRef, "status", "delivery_failed")
            Log.d("AdminOrderProcessing", "handleDeliveryFailed: Thêm thao tác cập nhật status 'delivery_failed' cho đơn hàng ${order.orderId} (User ${order.userId}) vào batch.")
        } else {
            Log.e("AdminOrderProcessing", "handleDeliveryFailed: LỖI: User ID của đơn hàng ${order.orderId} là null. Không thể cập nhật bản sao của người dùng (delivery failed).")
        }


        // Logic hoàn tồn kho sản phẩm (giữ nguyên)
        order.items?.let {
            for (item in it) {
                val productRef = firestore.collection("Products").document(item.productId ?: "")
                if (!item.productId.isNullOrEmpty()) {
                    batch.update(productRef, "stock", FieldValue.increment(item.quantity?.toLong() ?: 0L))
                    Log.d("AdminOrderProcessing", "handleDeliveryFailed: Thêm thao tác cộng stock cho sản phẩm ${item.productId} với số lượng ${item.quantity} vào batch.")
                } else {
                    Log.w("AdminOrderProcessing", "handleDeliveryFailed: ProductId rỗng cho item trong đơn hàng ${order.orderId}, không thể hoàn tồn kho.")
                }
            }
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d("AdminOrderProcessing", "handleDeliveryFailed: Batch cập nhật trạng thái và hoàn trả tồn kho thành công cho đơn hàng ${order.orderId} ở cả 2 vị trí.")
                Toast.makeText(this, "Đã đánh dấu đơn hàng ${order.orderId} là không thành công và hoàn trả tồn kho.", Toast.LENGTH_LONG).show()
                loadProcessingOrders() // Tải lại danh sách để cập nhật UI
            }
            .addOnFailureListener { e ->
                Log.e("AdminOrderProcessing", "handleDeliveryFailed: Lỗi khi commit batch xử lý đơn hàng không thành công ${order.orderId}:", e)
                Toast.makeText(this, "Lỗi xử lý đơn hàng không thành công: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
    private fun updateProductStockForApproval(order: Order, onComplete: (Boolean, String) -> Unit) {
        if (order.items.isNullOrEmpty()) {
            Log.d("AdminOrderApproval", "updateProductStockForApproval: Đơn hàng ${order.orderId} không có sản phẩm nào.")
            onComplete(true, "Đơn hàng không có sản phẩm.")
            return
        }

        val batch = firestore.batch()
        var hasStockIssue = false


        for (item in order.items) {
            val productId = item.productId
            val quantity = item.quantity ?: 0

            if (!productId.isNullOrEmpty() && quantity > 0) {
                val productRef = firestore.collection("Products").document(productId)

                // Lấy thông tin sản phẩm hiện tại để kiểm tra tồn kho
                productRef.get()
                    .addOnSuccessListener { productSnapshot ->
                        val currentStock = productSnapshot.toObject(Product::class.java)?.stock ?: 0

                        if (currentStock < quantity) {
                            hasStockIssue = true
                            Toast.makeText(this, "Không đủ tồn kho cho sản phẩm ${item.productName}", Toast.LENGTH_LONG).show()
                            onComplete(false, "Không đủ tồn kho cho sản phẩm ${item.productName}")
                            return@addOnSuccessListener
                        } else {
                            // Trừ số lượng vào batch
                            batch.update(productRef, "stock", FieldValue.increment(-(quantity.toLong())))
                            Log.d("AdminOrderApproval", "updateProductStockForApproval: Thêm thao tác giảm stock cho sản phẩm $productId với số lượng $quantity vào batch.")

                            // THÊM DÒNG NÀY: Tăng số lượng đã bán (soldCount)
                            batch.update(productRef, "soldCount", FieldValue.increment(quantity.toLong()))
                            Log.d("AdminOrderApproval", "updateProductStockForApproval: Thêm thao tác tăng soldCount cho sản phẩm $productId với số lượng $quantity vào batch.")
                        }

                        // ... (phần còn lại của hàm updateProductStockForApproval giữ nguyên)
                        // Chỉ commit batch khi đã duyệt qua tất cả các sản phẩm VÀ KHÔNG CÓ LỖI TỒN KHO
                        if (!hasStockIssue && order.items.last() == item) { // Kiểm tra đây có phải item cuối cùng không
                            batch.commit()
                                .addOnSuccessListener {
                                    Log.d("AdminOrderApproval", "updateProductStockForApproval: Cập nhật tồn kho và soldCount thành công cho đơn hàng ${order.orderId}.")
                                    onComplete(true, "Cập nhật tồn kho thành công.")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("AdminOrderApproval", "updateProductStockForApproval: Lỗi khi commit batch cập nhật tồn kho cho đơn hàng ${order.orderId}:", e)
                                    onComplete(false, "Lỗi khi cập nhật tồn kho: ${e.localizedMessage}")
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("AdminOrderApproval", "updateProductStockForApproval: Lỗi khi lấy thông tin sản phẩm $productId:", e)
                        onComplete(false, "Lỗi khi lấy thông tin sản phẩm: ${e.localizedMessage}")
                        return@addOnFailureListener
                    }
            } else {
                Log.w("AdminOrderApproval", "updateProductStockForApproval: ProductId rỗng hoặc số lượng không hợp lệ cho item trong đơn hàng ${order.orderId}.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("AdminOrderProcessing", "onResume: Activity resume, gọi loadProcessingOrders.")
        loadProcessingOrders()
    }
}