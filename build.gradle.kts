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
}

extra["java_version"] = JavaVersion.VERSION_1_8
extra["kotlin_min_supported_version"] = KotlinVersion.KOTLIN_1_8

allprojects {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    if (findProperty("final") != "true") {
        version = "$version-SNAPSHOT"
    }
}

subprojects {
    // Skip applying spotless to demo-apps projects
    if (!project.path.startsWith(":demo-apps") && project.name != "aws-runtime") {
        apply(plugin = "adot.spotless")

        afterEvaluate {
            tasks.named("preBuild") {
                dependsOn("spotlessApply")
            }
        }
    }
}
