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
        targetSdk = (property("android.compileSdk") as String).toInt()
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
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = javaVersion.toString()
        // Required for AWS SDK for Kotlin
        freeCompilerArgs += listOf("-Xjvm-default=all")
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":aws-runtime:kotlin-sdk-auth"))
    implementation(project(":aws-runtime:cognito-auth"))
    
    // AWS SDK for Kotlin dependencies
    implementation("aws.sdk.kotlin:s3:1.4.69")
    implementation("aws.sdk.kotlin:cognitoidentity:1.4.87")
    implementation("aws.sdk.kotlin:aws-core:1.4.69")
    
    // OpenTelemetry dependencies
    implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.31.0")
    
    // Android dependencies
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    coreLibraryDesugaring(libs.desugarJdkLibs)
}
