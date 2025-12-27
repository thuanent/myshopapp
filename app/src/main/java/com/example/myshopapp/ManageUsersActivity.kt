package com.example.myshopapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ManageUsersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        recyclerView = findViewById(R.id.usersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        userAdapter = UserAdapter(userList) { user ->
            deleteUser(user)
        }
        recyclerView.adapter = userAdapter

        loadUsers()
    }

    private fun loadUsers() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập hoặc phiên đã hết hạn.", Toast.LENGTH_SHORT).show()
            finish() // Đóng Activity nếu không có người dùng
            return
        }

        db.collection("Users").document(currentUserId).get()
            .addOnSuccessListener { adminDoc ->
                val adminRole = adminDoc.getString("role")
                if (adminRole == "admin") {
                    db.collection("Users")
                        .whereEqualTo("role", "buyer")
                        .get()
                        .addOnSuccessListener { result ->
                            userList.clear()
                            for (document in result) {
                                val user = document.toObject(User::class.java)
                                userList.add(user)
                            }
                            userAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Log.e("ManageUsersActivity", "Lỗi khi tải danh sách người dùng", e)
                            Toast.makeText(this, "Lỗi khi tải danh sách người dùng", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Bạn không có quyền truy cập chức năng này.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ManageUsersActivity", "Lỗi khi kiểm tra quyền admin", e)
                Toast.makeText(this, "Có lỗi xảy ra khi kiểm tra quyền.", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun deleteUser(user: User) {
        Toast.makeText(this, "Đang xóa người dùng ${user.fullName}...", Toast.LENGTH_SHORT).show()

        db.collection("Users").document(user.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Đã xóa người dùng ${user.fullName}", Toast.LENGTH_SHORT).show()
                loadUsers()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi xóa người dùng: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ManageUsersActivity", "Lỗi khi xóa người dùng: ${e.message}", e)
            }
    }
}