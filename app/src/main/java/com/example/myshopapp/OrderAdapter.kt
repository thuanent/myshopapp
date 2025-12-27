package com.example.myshopapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

// Adapter để hiển thị danh sách đơn hàng trong RecyclerView
class OrderAdapter(private val orders: MutableList<Order>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    // ViewHolder ánh xạ các View trong item_order.xml
    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderIdTextView: TextView = itemView.findViewById(R.id.orderIdTextView)
        val orderDateTextView: TextView = itemView.findViewById(R.id.orderDateTextView)
        val orderTotalTextView: TextView = itemView.findViewById(R.id.orderTotalTextView)
        val orderStatusTextView: TextView = itemView.findViewById(R.id.orderStatusTextView)

        val fullNameTextView: TextView = itemView.findViewById(R.id.fullNameTextView)
        val addressTextView: TextView = itemView.findViewById(R.id.addressTextView)
        val phoneNumberTextView: TextView = itemView.findViewById(R.id.phoneNumberTextView)


        fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
            val date = timestamp.toDate()
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return formatter.format(date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        if (position < 0 || position >= orders.size) {
            Log.e("OrderAdapter", "onBindViewHolder: Vị trí không hợp lệ: $position")
            return // Thoát nếu vị trí không hợp lệ
        }

        val order = orders[position]

        // Kiểm tra đối tượng order có null không
        if (order != null) {
            Log.d("OrderAdapter", "--- Đang bind đơn hàng tại vị trí: $position ---")
            Log.d("OrderAdapter", "Bind ID: ${order.orderId}") // Log full ID
            Log.d("OrderAdapter", "Bind UserID: ${order.userId}")
            Log.d("OrderAdapter", "Bind totalAmount: ${order.totalAmount}")
            Log.d("OrderAdapter", "Bind status: ${order.status}")
            Log.d("OrderAdapter", "Bind paymentMethod: ${order.paymentMethod}")
            Log.d("OrderAdapter", "Bind address: ${order.address}")
            Log.d("OrderAdapter", "Bind fullName: ${order.fullName}")
            Log.d("OrderAdapter", "Bind phoneNumber: ${order.phoneNumber}")
            Log.d("OrderAdapter", "Bind timestamp: ${holder.formatTimestamp(order.timestamp)}")
            Log.d("OrderAdapter", "Bind items count: ${order.items.size}")


            // Gán dữ liệu cho các TextView
            holder.orderIdTextView.text = "Mã đơn hàng: #${order.orderId.take(8)}" // Hiển thị 8 ký tự đầu
            holder.orderDateTextView.text = "Ngày đặt: ${holder.formatTimestamp(order.timestamp)}"
            // Định dạng tổng tiền thành chuỗi có dấu phẩy, không có số thập phân
            holder.orderTotalTextView.text = "Tổng tiền: ${String.format("%,.0f", order.totalAmount)} VND"
            holder.orderStatusTextView.text = "Trạng thái: ${order.status}"

            // Gán dữ liệu cho các TextView thông tin khách hàng
            holder.fullNameTextView.text = "Họ tên: ${order.fullName}"
            holder.addressTextView.text = "Địa chỉ: ${order.address}"
            holder.phoneNumberTextView.text = "Điện thoại: ${order.phoneNumber}"


        } else {
            Log.e("OrderAdapter", "onBindViewHolder: Đối tượng Order bị null tại vị trí $position")
            holder.orderIdTextView.text = "Lỗi tải"
            holder.orderDateTextView.text = ""
            holder.orderTotalTextView.text = ""
            holder.orderStatusTextView.text = ""
            holder.fullNameTextView.text = ""
            holder.addressTextView.text = ""
            holder.phoneNumberTextView.text = ""
        }
    }

    // Trả về tổng số mục trong danh sách
    override fun getItemCount(): Int {
        return orders.size
    }
    fun updateData(newOrders: List<Order>) {
        Log.d("OrderAdapter", "updateData được gọi. Kích thước danh sách nhận được: ${newOrders.size}")
        orders.clear() // Xóa dữ liệu cũ
        orders.addAll(newOrders) // Thêm dữ liệu mới
        notifyDataSetChanged() // Thông báo cho RecyclerView cập nhật giao diện
        Log.d("OrderAdapter", "Adapter internal list size after update: ${orders.size}")
    }
}