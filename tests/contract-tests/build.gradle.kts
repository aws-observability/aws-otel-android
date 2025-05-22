plugins {
    id("adot.android-library")
    kotlin("plugin.serialization") version "2.1.21"
}

android {
    namespace = "software.amazon.opentelemetry.android"
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed")
    }
}

dependencies {
    implementation(libs.serialization)
}
