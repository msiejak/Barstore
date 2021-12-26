import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.android.gms.oss-licenses-plugin")
}

fun buildTime(): String {
    val dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy_HH:mm:ss")
    val now = LocalDateTime.now()
    return dtf.format(now)
}

android {
    compileSdk = 32
    buildToolsVersion = "32.0.0"

    defaultConfig {
        applicationId = "com.msiejak.barstore"
        minSdk = 25
        targetSdk = 32
        versionCode = 2
        versionName = "0.0"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            resValue(
                "bool",
                "isNotRelease",
                "false"
            )
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            resValue(
                "bool",
                "isNotRelease",
                "true"
            )
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev_${buildTime()}" + "GMT-5"
            splits.abi.isEnable = false
            splits.density.isEnable = false
        }
        create("dogfood") {
            resValue(
                "bool",
                "isNotRelease",
                "true"
            )
            versionNameSuffix = "-dogfood_${buildTime()}" + "GMT-5"
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    dependenciesInfo {
        includeInBundle = true
        includeInApk = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("com.google.android.material:material:1.6.0-alpha01")
    implementation("androidx.constraintlayout:constraintlayout:2.1.2")
    testImplementation("junit:junit:4.13.2")
    implementation("com.google.mlkit:barcode-scanning:17.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0-native-mt")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("com.google.zxing:core:3.4.1")
    implementation(platform("com.google.firebase:firebase-bom:29.0.3"))
//    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")
//    debugImplementation(project(":internal"))
//    "dogfoodImplementation"(project(":internal"))

}