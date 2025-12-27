package com.example.myshopapp

import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        authManager = AuthManager()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val phoneEditText = findViewById<EditText>(R.id.phoneEditText)
        val addressEditText = findViewById<EditText>(R.id.addressEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val address = addressEditText.text.toString().trim()

            // Kiểm tra input
            if (!email.endsWith("@gmail.com") || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Vui lòng nhập đúng định dạng Gmail", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ số điện thoại và địa chỉ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gọi AuthManager để đăng ký
//            authManager.register(email, password) { success, message ->
//                runOnUiThread {
//                    Toast.makeText(this, message ?: "", Toast.LENGTH_SHORT).show()
//                    if (success) {
//                        val userId = authManager.getCurrentUserId()
//                        if (userId != null) {
//                            val db = FirebaseFirestore.getInstance()
//                            db.collection("Users").document(userId).update(
//                                mapOf(
//                                    "email" to email,
//                                    "role" to "buyer",
//                                    "fullName" to "",
//                                    "phoneNumber" to phone,
//                                    "address" to address
//                                )
//                            )
//                        }
//                        finish() // Quay lại màn hình đăng nhập
//                    }
//                }
//            }
            authManager.register(email, password) {success, message ->
                runOnUiThread{
                    Toast.makeText(this, message?: "", Toast.LENGTH_SHORT).show()
                    if(success) {
                        val userId = authManager.getCurrentUserId()
                        if(userId != null) {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("Users").document(userId).update(
                                mapOf(
                                    "email" to email,
                                    "role" to "buyer",
                                    "fullName" to "",
                                    "phoneNumber" to phone,
                                    "address" to address
                                )
                            )
                        }
                        finish()
                    }
                }
            }
        }
    }
}

