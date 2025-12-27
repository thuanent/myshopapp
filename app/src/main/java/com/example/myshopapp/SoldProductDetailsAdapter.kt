package com.example.myshopapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SoldProductDetailsAdapter(private var soldProductList: List<SoldProductDetailsItem>) :
    RecyclerView.Adapter<SoldProductDetailsAdapter.SoldProductViewHolder>() {

    class SoldProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
        val quantityTextView: TextView = itemView.findViewById(R.id.quantityTextView)
        val revenueTextView: TextView = itemView.findViewById(R.id.revenueTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoldProductViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sold_product_details, parent, false)
        return SoldProductViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SoldProductViewHolder, position: Int) {
        val currentItem = soldProductList[position]
        holder.productNameTextView.text = currentItem.productName
        holder.quantityTextView.text = "SL: ${currentItem.quantitySold}"
        holder.revenueTextView.text = "Doanh thu: ${String.format("%.0f", currentItem.totalProductRevenue)} VND"
    }

    override fun getItemCount(): Int = soldProductList.size

    fun updateData(newList: List<SoldProductDetailsItem>) {
        soldProductList = newList
        notifyDataSetChanged()
    }
}