package com.example.myshopapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LowStockProductAdapter(private var lowStockProductList: List<Product>) :
    RecyclerView.Adapter<LowStockProductAdapter.LowStockProductViewHolder>() {

    // ViewHolder chứa các View của một item sản phẩm sắp hết hàng
    inner class LowStockProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lowStockProductName: TextView = itemView.findViewById(R.id.lowStockProductName)
        val lowStockProductStock: TextView = itemView.findViewById(R.id.lowStockProductStock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LowStockProductViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_low_stock_product, parent, false)
        return LowStockProductViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LowStockProductViewHolder, position: Int) {
        val currentProduct = lowStockProductList[position]

        holder.lowStockProductName.text = currentProduct.name
        holder.lowStockProductStock.text = "SL: ${currentProduct.stock}" // Hiển thị số lượng tồn kho
    }

    // Trả về tổng số lượng item trong danh sách
    override fun getItemCount() = lowStockProductList.size

    // Hàm để cập nhật dữ liệu cho Adapter và thông báo thay đổi
    fun updateData(newList: List<Product>) {
        lowStockProductList = newList
        notifyDataSetChanged()
    }
}