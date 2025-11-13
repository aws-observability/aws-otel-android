import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath(libs.android.plugin)
        classpath(libs.kotlin.plugin)
        classpath(libs.byteBuddy.plugin)
    }
}

plugins {
    id("adot.spotless")
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

extra["java_version"] = JavaVersion.VERSION_1_8
extra["kotlin_min_supported_version"] = KotlinVersion.KOTLIN_1_8
val postReleaseTask = tasks.named("release")

allprojects {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    if (findProperty("final") != "true") {
        version = "$version-beta"
    }

    plugins.withId("maven-publish") {
        plugins.apply("signing")

        afterEvaluate {
            val publishTask = tasks.named("publishToSonatype")

            postReleaseTask.configure {
                dependsOn(publishTask)
            }
        }

        configure<PublishingExtension> {
            publications {
                register<MavenPublication>("maven") {
                    afterEvaluate {
                        artifactId = project.findProperty("archivesBaseName") as String
                    }

                    plugins.withId("java-platform") {
                        from(components["javaPlatform"])
                    }
                    plugins.withId("java") {
                        from(components["java"])
                    }

                    versionMapping {
                        allVariants {
                            fromResolutionResult()
                        }
                    }

                    pom {
                        name.set("AWS Distro for OpenTelemetry Android SDK")
                        description.set(
                            "The Amazon Web Services SDK of the OpenTelemetry Android Instrumentation.",
                        )
                        url.set("https:/github.com/aws-observability/aws-otel-android")

                        licenses {
                            license {
                                name.set("Apache License, Version 2.0")
                                url.set("https://aws.amazon.com/apache2.0")
                                distribution.set("repo")
                            }
                        }

                        developers {
                            developer {
                                id.set("amazonwebservices")
                                organization.set("Amazon Web Services")
                                organizationUrl.set("https://aws.amazon.com")
                                roles.add("developer")
                            }
                        }

                        scm {
                            connection.set("scm:git:git@github.com:aws-observability/aws-otel-android.git")
                            developerConnection.set("scm:git:git@github.com:aws-observability/aws-otel-android.git")
                            url.set("https://github.com/aws-observability/aws-otel-android.git")
                        }
                    }
                }
            }
        }

        tasks.withType<Sign>().configureEach {
            onlyIf { System.getenv("CI") == "true" }
        }

        configure<SigningExtension> {
            val signingKey = System.getenv("GPG_PRIVATE_KEY")
            val signingPassword = System.getenv("GPG_PASSPHRASE")
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(the<PublishingExtension>().publications["maven"])
        }
    }
}

subprojects {
    // Skip applying spotless to demo-apps projects
    if (!project.path.startsWith(":demo-apps") && project.name != "aws-runtime" && project.name != "tests") {
        apply(plugin = "adot.spotless")

        afterEvaluate {
            tasks.named("preBuild") {
                dependsOn("spotlessApply")
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(System.getenv("PUBLISH_TOKEN_USERNAME"))
            password.set(System.getenv("PUBLISH_TOKEN_PASSWORD"))
        }
    }
}
