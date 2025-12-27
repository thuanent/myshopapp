package com.example.myshopapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val cartList: MutableList<CartItem>,
    private val onItemRemoved: (CartItem) -> Unit,
    private val onQuantityChanged: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Ánh xạ các Views trong item_cart.xml
        val productImage: ImageView = itemView.findViewById(R.id.cartProductImage)
        val productName: TextView = itemView.findViewById(R.id.cartProductName)
        val productPrice: TextView = itemView.findViewById(R.id.cartProductPrice)
        val productSize: TextView = itemView.findViewById(R.id.cartProductSize)
        val productQuantity: TextView = itemView.findViewById(R.id.cartProductQuantity)
        val removeButton: ImageView = itemView.findViewById(R.id.cartRemoveButton)

        val plusButton: TextView = itemView.findViewById(R.id.btnIncrease)
        val minusButton: TextView = itemView.findViewById(R.id.btnDecrease)


        init {
            // Listener cho nút xóa
            removeButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemRemoved.invoke(cartList[position])
                }
            }
            plusButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = cartList[position]
                    item.quantity++ // Tăng số lượng
                    productQuantity.text = "SL: ${item.quantity}" // Cập nhật UI ngay lập tức
                    onQuantityChanged.invoke(item) // Gọi callback để Activity xử lý cập nhật Firestore
                }
            }
            minusButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = cartList[position]
                    if (item.quantity > 1) {
                        item.quantity--
                        productQuantity.text = "SL: ${item.quantity}"
                        onQuantityChanged.invoke(item)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val currentItem = cartList[position]

        holder.productName.text = currentItem.productName
        holder.productPrice.text = "Giá: ${formatCurrency(currentItem.productPrice)} VND"
        holder.productQuantity.text = "SL: ${currentItem.quantity}"
        holder.productSize.text = "Size: ${currentItem.selectedSize}"

        Glide.with(holder.itemView.context)
            .load(currentItem.productImageUrl)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_error)
            .into(holder.productImage)
    }

    override fun getItemCount() = cartList.size

    private fun formatCurrency(price: Double): String {
        val format = NumberFormat.getInstance(Locale("vi", "VN"))
        return format.format(price)
    }

    fun updateData(newList: List<CartItem>) {
        cartList.clear()
        cartList.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeItem(item: CartItem) {
        val index = cartList.indexOf(item)
        if (index != -1) {
            cartList.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}