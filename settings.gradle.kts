rootProject.name = "aws-opentelemetry-android"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

include(":core")
include(":aws-runtime:kotlin-sdk-auth")
include(":demo-apps:simple-aws-demo")
include(":demo-apps:agent-demo")

include(":agent")
