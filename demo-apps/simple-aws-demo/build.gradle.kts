plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val javaVersion = rootProject.extra["java_version"] as JavaVersion

android {
    namespace = "software.amazon.opentelemetry.android.demo.simple"
    compileSdk = (property("android.compileSdk") as String).toInt()

    defaultConfig {
        applicationId = "software.amazon.opentelemetry.android.demo.simple"
        minSdk = (property("android.minSdk") as String).toInt()
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
        jvmTarget = javaVersion.toString()
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":otelagent"))
    
    // AWS SDK dependencies
    implementation("com.amazonaws:aws-android-sdk-core:2.72.0")
    implementation("com.amazonaws:aws-android-sdk-s3:2.72.0")
    implementation("com.amazonaws:aws-android-sdk-cognitoauth:2.72.0")
    
    // OpenTelemetry dependencies
    implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.31.0")
    
    // Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
