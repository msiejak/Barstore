buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://maven.localazy.com/repository/release/")
        maven(url = "https://jitpack.io")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0")
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
        classpath("com.google.firebase:perf-plugin:1.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}