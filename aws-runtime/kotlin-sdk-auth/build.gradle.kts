description = "Types and functionality for auth with the AWS Kotlin SDK"
extra["displayName"] = "ADOT Android :: AWS Auth :: Kotlin SDK"

plugins {
    id("adot.android-library")
    id("adot.android-publish")
}

android {
    namespace = "software.amazon.opentelemetry.android.auth.kotlin"
}

dependencies {
    api(platform(libs.opentelemetry.platform.alpha))
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.opentelemetry.exporter.otlp.common)
    implementation(libs.opentelemetry.sdk)
    implementation(libs.aws.smithy.kotlin.aws.credentials)
    implementation(libs.aws.smithy.kotlin.aws.signing.default)
    implementation(libs.aws.smithy.kotlin.http.auth.api)
    implementation(libs.aws.smithy.kotlin.http.auth.aws)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed")
    }
}
