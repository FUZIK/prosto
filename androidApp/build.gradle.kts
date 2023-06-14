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
        versionCode = 6
        versionName = "0.2"
        // Required when setting minSdkVersion to 20 or lower
        // multiDexEnabled true

    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
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
        getByName("release") {
            isMinifyEnabled = true
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}


dependencies {
    implementation(project(":shared"))

    implementation("androidx.activity:activity-compose:1.7.2")

    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.ui:ui-tooling:1.4.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
    implementation("androidx.compose.foundation:foundation:1.4.3")
    implementation("androidx.compose.foundation:foundation-layout:1.4.3")

    implementation("androidx.compose.material3:material3:1.1.0")
    implementation("androidx.compose.material:material-icons-extended:1.4.3")

    implementation("com.google.accompanist:accompanist-flowlayout:0.28.0")

    implementation("io.coil-kt:coil-compose:2.2.2")
    implementation("io.github.g0dkar:qrcode-kotlin-android:3.3.0")
    implementation("io.github.alphicc:brick:2.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime-jvm:0.4.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

}