package com.example.myshopapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.WriteBatch // Import WriteBatch

class AdminOrderApprovalActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminOrderAdapter
    private val orderList = mutableListOf<Order>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_order_approval)

        recyclerView = findViewById(R.id.adminOrdersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AdminOrderAdapter(
            orderList,
            onApproveClick = { order, snapshot ->
                // Logic xử lý khi admin nhấn nút duyệt/xác nhận
                Log.d("AdminOrderApproval", "onApproveClick: Click nút duyệt/xác nhận cho đơn hàng ${order.orderId}, trạng thái hiện tại: ${order.status}")
                when (order.status) {
                    "pending" -> { // Duyệt đơn COD
                        // Cập nhật trạng thái từ "pending" sang "approved"
                        updateOrderStatus(order, snapshot, "approved")
                    }
                    "pending_payment_verification" -> { // Đơn chuyển khoản, admin xác nhận đã nhận tiền
                        // Cập nhật trạng thái từ "pending_payment_verification" sang "payment_received"
                        updateOrderStatus(order, snapshot, "payment_received")
                    }
                    "payment_received" -> { // Đơn chuyển khoản đã nhận tiền, admin duyệt đơn
                        // Cập nhật trạng thái từ "payment_received" sang "approved"
                        updateOrderStatus(order, snapshot, "approved")
                    }
                    else -> {
                        Log.w("AdminOrderApproval", "onApproveClick: Click nút duyệt/xác nhận ở trạng thái không mong muốn: ${order.status}")
                        Toast.makeText(this, "Không thể duyệt đơn hàng ở trạng thái này: ${order.status}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onItemClick = { order, snapshot ->
                // Logic khi click vào toàn bộ item đơn hàng (nếu có màn hình chi tiết đơn hàng)
                Log.d("AdminOrderApproval", "onItemClick: Click vào item đơn hàng ${order.orderId}")
            },
            onCancelClick = { order, snapshot ->
                Log.d("AdminOrderApproval", "onCancelClick: Click nút không thành công cho đơn hàng ${order.orderId}, trạng thái hiện tại: ${order.status}")
                // Hàm handleDeliveryFailed đã xử lý cập nhật trạng thái và hoàn tồn kho
                handleDeliveryFailed(order, snapshot)
            },
            screenType = "approval"

        )
        recyclerView.adapter = adapter
        Log.d("AdminOrderApproval", "onCreate: AdminOrderApprovalActivity được tạo.")
    }

    override fun onResume() {
        super.onResume()
        Log.d("AdminOrderApproval", "onResume: Activity resume, gọi loadPendingOrders.")
        loadPendingOrders()
    }

    private fun loadPendingOrders() {
        Log.d("AdminOrderApproval", "loadPendingOrders: Bắt đầu truy vấn các đơn hàng đang chờ xử lý.")
        firestore.collection("Orders")
            .whereIn("status", listOf("pending", "pending_payment_verification", "payment_received"))
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING) // Sắp xếp theo thời gian
            .get()
            .addOnSuccessListener { result ->
                Log.d("AdminOrderApproval", "loadPendingOrders: Truy vấn thành công. Tìm thấy ${result.size()} document.")

                val ordersToDisplay = mutableListOf<Order>()
                val snapshotsToDisplay = mutableListOf<QueryDocumentSnapshot>()

                if (result.isEmpty) {
                    Log.d("AdminOrderApproval", "loadPendingOrders: Không tìm thấy đơn hàng nào đang chờ xử lý.")
                }

                for (document in result) {
                    try {
                        val order = document.toObject(Order::class.java)
                        ordersToDisplay.add(order)
                        snapshotsToDisplay.add(document)
                        Log.d("AdminOrderApproval", "loadPendingOrders: Đã map đơn hàng ${document.id}. Trạng thái: ${order.status}, TT: ${order.paymentMethod}")
                    } catch (e: Exception) {
                        Log.e("AdminOrderApproval", "loadPendingOrders: LỖI KHI MAP DOCUMENT. ID Document: ${document.id}, Dữ liệu: ${document.data}", e)
                    }
                }
                adapter.setOrdersAndSnapshots(ordersToDisplay, snapshotsToDisplay)
                Log.d("AdminOrderApproval", "loadPendingOrders: Adapter đã được thông báo cập nhật. Tổng số đơn hàng trong danh sách: ${ordersToDisplay.size}")
            }
            .addOnFailureListener { e ->
                Log.e("AdminOrderApproval", "loadPendingOrders: Lỗi tải đơn hàng:", e)
                Toast.makeText(this, "Lỗi tải đơn hàng: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    // *** ĐÃ SỬA ĐỔI: Hàm cập nhật trạng thái đơn hàng để dùng WriteBatch ***
    private fun updateOrderStatus(order: Order, snapshot: QueryDocumentSnapshot, newStatus: String) {
        Log.d("AdminOrderApproval", "updateOrderStatus: Cập nhật đơn hàng ${order.orderId} từ ${order.status} sang $newStatus.")

        val userId = order.userId
        if (userId == null) {
            Log.e("AdminOrderApproval", "updateOrderStatus: LỖI: User ID của đơn hàng ${order.orderId} là null. Không thể cập nhật bản sao của người dùng.")
            Toast.makeText(this, "Lỗi: Không tìm thấy User ID để cập nhật đơn hàng.", Toast.LENGTH_SHORT).show()
            return
        }

        val batch = firestore.batch()

        // 1. Cập nhật trạng thái của đơn hàng trong collection "Orders" cấp gốc
        val globalOrderRef = firestore.collection("Orders").document(order.orderId ?: "")
        batch.update(globalOrderRef, "status", newStatus)
        Log.d("AdminOrderApproval", "updateOrderStatus: Thêm thao tác cập nhật status '$newStatus' cho đơn hàng ${order.orderId} (Global) vào batch.")

        // 2. Cập nhật trạng thái của đơn hàng trong collection con dưới Users
        val userOrderRef = firestore.collection("Users").document(userId).collection("Orders").document(order.orderId ?: "")
        batch.update(userOrderRef, "status", newStatus)
        Log.d("AdminOrderApproval", "updateOrderStatus: Thêm thao tác cập nhật status '$newStatus' cho đơn hàng ${order.orderId} (User ${userId}) vào batch.")

        // Logic để trừ tồn kho chỉ khi trạng thái mới là "approved" và trạng thái cũ không phải "approved"
        // Điều này đảm bảo tồn kho chỉ bị trừ một lần khi đơn hàng được duyệt lần đầu
        if (newStatus == "approved" && order.status != "approved") {
            updateProductStockForApproval(order, batch) { success, message ->
                if (success) {
                    // Nếu cập nhật tồn kho thành công, commit batch tổng thể
                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("AdminOrderApproval", "updateOrderStatus: Đơn hàng ${order.orderId} đã được duyệt, cập nhật tồn kho và trạng thái thành $newStatus thành công cho cả 2 vị trí.")
                            Toast.makeText(this, "Đã duyệt đơn hàng và cập nhật tồn kho.", Toast.LENGTH_SHORT).show()
                            loadPendingOrders() // Tải lại danh sách
                        }
                        .addOnFailureListener { e ->
                            Log.e("AdminOrderApproval", "updateOrderStatus: Lỗi khi commit batch duyệt đơn hàng ${order.orderId}:", e)
                            Toast.makeText(this, "Lỗi khi duyệt đơn hàng: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Log.e("AdminOrderApproval", "updateOrderStatus: Lỗi khi cập nhật tồn kho cho đơn hàng ${order.orderId}: $message. Không duyệt đơn hàng.")
                    Toast.makeText(this, "Lỗi khi cập nhật tồn kho: $message. Không thể duyệt đơn hàng.", Toast.LENGTH_LONG).show()
                    // Không commit batch nếu có lỗi tồn kho, mọi thao tác trước đó cũng sẽ không được áp dụng
                }
            }
        } else {
            // Nếu không phải trạng thái "approved" hoặc đã là "approved", chỉ cập nhật trạng thái (batch đã có 2 lệnh update status)
            batch.commit()
                .addOnSuccessListener {
                    Log.d("AdminOrderApproval", "updateOrderStatus: Đơn hàng ${order.orderId} đã được cập nhật trạng thái thành $newStatus thành công cho cả 2 vị trí.")
                    Toast.makeText(this, "Đã cập nhật trạng thái đơn hàng thành '$newStatus'", Toast.LENGTH_SHORT).show()
                    loadPendingOrders() // Tải lại danh sách
                }
                .addOnFailureListener { e ->
                    Log.e("AdminOrderApproval", "updateOrderStatus: Lỗi khi commit batch cập nhật trạng thái đơn hàng ${order.orderId}:", e)
                    Toast.makeText(this, "Lỗi cập nhật trạng thái: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }


    // *** SỬA ĐỔI: Hàm hoàn trả tồn kho (updateProductStockForApproval) để nhận một WriteBatch ***
    private fun updateProductStockForApproval(order: Order, batch: WriteBatch, onComplete: (Boolean, String) -> Unit) {
        if (order.items.isNullOrEmpty()) {
            Log.d("AdminOrderApproval", "updateProductStockForApproval: Đơn hàng ${order.orderId} không có sản phẩm nào.")
            onComplete(true, "Đơn hàng không có sản phẩm.")
            return
        }

        val totalItems = order.items.size
        var completedItems = 0
        var hasStockIssue = false

        for (item in order.items) {
            val productId = item.productId
            val quantity = item.quantity ?: 0

            if (!productId.isNullOrEmpty() && quantity > 0) {
                val productRef = firestore.collection("Products").document(productId)

                productRef.get()
                    .addOnSuccessListener { productSnapshot ->
                        if (productSnapshot.exists()) {
                            val currentStock = productSnapshot.toObject(Product::class.java)?.stock ?: 0

                            if (currentStock < quantity) {
                                hasStockIssue = true
                                Log.e("AdminApproval", "updateProductStockForApproval: Không đủ tồn kho cho sản phẩm ${item.productName} (ID: $productId). Yêu cầu: $quantity, Hiện có: $currentStock")
                                // Không hiển thị Toast ở đây để tránh nhiều Toast cùng lúc
                            } else {
                                // Trừ số lượng và tăng soldCount vào batch đã được truyền vào
                                batch.update(productRef, "stock", FieldValue.increment(-(quantity.toLong())))
                                Log.d("AdminApproval", "updateProductStockForApproval: Thêm thao tác giảm stock cho sản phẩm $productId với số lượng $quantity vào batch.")
                                batch.update(productRef, "soldCount", FieldValue.increment(quantity.toLong()))
                                Log.d("AdminApproval", "updateProductStockForApproval: Thêm thao tác tăng soldCount cho sản phẩm $productId với số lượng $quantity vào batch.")
                            }
                        } else {
                            hasStockIssue = true
                            Log.e("AdminApproval", "updateProductStockForApproval: Sản phẩm $productId không tồn tại trong DB.")
                            // Không hiển thị Toast ở đây để tránh nhiều Toast cùng lúc
                        }

                        completedItems++
                        // Khi tất cả các item đã được xử lý (thành công hoặc thất bại)
                        if (completedItems == totalItems) {
                            if (!hasStockIssue) {
                                onComplete(true, "Đã thêm các thao tác tồn kho vào batch.")
                            } else {
                                onComplete(false, "Không đủ tồn kho cho một số sản phẩm hoặc sản phẩm không tồn tại.")
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        hasStockIssue = true
                        Log.e("AdminApproval", "updateProductStockForApproval: Lỗi khi lấy thông tin sản phẩm $productId:", e)
                        // Không hiển thị Toast ở đây
                        completedItems++
                        if (completedItems == totalItems) {
                            onComplete(false, "Lỗi khi lấy thông tin sản phẩm.")
                        }
                    }
            } else {
                Log.w("AdminApproval", "updateProductStockForApproval: ProductId rỗng hoặc số lượng không hợp lệ cho item trong đơn hàng ${order.orderId}.")
                completedItems++
                if (completedItems == totalItems && !hasStockIssue) {
                    onComplete(true, "Không có sản phẩm hợp lệ để cập nhật tồn kho.")
                }
            }
        }

        if (totalItems == 0) {
            onComplete(true, "Đơn hàng không có sản phẩm để cập nhật tồn kho.")
        }
    }


    // *** SỬA ĐỔI: Hàm handleDeliveryFailed để dùng WriteBatch ***
    private fun handleDeliveryFailed(order: Order, snapshot: QueryDocumentSnapshot) {
        Log.d("AdminApproval", "handleDeliveryFailed: Xử lý đơn hàng không thành công: ${order.orderId}, trạng thái: ${order.status}")

        val userId = order.userId
        if (userId == null) {
            Log.e("AdminApproval", "handleDeliveryFailed: LỖI: User ID của đơn hàng ${order.orderId} là null. Không thể cập nhật bản sao của người dùng.")
            Toast.makeText(this, "Lỗi: Không tìm thấy User ID để xử lý đơn hàng.", Toast.LENGTH_SHORT).show()
            return
        }

        val batch = firestore.batch()

        // 1. Cập nhật trạng thái của đơn hàng trong collection "Orders" cấp gốc
        val globalOrderRef = firestore.collection("Orders").document(order.orderId ?: "")
        batch.update(globalOrderRef, "status", "cancelled") // Đặt trạng thái là "cancelled" hoặc "delivery_failed" tùy ý
        Log.d("AdminApproval", "handleDeliveryFailed: Thêm thao tác cập nhật status 'cancelled' cho đơn hàng ${order.orderId} (Global) vào batch.")

        // 2. Cập nhật trạng thái của đơn hàng trong collection con dưới Users
        val userOrderRef = firestore.collection("Users").document(userId).collection("Orders").document(order.orderId ?: "")
        batch.update(userOrderRef, "status", "cancelled")
        Log.d("AdminApproval", "handleDeliveryFailed: Thêm thao tác cập nhật status 'cancelled' cho đơn hàng ${order.orderId} (User ${userId}) vào batch.")

        // Hoàn trả tồn kho (nếu đã trừ khi đặt hàng hoặc duyệt)
        order.items?.let {
            for (item in it) {
                val productId = item.productId
                val quantity = item.quantity ?: 0
                if (!productId.isNullOrEmpty() && quantity > 0) {
                    val productRef = firestore.collection("Products").document(productId)
                    batch.update(productRef, "stock", FieldValue.increment(quantity.toLong()))
                    Log.d("AdminApproval", "handleDeliveryFailed: Thêm thao tác cộng stock cho sản phẩm ${item.productId} với số lượng ${item.quantity} vào batch.")
                    // Trừ soldCount khi hủy đơn hàng
                    batch.update(productRef, "soldCount", FieldValue.increment(-(quantity.toLong())))
                    Log.d("AdminApproval", "handleDeliveryFailed: Thêm thao tác giảm soldCount cho sản phẩm ${item.productId} với số lượng ${item.quantity} vào batch.")
                } else {
                    Log.w("AdminApproval", "handleDeliveryFailed: ProductId rỗng cho item trong đơn hàng ${order.orderId}, không thể hoàn tồn kho.")
                }
            }
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d("AdminApproval", "handleDeliveryFailed: Batch hủy đơn hàng ${order.orderId} thành công và đã hoàn trả tồn kho (nếu có).")
                Toast.makeText(this, "Đã hủy đơn hàng thành công!", Toast.LENGTH_SHORT).show()
                loadPendingOrders() // Tải lại danh sách để cập nhật UI
            }
            .addOnFailureListener { e ->
                Log.e("AdminApproval", "handleDeliveryFailed: Lỗi khi commit batch hủy đơn hàng ${order.orderId}:", e)
                Toast.makeText(this, "Lỗi khi hủy đơn hàng: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}