package com.example.myshopapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.Role
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authManager = AuthManager()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        if (loginButton == null) {
            Log.e("LoginActivity", "Không tìm thấy nút Đăng nhập (R.id.loginButton) trong layout!")
        } else {
            Log.d("LoginActivity", "Đã tìm thấy nút Đăng nhập trong layout.")
            loginButton.setOnClickListener {
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                authManager.login(email, password) { success, message ->
                    if (success) {
                        val userId = authManager.getCurrentUserId()
                        if (userId != null) {
                            getUserRoleAndNavigate(userId)
                        } else {
                            Toast.makeText(this, "Lỗi ko tìm thấy id", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        Toast.makeText(this,"Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        if (registerButton == null) {
            Log.e("LoginActivity", "Không tìm thấy nút Đăng ký (R.id.registerButton) trong layout!")
        } else {
            Log.d("LoginActivity", "Đã tìm thấy nút Đăng ký trong layout.") // **LOG Info**
            registerButton.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
    }

//    private fun getUserRoleAndNavigate(userId: String) {
//        Log.d("LoginActivity", "Trong hàm getUserRoleAndNavigate cho UID: $userId")
//        db.collection("Users")
//            .document(userId)
//            .get()
//            .addOnSuccessListener { document ->
//                Log.d("LoginActivity", "Firestore get document thành công.")
//                if (document.exists()) {
//                    val role = document.getString("role")
//                    Log.d("LoginActivity", "Document tồn tại. Vai trò đọc được: $role")
//                    navigateToAppropriateActivity(userId, role)
//                } else {
//                    Log.w("LoginActivity", "Document không tồn tại cho UID: $userId")
//                    Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng trong cơ sở dữ liệu.", Toast.LENGTH_SHORT).show()
//                    authManager.signOut()
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.e("LoginActivity", "Firestore get document thất bại: ", exception)
//                Toast.makeText(this, "Lỗi khi lấy thông tin người dùng.", Toast.LENGTH_SHORT).show()
//                authManager.signOut()
//            }
//    }

//    private fun navigateToAppropriateActivity(userId: String, role: String?) {
//        Log.d("LoginActivity", "Trong hàm navigateToAppropriateActivity. Vai trò: $role")
//        val intent: Intent
//        if (role != null && role.equals("admin", ignoreCase = true)) {
//            intent = Intent(this, ProductActivity::class.java)
//            Log.d("LoginActivity", "Xác định vai trò ADMIN. Chuyển hướng đến Admin Dashboard")
//        } else {
//            intent = Intent(this, ProductActivity::class.java)
//            Log.d("LoginActivity", "Xác định vai trò KHÁCH HÀNG (hoặc không xác định). Chuyển hướng đến Product Activity")
//        }
//        intent.putExtra("USER_ID", userId)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(intent)
//        finish()
//    }

    private fun navigateToApproiateActivity(userId: String, role:String? ){
        val intent: Intent
        if(role != null && role.equals("Admin", ignoreCase = true)) {
            intent = Intent(this, ProductActivity::class.java)
        }else{
            intent = Intent(this, ProductActivity::class.java)
        }
        intent.putExtra("USER_ID", userId)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun getUserRoleAndNavigate(userId:String) {
        db.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if(document.exists()){
                    val role = document.getString("role")
                    navigateToApproiateActivity(userId, role)
                }else {
                    authManager.signOut()
                }
            }
            .addOnFailureListener {
                authManager.signOut()
            }
    }
}