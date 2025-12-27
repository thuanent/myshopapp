package com.example.myshopapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore

class AccountActivity : AppCompatActivity() {

    private lateinit var buttonViewCart: Button
    private lateinit var buttonViewOrders: Button
    private lateinit var buttonApproveOrders: Button
    private lateinit var btnManageProcessingOrders: Button
    private lateinit var buttonManageUsers: Button
    private lateinit var buttonChangePassword: Button
    private lateinit var buttonLogout: Button
    private lateinit var textViewUserName: TextView
    private lateinit var buttonStatisticsOrders: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        // Khởi tạo Firebase Auth
        auth = Firebase.auth

        // Lấy userId từ Firebase Auth
        val currentUserId = auth.currentUser?.uid
        val currentUserEmail = auth.currentUser?.email

        if (currentUserId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Ánh xạ các view từ layout
        textViewUserName = findViewById(R.id.textViewUserName)
        buttonViewCart = findViewById(R.id.buttonViewCart)
        buttonViewOrders = findViewById(R.id.buttonViewOrders)
        buttonApproveOrders = findViewById(R.id.btnApproveOrders)
        btnManageProcessingOrders = findViewById(R.id.btnManageProcessingOrders)
        buttonStatisticsOrders = findViewById(R.id.buttonStatisticsOrders)

        buttonManageUsers = findViewById(R.id.btnManageUsers)
        buttonChangePassword = findViewById(R.id.buttonChangePassword)
        buttonLogout = findViewById(R.id.buttonLogout)


        // Hiển thị thông tin người dùng (tên hoặc email)
        textViewUserName.text = "Xin chào, ${auth.currentUser?.displayName ?: currentUserEmail ?: "Người dùng"}"

        // Kiểm tra vai trò của người dùng hiện tại để hiển thị/ẩn các nút chức năng admin
        checkUserRole(currentUserId)

        // Listener nút giỏ hàng
        buttonViewCart.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            intent.putExtra("USER_ID", currentUserId) // Truyền userId cho CartActivity nếu cần
            startActivity(intent)
        }

        // Listener nút lịch sử đơn hàng
        buttonViewOrders.setOnClickListener {
            val intent = Intent(this, OrderHistoryActivity::class.java)
            intent.putExtra("USER_ID", currentUserId) // Truyền userId cho OrderHistoryActivity nếu cần
            startActivity(intent)
        }

        // Listener nút duyệt đơn hàng (Chỉ hiển thị cho Admin)
        buttonApproveOrders.setOnClickListener {
            val intent = Intent(this, AdminOrderApprovalActivity::class.java)
            startActivity(intent)
        }

        // <-- Listener nút "Đơn hàng đang xử lý" (Chỉ hiển thị cho Admin)
        btnManageProcessingOrders.setOnClickListener {
            val intent = Intent(this, AdminOrderProcessingActivity::class.java) // <-- Điều hướng đến AdminOrderProcessingActivity
            startActivity(intent)
        }
        buttonStatisticsOrders.setOnClickListener {
            startActivity(Intent(this, OrderDetailsStatisticsActivity::class.java))
        }
        // Listener nút quản lý tài khoản người dùng (Chỉ hiển thị cho Admin)
        buttonManageUsers.setOnClickListener {
            val intent = Intent(this, ManageUsersActivity::class.java)
            startActivity(intent)
        }

        // Listener nút đổi mật khẩu
        buttonChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
            // TODO: Implement change password functionality
        }

        // Listener nút đăng xuất
        buttonLogout.setOnClickListener {
            auth.signOut()
            // Sau khi đăng xuất, chuyển về màn hình đăng nhập hoặc màn hình chính
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Đóng AccountActivity
            Toast.makeText(this, "Đã đăng xuất thành công.", Toast.LENGTH_SHORT).show()
        }
    }

    // Hàm kiểm tra vai trò người dùng và hiển thị/ẩn các nút chức năng admin
    private fun checkUserRole(userId: String) {
        FirebaseFirestore.getInstance().collection("Users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                val role = documentSnapshot.getString("role")
                if (role == "admin") {
                    // Là admin
                    // Các nút admin hiển thị
                    buttonApproveOrders.visibility = View.VISIBLE
                    btnManageProcessingOrders.visibility = View.VISIBLE
                    buttonManageUsers.visibility = View.VISIBLE

                    buttonStatisticsOrders.visibility = View.VISIBLE
                    buttonViewCart.visibility = View.GONE
                    buttonViewOrders.visibility = View.GONE

                } else {
                    buttonApproveOrders.visibility = View.GONE
                    btnManageProcessingOrders.visibility = View.GONE
                    buttonManageUsers.visibility = View.GONE
                    buttonStatisticsOrders.visibility = View.GONE
                    // Các nút user hiển thị
                    buttonViewCart.visibility = View.VISIBLE
                    buttonViewOrders.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                Log.e("AccountActivity", "Lỗi khi kiểm tra vai trò người dùng: ${e.message}", e)
                Toast.makeText(this, "Không thể kiểm tra vai trò người dùng. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()

                buttonApproveOrders.visibility = View.GONE
                btnManageProcessingOrders.visibility = View.GONE
                buttonManageUsers.visibility = View.GONE
                buttonStatisticsOrders.visibility = View.GONE

            }
    }
}