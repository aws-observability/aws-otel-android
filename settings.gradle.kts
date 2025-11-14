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
include(":demo-apps:agent-demo")
include(":demo-apps:crash-demo")
include("demo-apps:anr-demo")
include(":tests:contract-tests")

include(":agent")
include(":ui-loading")
include(":common")
