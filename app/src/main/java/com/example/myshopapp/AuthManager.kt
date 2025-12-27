package com.example.myshopapp

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

//    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    callback(true, null)
//                } else {
//                    callback(false, task.exception?.message)
//                }
//            }
//    }
    fun login(email: String, password:String, callback:(Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(task.isSuccessful) {
                callback(true, null)
            }else {
                callback(false, task.exception?.message)
            }

        }
    }
    fun register(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val newUser = User(id = userId, email = email, role = "buyer")
                        db.collection("Users").document(userId).set(newUser)
                            .addOnSuccessListener {
                                Log.d("AuthManager", "Tạo tài khoản thành công")
                                callback(true, "Đăng ký tài khoản thành công.")
                            }
                            .addOnFailureListener { e ->
                                Log.e("AuthManager", "Lỗi khi lưu thông tin người dùng vào Firestore", e)
                                callback(false, "Lỗi lưu người dùng ${e.localizedMessage}")
                            }
                    } else {
                        Log.e("AuthManager", "Lỗi không lấy được UID sau khi đăng ký")
                        callback(false, "Đăng ký thành công nhưng không lấy được thông tin người dùng.")
                    }
                } else {
                    val errorMessage = when (val exception = task.exception) {
                        is FirebaseAuthWeakPasswordException -> "Mật khẩu quá yếu. Vui lòng chọn mật khẩu mạnh hơn."
                        is FirebaseAuthInvalidCredentialsException -> "Email không hợp lệ. Vui lòng kiểm tra lại."
                        is FirebaseAuthUserCollisionException -> "Email đã được sử dụng. Vui lòng dùng email khác."
                        else -> exception?.localizedMessage ?: "Đăng ký thất bại. Vui lòng thử lại."
                    }
                    Log.e("AuthManager", "Đăng ký thất bại: $errorMessage", task.exception)
                    callback(false, errorMessage)
                }
            }
    }
//    fun register(email:String, password:String, callback:(Boolean, String?) -> Unit) {
//        auth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if(task.isSuccessful) {
//                    val userId = auth.currentUser?.uid
//                    if(userId != null) {
//                        val newUsers = User(id = userId, email = email, role = "buyer")
//                        db.collection("Users").document(userId).set(newUsers)
//                            .addOnSuccessListener {
//                                Log.d("AuthManager", "Tạo tk thành công")
//                                callback(true, "Đky thành công")
//                            }
//                            .addOnFailureListener { e ->
//                                Log.e("AuthManager", "Ko thành công")
//                                callback(false, "Đky ko thành công")
//                            }
//                    }else {
//                        Log.d("AuthManager", "Đăng ký ko thaành công")
//                        callback(false, "Đky thành công nhưng ko lấy đc UID")
//                    }
//                }else {
//                    val errorMessage = when(val exception = task.exception) {
//                        is FirebaseAuthWeakPasswordException -> "Mật khẩu yếu"
//                        is FirebaseAuthInvalidCredentialsException -> "Email ko hợp lế"
//                        is FirebaseAuthUserCollisionException -> "Email đã swr dụng"
//                        else -> exception?.localizedMessage ?: "Đăng ký ko thành công"
//                    }
//                    Log.e("AuthManager", "Đăng ký thaats bại")
//                    callback(false, errorMessage)
//                }
//            }
//    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun signOut() {
        auth.signOut()
    }
}