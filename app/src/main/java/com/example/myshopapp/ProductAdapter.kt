package com.example.myshopapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.security.KeyStore.PrivateKeyEntry

//class ProductAdapter(
//    private val productList: MutableList<Product>,
//    var isAdmin: Boolean,
//
//    private val onItemClick: (Product, String?) -> Unit,
//    private val onDeleteClick: (Product) -> Unit,
//    private val onEditClick: (Product) -> Unit
//) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
//    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val productImage: ImageView = itemView.findViewById(R.id.productImage)
//        val productName: TextView = itemView.findViewById(R.id.productName)
//        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
//        val productStockTextView: TextView = itemView.findViewById(R.id.productStockTextView)
//        val addToCartButton: Button = itemView.findViewById(R.id.addToCartButton)
//        val deleteProductIcon: ImageView = itemView.findViewById(R.id.deleteProductIcon)
//        val sizeSpinner: Spinner = itemView.findViewById(R.id.sizeSpinner)
//        val outOfStockText: TextView = itemView.findViewById(R.id.outOfStockText)
//        val editProductIcon: ImageView = itemView.findViewById(R.id.editProductIcon)
//
//
//        var selectedSize: String? = null
//
//        init {
//            itemView.setOnClickListener {
//                val position = adapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    onItemClick.invoke(productList[position], selectedSize)
//                }
//            }
//
//            // Listener cho nút "Thêm vào giỏ hàng"
//            addToCartButton.setOnClickListener {
//                val position = adapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    val product = productList[position]
//                    if (product.sizes.isNotEmpty() && selectedSize == null) {
//                        Toast.makeText(itemView.context, "Vui lòng chọn kích thước cho ${product.name}!", Toast.LENGTH_SHORT).show()
//                    } else {
//                        onItemClick.invoke(product, selectedSize)
//                    }
//                }
//            }
//            // Listener cho biểu tượng xóa cho admin
//            deleteProductIcon.setOnClickListener {
//                val position = adapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    onDeleteClick.invoke(productList[position])
//                }
//            }
//            editProductIcon.setOnClickListener {
//                val position = adapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    onEditClick.invoke(productList[position])
//                }
//            }
//            sizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                    selectedSize = parent?.getItemAtPosition(position).toString()
//                }
//
//                override fun onNothingSelected(parent: AdapterView<*>?) {
//                    selectedSize = null
//                }
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
//        val itemView = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_product, parent, false)
//        return ProductViewHolder(itemView)
//    }
//
//    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
//        val currentProduct = productList[position]
//
//        holder.productName.text = currentProduct.name
//        holder.productPrice.text = "Giá: ${currentProduct.price} VND"
//        holder.productStockTextView.text = "Tồn kho: ${currentProduct.stock}"
//
//        Glide.with(holder.productImage.context).load(currentProduct.imageUrl).into(holder.productImage)
//
//        if (currentProduct.sizes.isNotEmpty()) {
//            holder.sizeSpinner.visibility = View.VISIBLE
//            val adapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, currentProduct.sizes)
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            holder.sizeSpinner.adapter = adapter
//            holder.selectedSize = null
//        } else {
//            holder.sizeSpinner.visibility = View.GONE
//            holder.selectedSize = null
//        }
//
//        if (currentProduct.stock <= 0) {
//            // Sản phẩm hết hàng
//            holder.addToCartButton.visibility = View.GONE
//            holder.sizeSpinner.visibility = View.GONE
//            holder.outOfStockText.visibility = View.VISIBLE
//        } else {
//            holder.addToCartButton.visibility = View.VISIBLE
//            holder.outOfStockText.visibility = View.GONE
//            if (currentProduct.sizes.isNotEmpty()) {
//                holder.sizeSpinner.visibility = View.VISIBLE
//            } else {
//                holder.sizeSpinner.visibility = View.GONE
//            }
//        }
//
//
//        if (!isAdmin) {
//            if (currentProduct.stock <= 0) {
//                holder.addToCartButton.visibility = View.GONE
//                holder.sizeSpinner.visibility = View.GONE
//                holder.outOfStockText.visibility = View.VISIBLE
//            } else {
//                holder.addToCartButton.visibility = View.VISIBLE
//                holder.outOfStockText.visibility = View.GONE
//                if (currentProduct.sizes.isNotEmpty()) {
//                    holder.sizeSpinner.visibility = View.VISIBLE
//                } else {
//                    holder.sizeSpinner.visibility = View.GONE
//                }
//            }
//            holder.productStockTextView.visibility = View.GONE
//        } else {
//            holder.addToCartButton.visibility = View.GONE
//            holder.sizeSpinner.visibility = View.GONE
//            holder.outOfStockText.visibility = View.GONE
//            holder.productStockTextView.visibility = View.VISIBLE
//
//            holder.deleteProductIcon.visibility = View.VISIBLE
//            holder.editProductIcon.visibility = View.VISIBLE
//        }
//    }
//
//    // Trả về tổng số lượng item trong danh sách
//    override fun getItemCount() = productList.size
//
//    fun removeItem(product: Product) {
//        val index = productList.indexOf(product)
//        if (index != -1) {
//            productList.removeAt(index)
//            notifyItemRemoved(index)
//        }
//    }
//
//    fun updateData(newList: List<Product>) {
//        productList.clear()
//        productList.addAll(newList)
//        notifyDataSetChanged()
//    }
//}
class ProductAdapter (
    private val productList: MutableList<Product>,
    var isAdmin: Boolean,

    private val onItemClick: (Product, String?) -> Unit,
    private val onDeleteClick: (Product) -> Unit,
    private val onEditClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // SỬ DỤNG MAP ĐỂ LƯU TRỮ KÍCH THƯỚC ĐÃ CHỌN CHO TỪNG SẢN PHẨM
    // Key: Product ID, Value: Kích thước đã chọn
    private val selectedSizesMap = mutableMapOf<String, String>()

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        val productStockTextView: TextView = itemView.findViewById(R.id.productStockTextView)
        val addToCartButton: Button = itemView.findViewById(R.id.addToCartButton)
        val deleteProductIcon: ImageView = itemView.findViewById(R.id.deleteProductIcon)
        val sizeSpinner: Spinner = itemView.findViewById(R.id.sizeSpinner)
        val outOfStockText: TextView = itemView.findViewById(R.id.outOfStockText)
        val editProductIcon: ImageView = itemView.findViewById(R.id.editProductIcon)

        // LOẠI BỎ selectedSize Ở ĐÂY. NÓ SẼ ĐƯỢC QUẢN LÝ BẰNG selectedSizesMap


        init {
            // Logic thêm vào giỏ hàng:
            addToCartButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = productList[position]
                    val selectedSize = selectedSizesMap[product.id] // LẤY KÍCH THƯỚC TỪ MAP

                    if (product.sizes.isNotEmpty() && selectedSize.isNullOrEmpty()) {
                        // Nếu sản phẩm có kích thước và chưa chọn kích thước nào (hoặc đã chọn "Chọn kích thước")
                        Toast.makeText(itemView.context, "Vui lòng chọn kích thước cho ${product.name}!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Gọi callback để thêm vào giỏ hàng
                        onItemClick.invoke(product, selectedSize)
                    }
                }
            }

            // ĐÚNG LẠI CHỨC NĂNG CỦA CÁC NÚT ADMIN
            deleteProductIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick.invoke(productList[position]) // SỬA: CALL onDeleteClick
                }
            }

            editProductIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditClick.invoke(productList[position])
                }
            }

            // Logic chọn Spinner:
            sizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val product = productList[adapterPosition] // Lấy sản phẩm hiện tại
                    val selectedValue = parent.getItemAtPosition(position).toString()

                    if (selectedValue == "Chọn kích thước") {
                        selectedSizesMap.remove(product.id) // Xóa khỏi map nếu chọn lại giá trị mặc định
                    } else {
                        selectedSizesMap[product.id] = selectedValue
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing, hoặc có thể reset lựa chọn nếu cần
                    val product = productList[adapterPosition]
                    selectedSizesMap.remove(product.id)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductAdapter.ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProductAdapter.ProductViewHolder, position: Int) {
        val currentProduct = productList[position]

        holder.productName.text = currentProduct.name
        holder.productPrice.text = "Giá: ${currentProduct.price} VNĐ"
        Glide.with(holder.productImage.context).load(currentProduct.imageUrl).into(holder.productImage)

        // HIỂN THỊ TỒN KHO CHO ADMIN, ẨN VỚI NGƯỜI DÙNG BÌNH THƯỜNG
        holder.productStockTextView.text = "Tồn kho: ${currentProduct.stock}"


        // --- LOGIC HIỂN THỊ CHÍNH XÁC VÀ KHÔNG GHI ĐÈ ---

        if (isAdmin) {
            // ADMIN VIEW
            holder.addToCartButton.visibility = View.GONE
            holder.sizeSpinner.visibility = View.GONE
            holder.outOfStockText.visibility = View.GONE

            holder.productStockTextView.visibility = View.VISIBLE
            holder.deleteProductIcon.visibility = View.VISIBLE
            holder.editProductIcon.visibility = View.VISIBLE
        } else {
            // USER VIEW
            holder.productStockTextView.visibility = View.GONE
            holder.deleteProductIcon.visibility = View.GONE
            holder.editProductIcon.visibility = View.GONE

            if (currentProduct.stock <= 0) {
                // SẢN PHẨM HẾT HÀNG
                holder.addToCartButton.visibility = View.GONE
                holder.sizeSpinner.visibility = View.GONE
                holder.outOfStockText.visibility = View.VISIBLE
            } else {
                // SẢN PHẨM CÒN HÀNG
                holder.addToCartButton.visibility = View.VISIBLE
                holder.outOfStockText.visibility = View.GONE

                if (currentProduct.sizes.isNotEmpty()) {
                    // SẢN PHẨM CÓ KÍCH THƯỚC: HIỂN THỊ SPINNER
                    holder.sizeSpinner.visibility = View.VISIBLE

                    val sizesListWithPrompt = mutableListOf("Chọn kích thước") // Thêm prompt
                    sizesListWithPrompt.addAll(currentProduct.sizes)

                    val adapter = ArrayAdapter(
                        holder.itemView.context,
                        android.R.layout.simple_spinner_item,
                        sizesListWithPrompt
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    holder.sizeSpinner.adapter = adapter

                    // KHÔI PHỤC LỰA CHỌN TỪ MAP KHI TÁI CHẾ VIEW
                    val storedSelectedSize = selectedSizesMap[currentProduct.id]
                    if (storedSelectedSize != null) {
                        val selectionIndex = sizesListWithPrompt.indexOf(storedSelectedSize)
                        if (selectionIndex != -1) {
                            holder.sizeSpinner.setSelection(selectionIndex)
                        } else {
                            // Nếu kích thước đã lưu không còn trong danh sách (vd: do data thay đổi), reset về prompt
                            holder.sizeSpinner.setSelection(0)
                            selectedSizesMap.remove(currentProduct.id)
                        }
                    } else {
                        // Nếu chưa có lựa chọn nào, đặt về prompt
                        holder.sizeSpinner.setSelection(0)
                    }

                } else {
                    // SẢN PHẨM KHÔNG CÓ KÍCH THƯỚC: ẨN SPINNER
                    holder.sizeSpinner.visibility = View.GONE
                    selectedSizesMap.remove(currentProduct.id) // Đảm bảo không có kích thước đã chọn lưu trữ
                }
            }
        }
    }

    override fun getItemCount() = productList.size

    fun removeItem(product: Product){
        val index = productList.indexOf(product)
        if(index != -1) {
            productList.removeAt(index)
            notifyItemRemoved(index)
            selectedSizesMap.remove(product.id) // Xóa kích thước đã chọn khi sản phẩm bị xóa
        }
    }

    fun updateData(newList: List<Product>){
        productList.clear()
        productList.addAll(newList)
        // Khi update data, cần rà soát lại selectedSizesMap để loại bỏ các id không còn tồn tại
        val newProductIds = newList.map { it.id }.toSet()
        val idsToRemove = selectedSizesMap.keys.filter { it !in newProductIds }
        idsToRemove.forEach { selectedSizesMap.remove(it) }
        notifyDataSetChanged()
    }
}