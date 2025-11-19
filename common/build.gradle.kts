plugins {
    id("adot.android-library")
    id("adot.android-publish")
}

android {
    namespace = "software.amazon.opentelemetry.android.common"

    defaultConfig {
        buildConfigField("String", "RUM_SDK_VERSION", "\"$version\"")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.opentelemetry.sdk)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed")
    }
}
