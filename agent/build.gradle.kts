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
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed")
    }
}
