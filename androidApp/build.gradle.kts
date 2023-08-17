plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "dev.andrew.prosto.android"
    compileSdk = 33
    defaultConfig {
        applicationId = "dev.andrew.prosto"
        minSdk = 21
        targetSdk = 33
        versionCode = 9
        versionName = "0.4"
        // Required when setting minSdkVersion to 20 or lower
        // multiDexEnabled true
        manifestPlaceholders += mapOf(
            "application_icon" to "@mipmap/ic_launcher",
            "application_roundIcon" to "@mipmap/ic_launcher_round"
        )

    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders += mapOf(
                "application_icon" to "@mipmap/ic_launcher_debug",
                "application_roundIcon" to "@mipmap/ic_launcher_debug"
            )
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}


dependencies {
    val activityCompose = "1.7.2"
    val composeUI = "1.4.3"
    val material3 = "1.1.1"
    val accompanistFlowLayout = "0.30.1"
    val coilCompose = "2.4.0"
    val brickNavigation = "2.2.0"

    implementation(project(":shared"))

    implementation("androidx.activity:activity-compose:$activityCompose")

    implementation("androidx.compose.ui:ui:$composeUI")
    implementation("androidx.compose.ui:ui-tooling:$composeUI")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeUI")
    implementation("androidx.compose.foundation:foundation:$composeUI")
    implementation("androidx.compose.foundation:foundation-layout:$composeUI")
    implementation("androidx.compose.material:material-icons-extended:$composeUI")

    implementation("androidx.compose.material3:material3:$material3")

    implementation("com.google.accompanist:accompanist-flowlayout:$accompanistFlowLayout")

    implementation("io.coil-kt:coil-compose:$coilCompose")
    implementation("io.github.g0dkar:qrcode-kotlin-android:3.3.0")
    implementation("io.github.alphicc:brick:$brickNavigation")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime-jvm:0.4.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}