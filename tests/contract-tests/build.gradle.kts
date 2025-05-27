import kotlin.math.max

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
    maxParallelForks = max(1, Runtime.getRuntime().availableProcessors() / 2)
    enabled = project.hasProperty("runContractTests")
}

dependencies {
    implementation(libs.serialization)
    testImplementation(libs.awaitility)
}

/**
 * Task to clean the /tmp/otel-android-collector dir
 */
tasks.register("clean-collector") {
    val directoryToClean = file("/tmp/otel-android-collector")
    doLast {
        if (directoryToClean.exists()) {
            directoryToClean.deleteRecursively()
        }
    }
}
