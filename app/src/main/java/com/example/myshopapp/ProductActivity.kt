//package com.example.myshopapp
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.AdapterView
//import android.widget.ArrayAdapter
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.SearchView
//import android.widget.Spinner
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.android.material.bottomnavigation.BottomNavigationView
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.ktx.toObject
//import com.google.firebase.firestore.toObject
//
//class ProductActivity : AppCompatActivity() {
//
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var productAdapter: ProductAdapter
//    private val db = FirebaseFirestore.getInstance()
//
//    private lateinit var firestoreManager: FirestoreManager
//    private lateinit var userId: String
//    private lateinit var cartIcon: ImageView
//    private var isAdmin: Boolean = false
//
//    private lateinit var adminButtonsLayout: LinearLayout
//    private lateinit var btnAddProduct: Button
//    private lateinit var btnManageUsers: Button
//    private lateinit var bottomNavigationView:BottomNavigationView
//    private lateinit var btnViewStatistics: Button
//
//
//    private var originalProductList = mutableListOf<Product>()
//
//    private lateinit var searchView: SearchView
//    private lateinit var priceRangeSpinner: Spinner
//    private var selectedPriceRangeIndex: Int = 0
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_product)
//
//        userId = intent.getStringExtra("USER_ID") ?: ""
//        if (userId.isEmpty()) {
//            Log.e("ProductActivity", "Không nhận được userId, thoát...")
//            Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show()
//            finish()
//            return
//        }
//        Log.d("ProductActivity", "UserID hiện tại: $userId")
//
//        firestoreManager = FirestoreManager(userId)
//
//        recyclerView = findViewById(R.id.recyclerView)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        productAdapter = ProductAdapter(
//            mutableListOf(),
//            isAdmin = false,
//            onItemClick = { product, selectedSize -> addToCart(product, selectedSize) },
//            onDeleteClick = { product -> deleteProduct(product) },
//            onEditClick = { product -> navigateToEditProduct(product) } // Gọi hàm mới
//        )
//        recyclerView.adapter = productAdapter
//
//        cartIcon = findViewById(R.id.cartIcon)
//        cartIcon.setOnClickListener {
//            val intent = Intent(this, CartActivity::class.java)
//            intent.putExtra("USER_ID", userId)
//            startActivity(intent)
//        }
//
//        searchView = findViewById(R.id.searchView)
//        priceRangeSpinner = findViewById(R.id.priceRangeSpinner)
//        adminButtonsLayout = findViewById(R.id.adminButtonsLayout)
//        btnAddProduct = findViewById(R.id.btnAddProduct)
//        btnManageUsers = findViewById(R.id.btnManageUsers)
//        btnViewStatistics = findViewById(R.id.btnViewStatistics)
//
//
//        btnAddProduct.setOnClickListener {
//            Log.d("ProductActivity", "Đã ấn nút Thêm sản phẩm")
//            val intent = Intent(this, AddProductActivity::class.java)
//            startActivity(intent)
//        }
//
//        btnManageUsers.setOnClickListener {
//            Log.d("ProductActivity", "Đã ấn nút Quản lý tài khoản")
//            val intent = Intent(this, ManageUsersActivity::class.java)
//            startActivity(intent)
//        }
//
//        btnViewStatistics.setOnClickListener {
//            Log.d("ProductActivity", "Đã ấn nút Thống kê Sản phẩm")
//            val intent = Intent(this, AdminStatisticsActivity::class.java)
//            intent.putExtra("USER_ID", userId)
//            startActivity(intent)
//        }
//
//        bottomNavigationView = findViewById(R.id.bottom_navigation)
//        bottomNavigationView.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.nav_product_home -> {
//                    true
//                }
//                R.id.nav_account -> {
//                    // Chuyển đến màn hình Tài khoản
//                    val intent = Intent(this, AccountActivity::class.java)
//                    intent.putExtra("USER_ID", userId)
//                    intent.putExtra("IS_ADMIN", isAdmin)
//                    startActivity(intent)
//                    true
//                }
//                else -> false
//            }
//        }
//
//        bottomNavigationView.selectedItemId = R.id.nav_product_home
//        checkUserRoleAndSetupAdminFeatures()
//        setupSearchView()
//        setupPriceRangeSpinner()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        Log.d("ProductActivity", "onResume: Tải lại danh sách sản phẩm")
//        loadProductsFromFirestore()
//        bottomNavigationView.selectedItemId = R.id.nav_product_home
//
//    }
//
//    private fun setupSearchView() {
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                filterProducts(query, selectedPriceRangeIndex)
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                filterProducts(newText, selectedPriceRangeIndex)
//                return true
//            }
//        })
//    }
//
//
////    private fun setupPriceRangeSpinner() {
////        val adapter = ArrayAdapter.createFromResource(
////            this,
////            R.array.price_ranges,
////            android.R.layout.simple_spinner_item
////        )
////        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
////        priceRangeSpinner.adapter = adapter
////
////        priceRangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
////            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
////                selectedPriceRangeIndex = position
////                val currentQuery = searchView.query?.toString()
////                filterProducts(currentQuery, selectedPriceRangeIndex)
////            }
////            override fun onNothingSelected(parent: AdapterView<*>?) {
////            }
////        }
////    }
//    //hàm lọc sản phaamr theo giá
//    private fun setupPriceRangeSpinner() {
//        val adapter =ArrayAdapter.createFromResource(
//            this,
//            R.array.price_ranges,
//            android.R.layout.simple_spinner_item
//        )
//    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//    priceRangeSpinner.adapter = adapter
//
//    priceRangeSpinner.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener {
//        override fun onItemSelected(parent:AdapterView<*>?, view:View?, position:Int, id:Long) {
//            selectedPriceRangeIndex = position
//            val currentQuery = searchView.query?.toString()
//            filterProducts(currentQuery, selectedPriceRangeIndex)
//        }
//
//        override fun onNothingSelected(parent:AdapterView<*>?) {
//            TODO("Not yet implemented")
//        }
//
//    }
//    }
////    private fun filterProducts(query: String?, priceRangeIndex: Int) {
////        var filteredList = originalProductList.toList()
////        filteredList = when (priceRangeIndex) {
////            1 -> filteredList.filter { it.price >= 0 && it.price <= 100000.0 } // Khoảng 0 - 100.000
////            2 -> filteredList.filter { it.price > 100000.0 && it.price <= 500000.0 } // Khoảng 100.001 - 500.000
////            3 -> filteredList.filter { it.price > 500000.0 && it.price <= 1000000.0 } // Khoảng 500.001 - 1.000.000
////            else -> filteredList
////        }
////
////        if (!query.isNullOrEmpty()) {
////            filteredList = filteredList.filter {
////                it.name.contains(query, ignoreCase = true)
////            }
////        }
////        productAdapter.updateData(filteredList.toMutableList())
////    }
//    private fun filterProducts(query: String?, priceRangeIndex: Int) {
//        var filteredList = originalProductList.toList()
//        filteredList = when(priceRangeIndex) {
//            1 -> filteredList.filter { it.price >=0 && it.price <= 100000.0}
//            2 -> filteredList.filter { it.price > 100000.0 && it.price <= 500000.0 }
//            3 -> filteredList.filter { it.price > 500000.0 && it.price <= 1000000.0 }
//            else -> filteredList
//        }
//        if(!query.isNullOrEmpty()) {
//            filteredList = filteredList.filter {
//                it.name.contains(query, ignoreCase = true)
//            }
//        }
//    productAdapter.updateData(filteredList.toMutableList())
//    }
//    private fun checkUserRoleAndSetupAdminFeatures() {
//        db.collection("Users")
//            .document(userId)
//            .get()
//            .addOnSuccessListener { document ->
//                if (document != null && document.exists()) {
//                    val role = document.getString("role")
//                    isAdmin = role == "admin"
//                    Log.d("ProductActivity", "User ID: $userId, Vai trò đọc được: $role, isAdmin: $isAdmin")
//
//                    // Cập nhật hiển thị các nút admin và Adapter
//                    setupAdminUI(isAdmin)
//
//                } else {
//                    Log.w("ProductActivity", "Không tìm thấy document người dùng cho UID: $userId")
//                    // Xử lý khi không tìm thấy user: coi như không phải admin
//                    setupAdminUI(false)
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.e("ProductActivity", "Lỗi khi lấy vai trò người dùng: ", exception)
//                // Xử lý lỗi: coi như không phải admin
//                setupAdminUI(false)
//            }
//    }
//
//    //Hiển thị các thành phần cho admin
//    private fun setupAdminUI(isAdmin: Boolean) {
//        // Hiển thị/ẩn layout chứa các nút admin
//        if (isAdmin) {
//            adminButtonsLayout.visibility = View.VISIBLE
//            Toast.makeText(this, "Chào mừng Admin!", Toast.LENGTH_SHORT).show()
//        } else {
//            adminButtonsLayout.visibility = View.GONE
//        }
//        productAdapter.isAdmin = isAdmin
//    }
//
//    private fun loadProductsFromFirestore() {
//        db.collection("Products").get()
//            .addOnSuccessListener { result ->
//                val loadedProducts = mutableListOf<Product>()
//                for (document in result) {
//                    try {
//                        val product = document.toObject(Product::class.java)
//                        product.id = document.id
//                        loadedProducts.add(product)
//                    } catch (e: Exception) {
//                        Log.e("ProductActivity", "Failed to map document ${document.id} to Product.", e)
//                        Log.e("ProductActivity", "Document data: ${document.data}")
//                    }
//                }
//
//                loadedProducts.sortByDescending { it.timestamp }
//                originalProductList = loadedProducts
//                val currentQuery = searchView.query?.toString()
//                filterProducts(currentQuery, selectedPriceRangeIndex)
//
//                Log.d("ProductActivity", "Đã tải và sắp xếp ${originalProductList.size} sản phẩm từ Firestore")
//            }
//            .addOnFailureListener { e ->
//                Log.e("Firestore", "Lỗi khi lấy danh sách sản phẩm từ Firestore.", e)
//                Toast.makeText(this, "Lỗi khi tải danh sách sản phẩm", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//
//    private fun addToCart(product: Product, selectedSize: String?) {
//        if (!isAdmin) {
//
//            if (product.stock <= 0) {
//                Toast.makeText(this, "${product.name} hiện đã hết hàng.", Toast.LENGTH_SHORT).show()
//                Log.d("ProductActivity", "Add to cart prevented: Product is out of stock.")
//                return // Dừng lại nếu sản phẩm hết hàng
//            }
//
//            if (product.sizes.isNotEmpty() && selectedSize == null) {
//                Toast.makeText(this, "Vui lòng chọn kích thước cho ${product.name}!", Toast.LENGTH_SHORT).show()
//                Log.d("ProductActivity", "Add to cart prevented: Size not selected for product with sizes.")
//                return
//            }
//            firestoreManager.addToCart(product, selectedSize ?: "") { success, message ->
//                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            Toast.makeText(this, "Admin không thể thêm sản phẩm vào giỏ hàng.", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun navigateToEditProduct(product: Product) {
//        Log.d("ProductActivity", "Chuyển đến màn hình chỉnh sửa sản phẩm: ${product.name}, ID: ${product.id}")
//        val intent = Intent(this, EditProductActivity::class.java)
//        intent.putExtra("PRODUCT_ID", product.id)
//        startActivity(intent)
//    }
//
//
//    private fun deleteProduct(product: Product) {
//
//        Toast.makeText(this, "Đang xóa sản phẩm: ${product.name}", Toast.LENGTH_SHORT).show()
//        db.collection("Products").document(product.id)
//            .delete()
//            .addOnSuccessListener {
//                Toast.makeText(this, "Đã xóa sản phẩm: ${product.name} thành công!", Toast.LENGTH_SHORT).show()
//                // Cập nhật cả danh sách gốc và adapter sau khi xóa
//                originalProductList.remove(product)
//                // Sau khi xóa, áp dụng lại bộ lọc hiện tại để cập nhật UI
//                val currentQuery = searchView.query?.toString()
//                filterProducts(currentQuery, selectedPriceRangeIndex)
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Lỗi khi xóa sản phẩm: ${e.message}", Toast.LENGTH_SHORT).show()
//                Log.e("ProductActivity", "Lỗi khi xóa sản phẩm", e)
//            }
//    }
//
//}
package com.example.myshopapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query // Import này quan trọng cho orderBy
import com.google.firebase.firestore.ktx.toObject

class ProductActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView // RecyclerView cho tất cả sản phẩm
    private lateinit var productAdapter: ProductAdapter // Adapter cho tất cả sản phẩm
    private val db = FirebaseFirestore.getInstance()

    // THÊM KHAI BÁO CHO SẢN PHẨM BÁN CHẠY
    private lateinit var bestSellingRecyclerView: RecyclerView
    private lateinit var bestSellingAdapter: ProductAdapter // Có thể tái sử dụng ProductAdapter

    private lateinit var firestoreManager: FirestoreManager
    private lateinit var userId: String
    private lateinit var cartIcon: ImageView
    private var isAdmin: Boolean = false

    private lateinit var adminButtonsLayout: LinearLayout
    private lateinit var btnAddProduct: Button
    private lateinit var btnManageUsers: Button
    private lateinit var bottomNavigationView:BottomNavigationView
    private lateinit var btnViewStatistics: Button


    private var originalProductList = mutableListOf<Product>()

    private lateinit var searchView: SearchView
    private lateinit var priceRangeSpinner: Spinner
    private var selectedPriceRangeIndex: Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        userId = intent.getStringExtra("USER_ID") ?: ""
        if (userId.isEmpty()) {
            Log.e("ProductActivity", "Không nhận được userId, thoát...")
            Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        Log.d("ProductActivity", "UserID hiện tại: $userId")

        firestoreManager = FirestoreManager(userId)

        // Khởi tạo RecyclerView cho TẤT CẢ SẢN PHẨM
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this) // Thường là dọc
        productAdapter = ProductAdapter(
            mutableListOf(),
            isAdmin = false,
            onItemClick = { product, selectedSize -> addToCart(product, selectedSize) },
            onDeleteClick = { product -> deleteProduct(product) },
            onEditClick = { product -> navigateToEditProduct(product) }
        )
        recyclerView.adapter = productAdapter

        // KHỞI TẠO RECYCLERVIEW CHO SẢN PHẨM BÁN CHẠY
        bestSellingRecyclerView = findViewById(R.id.bestSellingProductsRecyclerView)
        // Dùng LinearLayoutManager.HORIZONTAL để hiển thị theo chiều ngang
        bestSellingRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        bestSellingAdapter = ProductAdapter(
            mutableListOf(),
            isAdmin = false, // Sản phẩm bán chạy hiển thị cho người dùng bình thường
            onItemClick = { product, selectedSize -> addToCart(product, selectedSize) },
            onDeleteClick = { /* Không cho phép xóa/sửa từ danh sách bán chạy */ },
            onEditClick = { /* Không cho phép xóa/sửa từ danh sách bán chạy */ }
        )
        bestSellingRecyclerView.adapter = bestSellingAdapter

        cartIcon = findViewById(R.id.cartIcon)
        cartIcon.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        searchView = findViewById(R.id.searchView)
        priceRangeSpinner = findViewById(R.id.priceRangeSpinner)
        adminButtonsLayout = findViewById(R.id.adminButtonsLayout)
        btnAddProduct = findViewById(R.id.btnAddProduct)
        btnManageUsers = findViewById(R.id.btnManageUsers)
        btnViewStatistics = findViewById(R.id.btnViewStatistics)


        btnAddProduct.setOnClickListener {
            Log.d("ProductActivity", "Đã ấn nút Thêm sản phẩm")
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
        }

        btnManageUsers.setOnClickListener {
            Log.d("ProductActivity", "Đã ấn nút Quản lý tài khoản")
            val intent = Intent(this, ManageUsersActivity::class.java)
            startActivity(intent)
        }

        btnViewStatistics.setOnClickListener {
            Log.d("ProductActivity", "Đã ấn nút Thống kê Sản phẩm")
            val intent = Intent(this, AdminStatisticsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_product_home -> { // Giữ nguyên ID này nếu bạn đã có nó trong bottom_nav_menu.xml
                    true
                }
                R.id.nav_account -> { // Giữ nguyên ID này nếu bạn đã có nó trong bottom_nav_menu.xml
                    // Chuyển đến màn hình Tài khoản
                    val intent = Intent(this, AccountActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    intent.putExtra("IS_ADMIN", isAdmin)
                    startActivity(intent)
                    true
                }
                // THÊM CHỨC NĂNG CHATBOT Ở ĐÂY SAU (nếu bạn làm tiếp)
                // R.id.navigation_chatbot -> {
                //     val intent = Intent(this, ChatbotActivity::class.java) // Hoặc Fragment
                //     startActivity(intent)
                //     true
                // }
                else -> false
            }
        }

        bottomNavigationView.selectedItemId = R.id.nav_product_home
        checkUserRoleAndSetupAdminFeatures()
        setupSearchView()
        setupPriceRangeSpinner()

        // GỌI HÀM TẢI SẢN PHẨM BÁN CHẠY
        loadBestSellingProducts()
    }

    override fun onResume() {
        super.onResume()
        Log.d("ProductActivity", "onResume: Tải lại danh sách sản phẩm")
        loadProductsFromFirestore() // Tải lại tất cả sản phẩm
        loadBestSellingProducts() // Tải lại sản phẩm bán chạy khi Activity resume
        bottomNavigationView.selectedItemId = R.id.nav_product_home

    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterProducts(query, selectedPriceRangeIndex)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts(newText, selectedPriceRangeIndex)
                return true
            }
        })
    }

    private fun setupPriceRangeSpinner() {
        val adapter =ArrayAdapter.createFromResource(
            this,
            R.array.price_ranges,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        priceRangeSpinner.adapter = adapter

        priceRangeSpinner.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent:AdapterView<*>?, view:View?, position:Int, id:Long) {
                selectedPriceRangeIndex = position
                val currentQuery = searchView.query?.toString()
                filterProducts(currentQuery, selectedPriceRangeIndex)
            }

            override fun onNothingSelected(parent:AdapterView<*>?) {
                // Có thể để trống hoặc thêm logic xử lý nếu không có gì được chọn
            }
        }
    }

    private fun filterProducts(query: String?, priceRangeIndex: Int) {
        var filteredList = originalProductList.toList()
        filteredList = when(priceRangeIndex) {
            1 -> filteredList.filter { it.price >=0 && it.price <= 100000.0}
            2 -> filteredList.filter { it.price > 100000.0 && it.price <= 500000.0 }
            3 -> filteredList.filter { it.price > 500000.0 && it.price <= 1000000.0 }
            else -> filteredList
        }
        if(!query.isNullOrEmpty()) {
            filteredList = filteredList.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        productAdapter.updateData(filteredList.toMutableList())
    }

    private fun checkUserRoleAndSetupAdminFeatures() {
        db.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")
                    isAdmin = role == "admin"
                    Log.d("ProductActivity", "User ID: $userId, Vai trò đọc được: $role, isAdmin: $isAdmin")

                    // Cập nhật hiển thị các nút admin và Adapter
                    setupAdminUI(isAdmin)

                } else {
                    Log.w("ProductActivity", "Không tìm thấy document người dùng cho UID: $userId")
                    // Xử lý khi không tìm thấy user: coi như không phải admin
                    setupAdminUI(false)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProductActivity", "Lỗi khi lấy vai trò người dùng: ", exception)
                // Xử lý lỗi: coi như không phải admin
                setupAdminUI(false)
            }
    }

    //Hiển thị các thành phần cho admin
    private fun setupAdminUI(isAdmin: Boolean) {
        // Hiển thị/ẩn layout chứa các nút admin
        if (isAdmin) {
            adminButtonsLayout.visibility = View.VISIBLE
            Toast.makeText(this, "Chào mừng Admin!", Toast.LENGTH_SHORT).show()
        } else {
            adminButtonsLayout.visibility = View.GONE
        }
        productAdapter.isAdmin = isAdmin
    }

    // Hàm tải TẤT CẢ SẢN PHẨM (giữ nguyên)
    private fun loadProductsFromFirestore() {
        db.collection("Products").get()
            .addOnSuccessListener { result ->
                val loadedProducts = mutableListOf<Product>()
                for (document in result) {
                    try {
                        val product = document.toObject(Product::class.java)
                        product.id = document.id
                        loadedProducts.add(product)
                    } catch (e: Exception) {
                        Log.e("ProductActivity", "Failed to map document ${document.id} to Product.", e)
                        Log.e("ProductActivity", "Document data: ${document.data}")
                    }
                }

                loadedProducts.sortByDescending { it.timestamp }
                originalProductList = loadedProducts
                val currentQuery = searchView.query?.toString()
                filterProducts(currentQuery, selectedPriceRangeIndex)

                Log.d("ProductActivity", "Đã tải và sắp xếp ${originalProductList.size} sản phẩm từ Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Lỗi khi lấy danh sách sản phẩm từ Firestore.", e)
                Toast.makeText(this, "Lỗi khi tải danh sách sản phẩm", Toast.LENGTH_SHORT).show()
            }
    }

    // HÀM MỚI: TẢI SẢN PHẨM BÁN CHẠY
    private fun loadBestSellingProducts() {
        db.collection("Products")
            .orderBy("soldCount", Query.Direction.DESCENDING) // Sắp xếp giảm dần theo soldCount
            .whereGreaterThanOrEqualTo("soldCount", 10L) // <--- SỬA DÒNG NÀY
            .limit(10) // Giới hạn chỉ lấy 10 sản phẩm bán chạy nhất
            .get()
            .addOnSuccessListener { querySnapshot ->
                val bestSellingProducts = mutableListOf<Product>()
                for (document in querySnapshot.documents) {
                    val product = document.toObject(Product::class.java)
                    if (product != null) {
                        bestSellingProducts.add(product)
                        Log.d("BestSellingDebug", "Đã tải SP: ${product.name}, ID: ${product.id}, SoldCount: ${product.soldCount}")

                    }
                }
                bestSellingAdapter.updateData(bestSellingProducts) // Cập nhật dữ liệu cho adapter sản phẩm bán chạy
                Log.d("ProductActivity", "Đã tải ${bestSellingProducts.size} sản phẩm bán chạy.")
            }
            .addOnFailureListener { e ->
                Log.e("ProductActivity", "Lỗi tải sản phẩm bán chạy", e)
                Toast.makeText(this, "Lỗi khi tải sản phẩm bán chạy.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun addToCart(product: Product, selectedSize: String?) {
        if (!isAdmin) {

            if (product.stock <= 0) {
                Toast.makeText(this, "${product.name} hiện đã hết hàng.", Toast.LENGTH_SHORT).show()
                Log.d("ProductActivity", "Add to cart prevented: Product is out of stock.")
                return // Dừng lại nếu sản phẩm hết hàng
            }

            if (product.sizes.isNotEmpty() && selectedSize == null) {
                Toast.makeText(this, "Vui lòng chọn kích thước cho ${product.name}!", Toast.LENGTH_SHORT).show()
                Log.d("ProductActivity", "Add to cart prevented: Size not selected for product with sizes.")
                return
            }
            firestoreManager.addToCart(product, selectedSize ?: "") { success, message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Admin không thể thêm sản phẩm vào giỏ hàng.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToEditProduct(product: Product) {
        Log.d("ProductActivity", "Chuyển đến màn hình chỉnh sửa sản phẩm: ${product.name}, ID: ${product.id}")
        val intent = Intent(this, EditProductActivity::class.java)
        intent.putExtra("PRODUCT_ID", product.id)
        startActivity(intent)
    }


    private fun deleteProduct(product: Product) {

        Toast.makeText(this, "Đang xóa sản phẩm: ${product.name}", Toast.LENGTH_SHORT).show()
        db.collection("Products").document(product.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Đã xóa sản phẩm: ${product.name} thành công!", Toast.LENGTH_SHORT).show()
                // Cập nhật cả danh sách gốc và adapter sau khi xóa
                originalProductList.remove(product)
                // Sau khi xóa, áp dụng lại bộ lọc hiện tại để cập nhật UI
                val currentQuery = searchView.query?.toString()
                filterProducts(currentQuery, selectedPriceRangeIndex)
                loadBestSellingProducts() // Tải lại danh sách bán chạy sau khi xóa
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi xóa sản phẩm: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ProductActivity", "Lỗi khi xóa sản phẩm", e)
            }
    }
}