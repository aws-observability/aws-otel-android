plugins {
    id("adot.android-library")
    id("adot.android-publish")
    id("signing")
    id("maven-publish")
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
    implementation(libs.opentelemetry.android)
    implementation(libs.opentelemetry.android.common)
    implementation(libs.opentelemetry.android.session)
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.semconv)
    implementation(libs.opentelemetry.semconv.incubating)
    implementation(libs.opentelemetry.android.sessions.instrumentation)
    api(libs.opentelemetry.android.httpurlconnection)
    api(libs.opentelemetry.android.okhttp3)
    implementation(project(":ui-loading"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("aws-otel-android")
                description.set("AWS Distro for OpenTelemetry (ADOT) on Android")
                url.set("https://github.com/aws-observability/aws-otel-android")
                inceptionYear.set("2025")
                scm {
                    url.set("https://github.com/aws-observability/aws-otel-android/tree/main")
                    connection.set("scm:git:ssh://git@github.com/aws-observability/aws-otel-android.git")
                    developerConnection.set("scm:git:ssh://git@github.com/aws-observability/aws-otel-android.git")
                }

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
            }
        }
    }
    repositories {
        maven{
            url = uri("https://aws.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("sonatypeUsername") as String?
                password = project.findProperty("sonatypePassword") as String?
            }
        }
    }
}

signing {
    sign(publishing.publications)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed")
    }
}
