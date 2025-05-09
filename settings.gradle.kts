rootProject.name = "aws-opentelemetry-android"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

include(":otelagent")
include(":demo-apps:simple-aws-demo")
include(":demo-apps:zero-code-instrumentation-demo")

include(":zero-code-agent")
