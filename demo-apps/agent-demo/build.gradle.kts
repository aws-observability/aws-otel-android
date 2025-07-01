plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("net.bytebuddy.byte-buddy-gradle-plugin")
}

val javaVersion = rootProject.extra["java_version"] as JavaVersion


android {
    namespace = "software.amazon.opentelemetry.android.demo.agent"
    compileSdk = (property("android.compileSdk") as String).toInt()

    defaultConfig {
        applicationId = "software.amazon.opentelemetry.android.demo.agent"
        minSdk = (property("android.minSdk") as String).toInt()
        targetSdk = (property("android.compileSdk") as String).toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        animationsDisabled = true
        unitTests {
            isIncludeAndroidResources = true
        }
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
        freeCompilerArgs += listOf("-Xjvm-default=all")
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":agent"))

    // AWS SDK for Kotlin dependencies
    implementation("aws.sdk.kotlin:s3:1.4.116")
    implementation("aws.sdk.kotlin:cognitoidentity:1.4.87")
    implementation("aws.sdk.kotlin:aws-core:1.4.116")
    
    // OpenTelemetry dependencies
    compileOnly(libs.opentelemetry.api)

    // Android dependencies
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation(libs.okhttp3)
    byteBuddy(libs.opentelemetry.android.httpurlconnection.agent)
    byteBuddy(libs.opentelemetry.android.okhttp3.agent)
    // Testing
    testImplementation(libs.bundles.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test:runner:1.6.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
    coreLibraryDesugaring(libs.desugarJdkLibs)
}