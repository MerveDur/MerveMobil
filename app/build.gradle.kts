plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.mervemobil"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mervemobil"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        println("once >>>> OPENWEATHER_API_KEY (Gradle): " + project.findProperty("OPENWEATHER_API_KEY"))

        buildConfigField(
            "String",
            "OPENWEATHER_API_KEY",
            "\"${rootProject.findProperty("OPENWEATHER_API_KEY") ?: ""}\""
        )



    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    //val apiKey: String = project.findProperty("OPENWEATHER_API_KEY") as String? ?: ""

}
println(">>>>> build.gradle'da KEY: ${project.properties["OPENWEATHER_API_KEY"]}")
println(">>>> OPENWEATHER_API_KEY (Gradle): " + project.findProperty("OPENWEATHER_API_KEY"))

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Compose BOM (Bill of Materials) — versiyon yönetimi kolaylığı için:
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))

    // Compose temel kütüphaneleri:
    implementation("androidx.activity:activity-compose")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // (İsteğe bağlı) Preview için:
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Konum izni
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.json:json:20220320")

    implementation("androidx.compose.material:material-icons-extended")



}
