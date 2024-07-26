// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}
buildscript {
//    ext.objectboxVersion = "4.0.1" // For Groovy build scripts
     val objectboxVersion by extra("4.0.1") // For KTS build scripts

    repositories {
        mavenCentral()
        // Note: 2.9.0 and older are available on jcenter()
    }

    dependencies {
        // Android Gradle Plugin 4.1.0 or later supported.
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("io.objectbox:objectbox-gradle-plugin:$objectboxVersion")
    }
}