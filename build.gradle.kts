buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://maven.localazy.com/repository/release/")
        maven(url = "https://jitpack.io")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.8.1")
        classpath("com.google.firebase:perf-plugin:1.4.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.0")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.4")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}