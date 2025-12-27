package com.example.myshopapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.QueryDocumentSnapshot

class AdminOrderAdapter(
    private val orderList: MutableList<Order>,
    private val onApproveClick: (Order, QueryDocumentSnapshot) -> Unit, // Duyệt / Xác nhận nhận tiền / Đánh dấu đang giao / Đánh dấu đã giao
    private val onItemClick: (Order, QueryDocumentSnapshot) -> Unit,
    private val onCancelClick: (Order, QueryDocumentSnapshot) -> Unit,
    private val screenType: String
) : RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder>() {

    private val orderSnapshots = mutableListOf<QueryDocumentSnapshot>()

    fun setOrdersAndSnapshots(orders: List<Order>, snapshots: List<QueryDocumentSnapshot>) {
        orderList.clear()
        orderList.addAll(orders)
        orderSnapshots.clear()
        orderSnapshots.addAll(snapshots)
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val adminOrderNameTextView: TextView = itemView.findViewById(R.id.adminOrderNameTextView)
        val adminOrderPhoneTextView: TextView = itemView.findViewById(R.id.adminOrderPhoneTextView)
        val adminOrderAddressTextView: TextView = itemView.findViewById(R.id.adminOrderAddressTextView)
        val llProductItemsContainer: LinearLayout = itemView.findViewById(R.id.llProductItemsContainer)
        val adminOrderTotalTextView: TextView = itemView.findViewById(R.id.adminOrderTotalTextView)
        val adminOrderStatusTextView: TextView = itemView.findViewById(R.id.adminOrderStatusTextView)
        val approveOrderButton: Button = itemView.findViewById(R.id.approveOrderButton)
        val markAsFailedButton: Button = itemView.findViewById(R.id.markAsFailedButton)

        init {
            approveOrderButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onApproveClick.invoke(orderList[position], orderSnapshots[position])
                }
            }

            markAsFailedButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCancelClick.invoke(orderList[position], orderSnapshots[position])
                }
            }

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick.invoke(orderList[position], orderSnapshots[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_order, parent, false)
        return OrderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val currentOrder = orderList[position]

        holder.adminOrderNameTextView.text = "Họ tên: ${currentOrder.fullName}"
        holder.adminOrderPhoneTextView.text = "SĐT: ${currentOrder.phoneNumber}"
        holder.adminOrderAddressTextView.text = "Địa chỉ: ${currentOrder.address}"

        holder.llProductItemsContainer.removeAllViews()
        currentOrder.items?.let {
            for (item in it) {
                val productTextView = TextView(holder.itemView.context)
                productTextView.text = "- ${item.productName ?: "Không rõ"} (x${item.quantity ?: 1}) - ${String.format("%.0f", item.productPrice ?: 0.0)}đ/SP"
                productTextView.textSize = 14f
                productTextView.setPadding(0, 4, 0, 4)
                holder.llProductItemsContainer.addView(productTextView)
            }
        }

        holder.adminOrderTotalTextView.text = "Tổng tiền: ${String.format("%.0f", currentOrder.totalAmount ?: 0.0)}đ"
        holder.adminOrderStatusTextView.text = "Trạng thái: ${currentOrder.status ?: "Không rõ"} (TT: ${if(currentOrder.paymentMethod == "bank_transfer") "Chuyển khoản" else "COD"})"


        // **CẬP NHẬT Logic hiển thị các nút dựa vào screenType và trạng thái**
        when (screenType) {
            "approval" -> {
                // Giữ nguyên logic hiển thị nút cho màn hình duyệt đơn (đã sửa ở bình luận trước)
                when (currentOrder.status) {
                    "pending" -> {
                        holder.approveOrderButton.visibility = View.VISIBLE
                        holder.approveOrderButton.text = "Duyệt đơn"
                        holder.markAsFailedButton.visibility = View.GONE
                    }
                    "pending_payment_verification" -> {
                        holder.approveOrderButton.visibility = View.VISIBLE
                        holder.approveOrderButton.text = "Xác nhận đã nhận tiền"
                        holder.markAsFailedButton.visibility = View.GONE
                    }
                    "payment_received" -> {
                        holder.approveOrderButton.visibility = View.VISIBLE
                        holder.approveOrderButton.text = "Duyệt đơn"
                        holder.markAsFailedButton.visibility = View.GONE
                    }
                    else -> {
                        holder.approveOrderButton.visibility = View.GONE
                        holder.markAsFailedButton.visibility = View.GONE
                    }
                }
            }
            "processing" -> { // Logic cho màn hình đơn hàng đang xử lý/giao (AdminOrderProcessingActivity)
                when (currentOrder.status) {
                    "approved", "processing" -> {
                        holder.approveOrderButton.visibility = View.VISIBLE
                        holder.approveOrderButton.text = "Đánh dấu Đang giao"
                        holder.markAsFailedButton.visibility = View.GONE
                    }
                    "shipping" -> { // <-- THÊM MỚI: Đơn hàng đang giao
                        holder.approveOrderButton.visibility = View.VISIBLE
                        holder.approveOrderButton.text = "Đánh dấu Đã giao hàng"
                        holder.markAsFailedButton.visibility = View.VISIBLE
                    }
                    else -> {
                        holder.approveOrderButton.visibility = View.GONE
                        holder.markAsFailedButton.visibility = View.GONE
                    }
                }
            }
            else -> {
                holder.approveOrderButton.visibility = View.GONE
                holder.markAsFailedButton.visibility = View.GONE
            }
        }
    }


    override fun getItemCount() = orderList.size
}