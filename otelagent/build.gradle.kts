plugins {
    id("adot.android-library")
    id("adot.android-publish")
}

android {
    namespace = "software.amazon.opentelemetry.android"
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java", "src/main/kotlin")
        }
        getByName("test") {
            java.srcDirs("src/test/java", "src/test/kotlin")
        }
    }
}

dependencies {
    implementation(libs.opentelemetry.android)
    implementation(libs.opentelemetry.android.common)
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.semconv)
    implementation(libs.opentelemetry.semconv.incubating)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed")
    }
}
