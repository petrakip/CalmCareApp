plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.diamonds"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.diamonds"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.okhttp)
    implementation (libs.play.services.location.v2101)
    implementation ("com.github.3llomi:CircularStatusView:V1.0.3")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")
}