plugins {
    id("adot.android-library")
    id("adot.android-publish")
}

android {
    namespace = "software.amazon.opentelemetry.android"
}

dependencies {
    implementation(libs.opentelemetry.android)
    implementation(libs.opentelemetry.android.common)
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.semconv)
    implementation(libs.opentelemetry.semconv.incubating)
}