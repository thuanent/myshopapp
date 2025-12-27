package com.example.myshopapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.text.SimpleDateFormat

class PaymentActivity : AppCompatActivity() {

    private val MOMO_PARTNER_CODE = "MOMO"
    private val MOMO_ACCESS_KEY = "F8BBA842ECF85"
    private val MOMO_SECRET_KEY = "K951B6PE1waDMi640xX08PD3vg6EkVlz"

    // Endpoint tạo đơn hàng Sandbox v2 của MoMo
    private val MOMO_CREATE_ORDER_ENDPOINT = "https://test-payment.momo.vn/v2/gateway/api/create"

    private lateinit var totalAmountTextView: TextView
    private lateinit var edtFullName: EditText
    private lateinit var edtPhoneNumber: EditText
    private lateinit var edtAddress: EditText
    private lateinit var payButton: Button
    private lateinit var qrCodeImageView: ImageView
    private lateinit var btnConfirmBankTransfer: Button
    private lateinit var radioCash: RadioButton
    private lateinit var radioBankTransfer: RadioButton
    private lateinit var radioMomo: RadioButton
    private lateinit var paymentMethodRadioGroup: RadioGroup

    private var totalAmount: Double = 0.0
    private var cartItems: List<CartItem> = listOf()

    private val db = FirebaseFirestore.getInstance()

    // Request Code để xử lý kết quả trả về từ ứng dụng MoMo sau khi mở qua Intent
    private val MOMO_REQUEST_CODE = 2002

