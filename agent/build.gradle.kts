plugins {
    id("adot.android-library")
    id("adot.android-publish")
    kotlin("plugin.serialization") version "2.1.21"
    id("net.bytebuddy.byte-buddy-gradle-plugin")
}

android {
    namespace = "software.amazon.opentelemetry.android.agent"
}

dependencies {
    implementation(project(":core"))
    implementation(libs.serialization)
    implementation(libs.opentelemetry.android)
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.opentelemetry.android.httpurlconnection)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed")
    }
}
