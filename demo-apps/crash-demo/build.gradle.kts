plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val javaVersion = rootProject.extra["java_version"] as JavaVersion

android {
    namespace = "software.amazon.opentelemetry.android.demo.crash"
    compileSdk = (property("android.compileSdk") as String).toInt()

    defaultConfig {
        applicationId = "software.amazon.opentelemetry.android.demo.crash"
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
    }
    
    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":agent"))
    
    // Android dependencies
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