    // OkHttp client để thực hiện các cuộc gọi API
    private val okHttpClient = OkHttpClient()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        totalAmountTextView = findViewById(R.id.totalAmountTextView)
        edtFullName = findViewById(R.id.edtFullName)
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber)
        edtAddress = findViewById(R.id.edtAddress)
        payButton = findViewById(R.id.payButton)
        qrCodeImageView = findViewById(R.id.qrCodeImageView)
        btnConfirmBankTransfer = findViewById(R.id.btnConfirmBankTransfer)
        radioCash = findViewById(R.id.radioCash)
        radioBankTransfer = findViewById(R.id.radioBankTransfer)
        radioMomo = findViewById(R.id.radioMomo)
        paymentMethodRadioGroup = findViewById(R.id.paymentMethodGroup)


        // Lấy dữ liệu đơn hàng và giỏ hàng từ Intent
        totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)
        cartItems = intent.getParcelableArrayListExtra<CartItem>("CART_ITEMS") as List<CartItem> ?: listOf()

        // Hiển thị tổng tiền
        totalAmountTextView.text = "Tổng tiền: ${String.format("%.0f", totalAmount)} VND"


        qrCodeImageView.setImageResource(R.drawable.my_qr)
        qrCodeImageView.visibility = View.GONE
        btnConfirmBankTransfer.visibility = View.GONE

        // Listener khi người dùng chọn phương thức thanh toán
        paymentMethodRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioCash -> {
                    payButton.visibility = View.VISIBLE
                    qrCodeImageView.visibility = View.GONE
                    btnConfirmBankTransfer.visibility = View.GONE
                }
                R.id.radioBankTransfer -> {
                    payButton.visibility = View.GONE
                    qrCodeImageView.visibility = View.VISIBLE
                    btnConfirmBankTransfer.visibility = View.VISIBLE
                }
                R.id.radioMomo -> {
                    payButton.visibility = View.VISIBLE
                    qrCodeImageView.visibility = View.GONE
                    btnConfirmBankTransfer.visibility = View.GONE
                }
            }
        }

        // Listener cho nút "Thanh toán"
        payButton.setOnClickListener {
            val selectedPaymentMethodId = paymentMethodRadioGroup.checkedRadioButtonId
            when (selectedPaymentMethodId) {
                R.id.radioCash -> processOrder(paymentMethod = "cash")
                R.id.radioMomo -> processOrder(paymentMethod = "momo")
                else -> Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener cho nút "Xác nhận đã chuyển khoản"
        btnConfirmBankTransfer.setOnClickListener {
            processOrder(paymentMethod = "bank_transfer")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MOMO_REQUEST_CODE) {
            if (data != null) {
                val momoData = data.getStringExtra("data")
                val signature = data.getStringExtra("signature")

                if (momoData != null) {
                    try {
                        val jsonResponse = JSONObject(momoData)
                        val status = jsonResponse.optInt("status")
                        val message = jsonResponse.optString("message")
                        val transId = jsonResponse.optString("transid")
                        val orderId = jsonResponse.optString("orderId")
                        val errorCode = jsonResponse.optInt("errorCode", -1)

                        Log.d("MoMoResult", "onActivityResult | Status: $status, Message: $message, TransId: $transId, OrderId: $orderId, ErrorCode: $errorCode")

                        when (status) {
                            0 -> {
                                Toast.makeText(this, "Hoàn thành thao tác MoMo. Vui lòng chờ xác nhận.", Toast.LENGTH_LONG).show()
                                clearUserCart(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                            }
                            -1 -> {
                                Toast.makeText(this, "Bạn đã hủy thanh toán MoMo.", Toast.LENGTH_SHORT).show()
                            }
                            2 -> {
                                val errorMessage = message ?: "Lỗi không xác định"
                                Toast.makeText(this, "Thanh toán MoMo thất bại: $errorMessage (Lỗi: $errorCode)", Toast.LENGTH_LONG).show()
                            }
                            else -> {
                                Toast.makeText(this, "Kết quả thanh toán MoMo không xác định.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MoMoResult", "onActivityResult | Lỗi phân tích dữ liệu trả về từ MoMo: ${e.message}", e)
                        Toast.makeText(this, "Lỗi xử lý kết quả MoMo.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("MoMoResult", "onActivityResult | Dữ liệu trả về từ MoMo rỗng.")
                    Toast.makeText(this, "Không nhận được dữ liệu kết quả từ MoMo.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("MoMoResult", "onActivityResult | Intent trả về từ MoMo rỗng.")
                Toast.makeText(this, "Không nhận được kết quả từ MoMo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProductStock(items: List<CartItem>, onComplete: (Boolean, String) -> Unit) {
        if (items.isEmpty()) {
            Log.d("PaymentActivity", "updateProductStock: Danh sách sản phẩm rỗng, không cần cập nhật tồn kho.")
            onComplete(true, "Không có sản phẩm nào để cập nhật tồn kho.")
            return
        }

        val batch = db.batch()

        for (item in items) {
            val productRef = db.collection("Products").document(item.productId ?: "")

            if (item.productId.isNullOrEmpty()) {
                Log.e("PaymentActivity", "updateProductStock: ProductId rỗng cho item giỏ hàng, bỏ qua cập nhật tồn kho.")
                continue
            }

            batch.update(productRef, "stock", FieldValue.increment(-(item.quantity?.toLong() ?: 0L)))
            Log.d("PaymentActivity", "updateProductStock: Thêm thao tác giảm stock cho sản phẩm ${item.productId} với số lượng ${item.quantity} vào batch.")
        }

        if (items.isNotEmpty()) {
            batch.commit()
                .addOnSuccessListener {
                    Log.d("PaymentActivity", "updateProductStock: Batch cập nhật tồn kho thành công.")
                    onComplete(true, "Đã cập nhật tồn kho thành công.")
                }
                .addOnFailureListener { e ->
                    Log.e("PaymentActivity", "updateProductStock: Lỗi khi commit batch cập nhật tồn kho: ${e.message}", e)
                    onComplete(false, "Lỗi khi cập nhật tồn kho.")
                }
        } else {
            onComplete(true, "Không có sản phẩm để cập nhật tồn kho.")
        }
    }

    private fun processOrder(paymentMethod: String) {
        val fullName = edtFullName.text.toString().trim()
        val phoneNumber = edtPhoneNumber.text.toString().trim()
        val address = edtAddress.text.toString().trim()

        if (fullName.isEmpty() || phoneNumber.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            return
        }

        // Tạo một tham chiếu document mới cho đơn hàng ở cấp gốc "Orders"
        val globalOrderDocRef = db.collection("Orders").document()
        val newOrderId = globalOrderDocRef.id // Sử dụng ID này cho cả hai bản ghi

        // Tạo tham chiếu document cho đơn hàng dưới User cụ thể
        val userOrderDocRef = db.collection("Users").document(userId).collection("Orders").document(newOrderId) // Sử dụng cùng ID

        val initialStatus = when (paymentMethod) {
            "momo" -> "pending_payment_approval"
            "bank_transfer" -> "pending"
            else -> "pending"
        }

        val orderToSave = Order(
            orderId = newOrderId,
            userId = userId,
            items = cartItems,
            totalAmount = totalAmount,
            status = initialStatus,
            paymentMethod = paymentMethod,
            address = address,
            fullName = fullName,
            phoneNumber = phoneNumber,
            timestamp = Timestamp.now(), // Sử dụng Timestamp.now() là tốt
            paymentInfo = null
        )

        Log.d("PaymentActivity", "processOrder: Đối tượng Order được tạo TRƯỚC khi lưu, với orderId = ${orderToSave.orderId}: $orderToSave")

        // **Sử dụng Write Batch để lưu đơn hàng ở cả hai vị trí**
        val batch = db.batch()

        // Lệnh ghi đầu tiên: Lưu vào collection "Orders" ở cấp gốc
        batch.set(globalOrderDocRef, orderToSave)
        Log.d("PaymentActivity", "processOrder: Thêm thao tác lưu đơn hàng ${newOrderId} vào collection 'Orders' cấp gốc vào batch.")

        // Lệnh ghi thứ hai: Lưu vào collection "Orders" dưới user đó
        batch.set(userOrderDocRef, orderToSave)
        Log.d("PaymentActivity", "processOrder: Thêm thao tác lưu đơn hàng ${newOrderId} vào collection 'Users/${userId}/Orders' vào batch.")

        // Commit batch để thực hiện cả hai thao tác ghi cùng lúc
        batch.commit()
            .addOnSuccessListener {
                Log.d("PaymentActivity", "processOrder: Đơn hàng $newOrderId đã lưu thành công ở cả hai vị trí với trạng thái ban đầu: $initialStatus")

                // Sau khi lưu đơn hàng thành công, tiếp tục với các bước thanh toán và xóa giỏ hàng
                if (paymentMethod == "momo") {
                    lifecycleScope.launch {
                        callCreateMomoOrderApiDirect(
                            orderId = newOrderId,
                            amount = totalAmount,
                            userId = userId,
                            fullName = fullName,
                            phoneNumber = phoneNumber,
                            address = address
                        )
                    }
                } else {
                    Log.d("PaymentActivity", "processOrder: Bắt đầu xóa giỏ hàng cho phương thức khác MoMo.")
                    clearUserCart(userId)
                    // Đối với COD/Bank Transfer, bạn có thể muốn giảm tồn kho ngay lập tức
//                    updateProductStock(cartItems) { success, message ->
//                        if (success) {
//                            Log.d("PaymentActivity", "processOrder: Cập nhật tồn kho thành công cho COD/Bank Transfer.")
//                        } else {
//                            Log.e("PaymentActivity", "processOrder: Lỗi cập nhật tồn kho cho COD/Bank Transfer: $message")
//                        }
//                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("PaymentActivity", "processOrder: Lỗi khi lưu đơn hàng $newOrderId vào Firestore (Batch failed)", e)
                Toast.makeText(this, "Đặt hàng thất bại: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    // Gọi MoMo API tạo đơn hàng trực tiếp từ Client và chứa Secret Key.
    private suspend fun callCreateMomoOrderApiDirect(orderId: String, amount: Double, userId: String, fullName: String, phoneNumber: String, address: String) {
        try {
            val requestId = UUID.randomUUID().toString()
            val orderInfo = "Thanh toán đơn hàng $orderId cho $fullName"
            val amountLong = amount.toLong()

            val ipnUrl = "https://example.com/momo/ipn"
            val redirectUrl = "momomomo://result" // <-- Schema chuyển hướng về app dựa trên Partner Code "MOMO"

            val extraDataJson = JSONObject().apply {
                put("your_order_id_from_app", orderId)
                put("user_id_from_app", userId)
            }
            val extraData = Base64.encodeToString(extraDataJson.toString().toByteArray(), Base64.NO_WRAP)

            val requestType = "captureWallet"

            val autoCaptureDelay = 900

            val orderParams = JSONObject().apply {
                put("partnerCode", MOMO_PARTNER_CODE)
                put("accessKey", MOMO_ACCESS_KEY)
                put("requestId", requestId)
                put("amount", amountLong)
                put("orderId", orderId)
                put("orderInfo", orderInfo)
                put("redirectUrl", redirectUrl)
                put("ipnUrl", ipnUrl)
                put("extraData", extraData)
                put("requestType", requestType)
                put("autoCaptureDelay", autoCaptureDelay)
                put("lang", "vi")
            }

            val dataToSign = "accessKey=${orderParams["accessKey"]}&amount=${orderParams["amount"]}&extraData=${orderParams["extraData"]}&ipnUrl=${orderParams["ipnUrl"]}&orderId=${orderParams["orderId"]}&orderInfo=${orderParams["orderInfo"]}&partnerCode=${orderParams["partnerCode"]}&redirectUrl=${orderParams["redirectUrl"]}&requestId=${orderParams["requestId"]}&requestType=${orderParams["requestType"]}"

            Log.d("MoMoAPI_Client", "Data to sign: $dataToSign")

            val signature = generateHmacSha256(dataToSign, MOMO_SECRET_KEY)

            val requestBodyJson = orderParams.apply {
                put("signature", signature)
            }.toString()

            Log.d("MoMoAPI_Client", "Request body sent to MoMo: $requestBodyJson")

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = requestBodyJson.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(MOMO_CREATE_ORDER_ENDPOINT)
                .post(body)
                .build()

            val response = withContext(Dispatchers.IO) {
                okHttpClient.newCall(request).execute()
            }

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("MoMoAPI_Client", "MoMo create order response: $responseBody")

                    if (responseBody != null) {
                        val jsonResponse = JSONObject(responseBody)
                        val resultCode = jsonResponse.optInt("resultCode", -1)
                        val message = jsonResponse.optString("message", "Unknown error")
                        val payUrl = jsonResponse.optString("payUrl", null)

                        if (resultCode == 0 && payUrl != null) {
                            Log.d("MoMoAPI_Client", "Tạo đơn hàng MoMo thành công. PayUrl: $payUrl")
                            Toast.makeText(this@PaymentActivity, "Chuyển đến MoMo để thanh toán...", Toast.LENGTH_SHORT).show()

                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(payUrl))
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

                            if (intent.resolveActivity(packageManager) != null) {
                                startActivityForResult(intent, MOMO_REQUEST_CODE)
                            } else {
                                Toast.makeText(this@PaymentActivity, "Không tìm thấy ứng dụng MoMo hoặc trình duyệt để xử lý thanh toán.", Toast.LENGTH_LONG).show()
                                Log.e("MoMoAPI_Client", "Không tìm thấy ứng dụng để mở payUrl: $payUrl")
                            }

                        } else {
                            Log.e("MoMoAPI_Client", "MoMo API báo lỗi tạo đơn hàng: $resultCode - $message")
                            Toast.makeText(this@PaymentActivity, "Lỗi từ MoMo: $message (Lỗi: $resultCode)", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.e("MoMoAPI_Client", "Phản hồi từ MoMo rỗng hoặc không hợp lệ.")
                        Toast.makeText(this@PaymentActivity, "MoMo trả về phản hồi không hợp lệ.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorBody = response.body?.string()
                    Log.e("MoMoAPI_Client", "Lỗi HTTP khi gọi MoMo API: ${response.code} - $errorBody")
                    Toast.makeText(this@PaymentActivity, "Lỗi kết nối đến MoMo: ${response.code}", Toast.LENGTH_LONG).show()
                }
            }

        } catch (e: Exception) {
            Log.e("MoMoAPI_Client", "Exception khi gọi MoMo API: ${e.message}", e)
            Toast.makeText(this@PaymentActivity, "Có lỗi xảy ra khi gọi API MoMo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Tạo chữ ký HMAC-SHA256 sử dụng Secret Key trực tiếp ở Client.
    private fun generateHmacSha256(data: String, key: String): String {
        try {
            val mac = Mac.getInstance("HmacSHA256")
            val secretKeySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
            mac.init(secretKeySpec)
            val bytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
            return bytes.fold("") { str, it -> str + "%02x".format(it) }
        } catch (e: Exception) {
            Log.e("SignatureGen", "Error generating HMAC-SHA256 signature for MoMo", e)
            return ""
        }
    }

    private fun clearUserCart(userId: String) {
        if (userId.isNullOrEmpty()) {
            Log.e("PaymentActivity", "clearUserCart: UserId rỗng, không thể xóa giỏ hàng.")
            navigateToHomeScreen()
            return
        }
        val cartCollectionRef = db.collection("Users").document(userId).collection("Cart")

        cartCollectionRef.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("PaymentActivity", "clearUserCart: Giỏ hàng của người dùng $userId rỗng, không cần xóa.")
                    Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
                    navigateToHomeScreen()
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                for (document in documents) {
                    batch.delete(cartCollectionRef.document(document.id))
                }

                batch.commit()
                    .addOnSuccessListener {
                        Log.d("PaymentActivity", "clearUserCart: Giỏ hàng của người dùng $userId đã xóa thành công.")
                        Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
                        navigateToHomeScreen()
                    }
                    .addOnFailureListener { e ->
                        Log.e("PaymentActivity", "clearUserCart: Lỗi khi xóa giỏ hàng của người dùng $userId", e)
                        Toast.makeText(this, "Đặt hàng thành công nhưng gặp lỗi khi cập nhật giỏ hàng.", Toast.LENGTH_SHORT).show()
                        navigateToHomeScreen()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("PaymentActivity", "clearUserCart: Lỗi khi lấy danh sách giỏ hàng của người dùng $userId để xóa", e)
                Toast.makeText(this, "Đặt hàng thành công nhưng gặp lỗi khi cập nhật giỏ hàng.", Toast.LENGTH_SHORT).show()
                navigateToHomeScreen()
            }
    }

    private fun navigateToHomeScreen() {
        val intent = Intent(this, ProductActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("PaymentActivity", "navigateToHomeScreen: User ID chuẩn bị được truyền sang ProductActivity: $userId")
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
        finish()
    }
}