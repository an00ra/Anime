plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.lagradost.cloudstream3.gradle")
}

android {
    defaultConfig {
        minSdk = 21
        compileSdk = 34
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}

version = 1

cloudstream {
    description = "Nonton Anime Bahasa Indonesia dari Samehadaku"
    authors = listOf("an00ra")
    status = 1
    tvTypes = listOf("Anime")
    requiresResources = true
    language = "id"
    iconUrl = "https://v2.samehadaku.how/wp-content/themes/samehada/assets/images/logo.png"
}
