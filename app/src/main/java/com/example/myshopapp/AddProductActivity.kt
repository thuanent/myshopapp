package com.example.myshopapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddProductActivity : AppCompatActivity() {

    private lateinit var edtProductName: EditText
    private lateinit var edtProductPrice: EditText
    private lateinit var edtProductDescription: EditText
    private lateinit var edtProductImageUrl: EditText
    private lateinit var edtProductSizes: EditText
    private lateinit var edtProductStock: EditText
    private lateinit var btnSaveProduct: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        edtProductName = findViewById(R.id.edtProductName)
        edtProductPrice = findViewById(R.id.edtProductPrice)
        edtProductDescription = findViewById(R.id.edtProductDescription)
        edtProductImageUrl = findViewById(R.id.edtProductImageUrl)
        edtProductSizes = findViewById(R.id.edtProductSizes)
        edtProductStock = findViewById(R.id.edtProductStock)
        btnSaveProduct = findViewById(R.id.btnSaveProduct)

        btnSaveProduct.setOnClickListener {
            saveProduct()
        }
    }

    private fun saveProduct() {
        val name = edtProductName.text.toString().trim()
        val priceStr = edtProductPrice.text.toString().trim()
        val description = edtProductDescription.text.toString().trim()
        val imageUrl = edtProductImageUrl.text.toString().trim()
        val sizesStr = edtProductSizes.text.toString().trim()
        val stockStr = edtProductStock.text.toString().trim() // **Đọc giá trị từ EditText số lượng tồn kho


        // Xử lý chuỗi size: tách bằng dấu phẩy, loại bỏ khoảng trắng thừa, và lọc bỏ các chuỗi rỗng
        var sizes = sizesStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        // **Kiểm tra nếu danh sách size rỗng thì gán giá trị mặc định "S", "M", "L"**
        if (sizes.isEmpty()) {
            sizes = listOf("S", "M", "L")
        }



        if (name.isEmpty() || priceStr.isEmpty() || description.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin sản phẩm (tên, giá, mô tả, URL ảnh)", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            Toast.makeText(this, "Giá sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        //kiểm tra số lượng tồn kho
        val stock = stockStr.toIntOrNull()
        if (stock == null || stock < 0) {
            Toast.makeText(this, "Số lượng tồn kho không hợp lệ. Vui lòng nhập số nguyên không âm.", Toast.LENGTH_SHORT).show()
            return // Dừng hàm nếu số lượng tồn kho không hợp lệ
        }


        // Tạo đối tượng Product với tất cả các trường
        val product = Product(
            name = name,
            price = price,
            description = description,
            imageUrl = imageUrl,
            stock = stock,
            sizes = sizes
        )

        db.collection("Products")
            .add(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show()
                finish() // Đóng Activity sau khi thêm
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi thêm sản phẩm: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
    }
}