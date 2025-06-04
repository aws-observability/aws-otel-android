plugins {
    id("adot.android-library")
    id("adot.android-publish")
}

android {
    namespace = "software.amazon.opentelemetry.android"

    defaultConfig {
        buildConfigField("String", "RUM_SDK_VERSION", "\"$version\"")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(platform(libs.opentelemetry.platform.alpha))
    implementation(libs.opentelemetry.android)
    implementation(libs.opentelemetry.android.common)
    implementation(libs.opentelemetry.android.session)
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.semconv)
    implementation(libs.opentelemetry.semconv.incubating)
    implementation(project(":ui-loading"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed")
    }
}
