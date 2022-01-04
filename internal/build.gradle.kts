plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = 31
    buildToolsVersion = "31.0.0"

    defaultConfig {
        minSdk = 26
        targetSdk = 31
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("dogfood") {
            isMinifyEnabled = true
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.0-rc02"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("com.google.android.material:material:1.6.0-alpha01")
    implementation("androidx.constraintlayout:constraintlayout:2.1.2")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.activity:activity-compose:1.4.0")
    // Compose Material Design
    implementation("androidx.compose.material:material:1.1.0-rc01")
    implementation("androidx.compose.material3:material3:1.0.0-alpha02")
    // Animations
    implementation("androidx.compose.animation:animation:1.1.0-rc01")
    // Tooling support (Previews, etc.)
    implementation("androidx.compose.ui:ui-tooling:1.1.0-rc01")
    // Integration with ViewModels
    implementation("com.google.android.material:compose-theme-adapter:1.1.2")
}