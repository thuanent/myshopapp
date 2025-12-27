package com.example.myshopapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditProductActivity : AppCompatActivity() {

    private lateinit var edtProductName: EditText
    private lateinit var edtProductPrice: EditText
    private lateinit var edtProductDescription: EditText
    private lateinit var edtProductImageUrl: EditText
    private lateinit var edtProductSizes: EditText
    private lateinit var edtProductStock: EditText
    private lateinit var btnUpdateProduct: Button

    private val db = FirebaseFirestore.getInstance()
    private var productId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_product)

        edtProductName = findViewById(R.id.edtProductName)
        edtProductPrice = findViewById(R.id.edtProductPrice)
        edtProductDescription = findViewById(R.id.edtProductDescription)
        edtProductImageUrl = findViewById(R.id.edtProductImageUrl)
        edtProductSizes = findViewById(R.id.edtProductSizes)
        edtProductStock = findViewById(R.id.edtProductStock)
        btnUpdateProduct = findViewById(R.id.btnUpdateProduct)

        // Lấy Product ID từ Intent
        productId = intent.getStringExtra("PRODUCT_ID")
        Log.d("EditProductActivity", "Nhận được Product ID: $productId")

        // Kiểm tra nếu có Product ID, tải dữ liệu sản phẩm
        if (productId != null) {
            loadProductData(productId!!)
        } else {
            // Xử lý trường hợp không nhận được ID (lỗi)
            Toast.makeText(this, "Không tìm thấy ID sản phẩm để chỉnh sửa.", Toast.LENGTH_SHORT).show()
            finish() // Đóng activity
        }

        // Thiết lập Listener cho nút "Lưu thay đổi"
        btnUpdateProduct.setOnClickListener {
            if (productId != null) {
                updateProduct(productId!!)
            } else {
                Toast.makeText(this, "Không có ID sản phẩm, không thể cập nhật.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun loadProductData(id: String) {
        db.collection("Products").document(id).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val product = document.toObject(Product::class.java)
                    if (product != null) {
                        // Đổ dữ liệu sản phẩm vào các EditText
                        edtProductName.setText(product.name)
                        edtProductPrice.setText(product.price.toString())
                        edtProductDescription.setText(product.description)
                        edtProductImageUrl.setText(product.imageUrl)
                        // Chuyển List<String> sizes thành chuỗi "S, M, L"
                        edtProductSizes.setText(product.sizes.joinToString(", "))
                        edtProductStock.setText(product.stock.toString())

                        Log.d("EditProductActivity", "Đã tải dữ liệu sản phẩm: ${product.name}")

                    } else {
                        Toast.makeText(this, "Không thể đọc dữ liệu sản phẩm.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy sản phẩm.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditProductActivity", "Lỗi khi tải dữ liệu sản phẩm: ", e)
                Toast.makeText(this, "Lỗi khi tải dữ liệu sản phẩm.", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
    private fun updateProduct(id: String) {
        val name = edtProductName.text.toString().trim()
        val priceStr = edtProductPrice.text.toString().trim()
        val description = edtProductDescription.text.toString().trim()
        val imageUrl = edtProductImageUrl.text.toString().trim()
        val sizesStr = edtProductSizes.text.toString().trim()
        val stockStr = edtProductStock.text.toString().trim()

        // Xử lý chuỗi size
        val sizes = sizesStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (name.isEmpty() || priceStr.isEmpty() || description.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin sản phẩm (tên, giá, mô tả, URL ảnh)", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            Toast.makeText(this, "Giá sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        // Đọc và kiểm tra số lượng tồn kho
        val stock = stockStr.toIntOrNull()
        if (stock == null || stock < 0) {
            Toast.makeText(this, "Số lượng tồn kho không hợp lệ. Vui lòng nhập số nguyên không âm.", Toast.LENGTH_SHORT).show()
            return
        }

        // Tạo một Map chứa dữ liệu cần cập nhật
        val updatedProductData = hashMapOf(
            "name" to name,
            "price" to price,
            "description" to description,
            "imageUrl" to imageUrl,
            "sizes" to sizes,
            "stock" to stock
            // Không cập nhật ID ở đây
        )

        db.collection("Products").document(id)
            .update(updatedProductData as Map<String, Any>) // Sử dụng update thay vì set, và ép kiểu Map
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật sản phẩm thành công!", Toast.LENGTH_SHORT).show()
                finish() // Đóng Activity sau khi cập nhật
            }
            .addOnFailureListener { e ->
                Log.e("EditProductActivity", "Lỗi khi cập nhật sản phẩm: ", e)
                Toast.makeText(this, "Lỗi khi cập nhật sản phẩm: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}