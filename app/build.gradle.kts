plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.eldercareplus"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.eldercareplus"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Firebase Auth (required)
    implementation("com.google.firebase:firebase-auth-ktx")

// For OTP auto-retrieval (recommended)
    implementation("com.google.android.gms:play-services-auth-api-phone:18.0.1")


    // AppCompat (for XML theme)
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Compose
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // 🔥 FIREBASE (EXPLICIT VERSIONS — NO BOM)
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.3")

    // Location Service
    implementation("com.google.android.gms:play-services-location:21.1.0")

    // Extended Icons
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
}
