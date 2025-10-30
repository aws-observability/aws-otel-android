plugins {
    id("adot.android-library")
    id("adot.android-publish")
    kotlin("plugin.serialization") version "2.1.21"
}

android {
    namespace = "software.amazon.opentelemetry.android.agent"
}

dependencies {
    api(project(":api"))
    implementation(project(":core"))
    implementation(libs.serialization)
    implementation(libs.opentelemetry.android)
    implementation(libs.opentelemetry.exporter.otlp)

    // Optional dependencies for Kotlin SDK authentication
    compileOnly(project(":aws-runtime:kotlin-sdk-auth"))

    // Test dependencies
    testImplementation(project(":aws-runtime:kotlin-sdk-auth"))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.test.core)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed")
    }
}
