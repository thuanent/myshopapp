plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    //
    id("kotlin-parcelize") // Thêm dòng này
    id("kotlin-kapt")
    id("com.google.gms.google-services") // Plugin Google Services
}

android {
    namespace = "com.example.myshopapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.thuan.myshopapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

}

dependencies {
    implementation("com.google.android.material:material:1.10.0") // Sử dụng phiên bản mới nhất nếu có
    implementation("androidx.cardview:cardview:1.0.0") // Sử dụng phiên bản mới nhất nếu có
    // ConstraintLayout (để bố cục linh hoạt hơn)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // Sử dụng phiên bản mới nhất nếu có
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22") // Phiên bản phải khớp với plugin
    implementation("androidx.activity:activity-ktx:1.9.3") // Cho Parcelable
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.material3.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    // Chỉ giữ một dòng Firebase BoM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Gson (Cần cho việc xử lý dữ liệu JSON)
    implementation("com.google.code.gson:gson:2.10.1") // Hoặc phiên bản mới nhất

    // OkHttp (Thư viện để thực hiện các cuộc gọi HTTP/HTTPS API)
    implementation("com.squareup.okhttp3:okhttp:4.11.0") // Hoặc phiên bản mới nhất
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0") // Logging (Rất hữu ích khi debug các cuộc gọi API)

    // Coroutines (Để thực hiện các tác vụ bất đồng bộ như gọi API mà không làm đơ UI)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // Hoặc phiên bản mới nhất
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Hoặc phiên bản mới nhất
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2") // Hoặc phiên bản mới nhất (Cung cấp CoroutineScope cho Activity/Fragment)

}


