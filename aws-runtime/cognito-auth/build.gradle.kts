description = "Managed auth functionality with Amazon Cognito"
extra["displayName"] = "ADOT Android :: AWS Auth :: Cognito"

plugins {
    id("adot.android-library")
    id("adot.android-publish")
}

android {
    namespace = "software.amazon.opentelemetry.android.auth.cognito"

    defaultConfig {
        consumerProguardFiles("consumer-proguard-rules.pro")
    }
}

dependencies {
    implementation(libs.aws.smithy.kotlin.aws.credentials)
    implementation(libs.aws.sdk.kotlin.cognitoidentity)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed")
    }
}
