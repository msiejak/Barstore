import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
    id("com.google.android.gms.oss-licenses-plugin")
    id("kotlin-kapt")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

fun buildTime(): String {
    val dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy_HH:mm:ss")
    val now = LocalDateTime.now()
    return dtf.format(now)
}

fun getSystemUserName(): String {
    return System.getProperty("user.name")
}

android {
    compileSdk = 32
    buildToolsVersion = "32.0.0"
    val subName = "1"
    val username = "-" + getSystemUserName()
    defaultConfig {
        applicationId = "com.msiejak.barstore"
        minSdk = 26
        targetSdk = 32
        versionCode = 5
        versionName = "1.0.0"
        resValue(
            "string",
            "build_time",
            buildTime()
        )
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
            resValue(
                "string",
                "version_name",
                defaultConfig.versionName + versionNameSuffix
            )
            buildConfigField(
                "String",
                "ADMOB_AD_ID",
                "${gradleLocalProperties(rootDir).getProperty("admob.ad_id")}"
            )
            resValue(
                "string",
                "admob_app_id",
                "${gradleLocalProperties(rootDir).getProperty("admob.app_id")}"
            )
        }
        debug {
            resValue(
                "bool",
                "isNotRelease",
                "true"
            )
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev_${buildTime()}" + "GMT-5" + username
            splits.abi.isEnable = false
            splits.density.isEnable = false
            resValue(
                "string",
                "version_name",
                defaultConfig.versionName + versionNameSuffix + username
            )
            buildConfigField(
                "String",
                "ADMOB_AD_ID",
                "\"ca-app-pub-3940256099942544/2247696110\""
            )
            resValue(
                "string",
                "admob_app_id",
                "\"ca-app-pub-3940256099942544~3347511713\""
            )
        }
        create("dogfood") {
            resValue(
                "bool",
                "isNotRelease",
                "true"
            )
            versionNameSuffix = "-dogfood $subName"
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue(
                "string",
                "version_name",
                defaultConfig.versionName + versionNameSuffix
            )
            buildConfigField(
                "String",
                "ADMOB_AD_ID",
                "${gradleLocalProperties(rootDir).getProperty("admob.ad_id")}"
            )
            resValue(
                "string",
                "admob_app_id",
                "${gradleLocalProperties(rootDir).getProperty("admob.app_id")}"
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
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.7.0-alpha02")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.mlkit:barcode-scanning:17.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0-native-mt")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
    implementation("com.google.zxing:core:3.4.1")
    implementation(platform("com.google.firebase:firebase-bom:30.0.2"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")
    implementation("androidx.core:core-splashscreen:1.0.0-rc01")
    implementation("androidx.browser:browser:1.4.0")
    debugImplementation("com.google.android.gms:play-services-ads:21.0.0")
    "dogfoodImplementation"("com.google.android.gms:play-services-ads-lite:21.0.0")
    releaseImplementation("com.google.android.gms:play-services-ads-lite:21.0.0")
    debugImplementation(project(":internal"))
    "dogfoodImplementation"(project(":internal"))
    implementation(project(":nativetemplates"))

}