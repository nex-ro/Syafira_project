plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.project"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.project"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding=true;
    }
}

dependencies {
    implementation(libs.firebase.database)
    implementation("com.google.firebase:firebase-database:20.1.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("com.google.android.material:material:1.2.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.appcompat:appcompat-resources:1.5.1")
    implementation("androidx.emoji2:emoji2-views-helper:1.2.0")
    implementation("androidx.emoji2:emoji2:1.2.0")
    implementation("androidx.lifecycle:lifecycle-process:2.5.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}