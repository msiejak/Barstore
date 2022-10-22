buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://maven.localazy.com/repository/release/")
        maven(url = "https://jitpack.io")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("com.google.gms:google-services:4.3.14")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.2")
        classpath("com.google.firebase:perf-plugin:1.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.5")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}